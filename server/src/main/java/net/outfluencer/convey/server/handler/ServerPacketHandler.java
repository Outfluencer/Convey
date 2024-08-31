package net.outfluencer.convey.server.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.common.api.CommonServer;
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
    @Getter
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
        Preconditions.checkState(!this.auth, "already authenticated");
        this.auth = true;

        JsonServerConfig config = this.convey.getConfig();
        String hostAddress = ((InetSocketAddress) this.channel.remoteAddress()).getAddress().getHostAddress();
        List<CommonServer> serverInfos = new ArrayList<>();
        for (JsonServerConfig.Host host : config.hosts) {
            if (host.getConnectedUsers() == null) {
                host.setConnectedUsers(new ArrayList<>());
            }

            serverInfos.add(new CommonServer(host.getName(),
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
        this.channel.writeAndFlush(serverInfoPacket).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });

        AdminUsersPacket adminUsersPacket = new AdminUsersPacket(config.getAdmins());
        this.channel.writeAndFlush(adminUsersPacket).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
        this.scheduledFuture = this.channel.eventLoop().scheduleAtFixedRate(() -> {
            if (!channel.isActive()) {
                scheduledFuture.cancel(false);
                return;
            }
            List<CommonServer> allServers = new ArrayList<>();
            for (JsonServerConfig.Host host : config.hosts) {
                allServers.add(new CommonServer(host.getName(),
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
        if (currentHost.getPacketHandler() == this) {
            currentHost.setPacketHandler(null);
        }
        ServerSyncPacket serverSyncPacket = new ServerSyncPacket(currentHost.getName(), true, false, List.of());
        Convey.getConvey().getServerInfos().values().forEach(host -> {
            host.trySendPacket(serverSyncPacket);
        });
    }

    @Override
    public void handle(ServerSyncPacket serverDisconnectedPacket) {
        serverDisconnectedPacket.setServer(currentHost.getName());
        Convey.getConvey().getServerInfos().values().forEach(host -> {
            host.trySendPacket(serverDisconnectedPacket);
        });
    }

    @Override
    public void handle(PingPacket pingPacket) {
        this.channel.writeAndFlush(pingPacket);
    }

    @Override
    public void handle(PlayerServerPacket playerServerPacket) {
        JsonServerConfig.Host host = this.convey.getServerInfos().get(this.currentHost.getName());
        if (playerServerPacket.isJoin()) {
            host.getConnectedUsers().add(playerServerPacket.getUserData());
        } else {
            host.getConnectedUsers().remove(playerServerPacket.getUserData());
        }

        this.convey.getServerInfos().values().forEach(info -> {
            info.trySendPacket(playerServerPacket);
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("[");
        if (this.currentHost != null) {
            sb.append(this.currentHost.getName()).append(" | ");
        }
        sb.append(this.channel.remoteAddress()).append("]");
        return sb.toString();
    }

    @Override
    public void handle(PlayerKickPacket playerKickPacket) {
        this.convey.getServerInfos().values().forEach(info -> {
            info.trySendPacket(playerKickPacket);
        });
    }
}
