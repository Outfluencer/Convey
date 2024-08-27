package net.outfluencer.convey.server.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.common.api.Server;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;
import net.outfluencer.convey.common.protocol.packets.*;
import net.outfluencer.convey.server.Convey;
import net.outfluencer.convey.server.config.JsonServerConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class ServerPacketHandler extends AbstractPacketHandler {

    private final Convey convey;

    private boolean auth = false;
    private Channel channel;
    @Getter
    private JsonServerConfig.Host currentHost;
    private ScheduledFuture<?> scheduledFuture;

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }


    @Override
    public void connected(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(HelloPacket helloPacket) {
        Preconditions.checkState(!auth, "already authenticated");
        auth = true;

        JsonServerConfig config = convey.getConfig();
        String hostAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        List<Server> serverInfos = new ArrayList<>();
        for (JsonServerConfig.Host host : config.hosts) {
            if(host.getConnectedUsers() == null) host.setConnectedUsers(new ArrayList<>());
            serverInfos.add(new Server(host.getName(),
                    host.getAddress(),
                    host.isRequiresPermission(),
                    host.isJoinDirectly(),
                    host.isFallbackServer(),
                    new CopyOnWriteArrayList<>(host.getConnectedUsers()), host.isConnected()));
            if (host.getAddress().equals(hostAddress + ":" + helloPacket.getPort())) {
                this.currentHost = host;
                this.currentHost.setConnectedUsers(helloPacket.getOnlinePlayers());
                this.currentHost.setPacketHandler(this);
                this.currentHost.setConnected(true);
            }
        }

        Preconditions.checkState(this.currentHost != null, "could not find your name");

        ServerInfoPacket serverInfoPacket = new ServerInfoPacket(serverInfos, this.currentHost.getName());
        channel.writeAndFlush(serverInfoPacket).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });

        AdminUsersPacket adminUsersPacket = new AdminUsersPacket(config.getAdmins());
        channel.writeAndFlush(adminUsersPacket).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
        scheduledFuture = channel.eventLoop().scheduleAtFixedRate(() -> {
            if(!channel.isActive()) {
                scheduledFuture.cancel(false);
                return;
            }
            List<Server> allServers = new ArrayList<>();
            for (JsonServerConfig.Host host : config.hosts) {
                allServers.add(new Server(host.getName(),
                        host.getAddress(),
                        host.isRequiresPermission(),
                        host.isJoinDirectly(),
                        host.isFallbackServer(),
                        host.getConnectedUsers(), host.isConnected()));
            }
            channel.writeAndFlush(new ServerInfoPacket(allServers, this.currentHost.getName()));
        }, 30, 30, java.util.concurrent.TimeUnit.SECONDS);

        System.out.println(this + " connected");
    }

    @Override
    public void disconnected(Channel channel) {
        currentHost.getConnectedUsers().clear();
        currentHost.setConnected(false);
        if(currentHost.getPacketHandler() == this) {
            currentHost.setPacketHandler(null);
        }
    }

    @Override
    public void handle(PingPacket pingPacket) {
        channel.writeAndFlush(pingPacket);
    }

    @Override
    public void handle(PlayerServerPacket playerServerPacket) {
        JsonServerConfig.Host host = convey.getServerInfos().get(currentHost.getName());
        if (playerServerPacket.isJoin()) {
            host.getConnectedUsers().add(playerServerPacket.getUserData());
        } else {
            host.getConnectedUsers().remove(playerServerPacket.getUserData());
        }

        convey.getServerInfos().values().forEach( h -> {
            ServerPacketHandler packetHandler = h.getPacketHandler();
            if(packetHandler != null && packetHandler.isConnected()) {
                packetHandler.channel.writeAndFlush(playerServerPacket);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("[");
        if (currentHost != null) {
            sb.append(currentHost.getName()).append(" | ");
        }
        sb.append(channel.remoteAddress()).append("]");
        return sb.toString();
    }
}
