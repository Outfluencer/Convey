package net.outfluencer.convey.server.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.HelloPacket;
import net.outfluencer.convey.protocol.packets.PingPacket;
import net.outfluencer.convey.protocol.packets.PlayerServerPacket;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;
import net.outfluencer.convey.server.Convey;
import net.outfluencer.convey.server.config.JsonServerConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerPacketHandler extends AbstractPacketHandler {

    private boolean auth = false;
    private Channel channel;
    private String serverName;


    @Override
    public void connected(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(HelloPacket helloPacket) {
        Preconditions.checkState(!auth, "already authenticated");
        auth = true;

        JsonServerConfig config = Convey.getConvey().getConfig();
        String hostAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        List<Server> serverInfos = new ArrayList<>();
        for (JsonServerConfig.Host host : config.hosts) {
            serverInfos.add(new Server(host.getName(),
                    host.getAddress(),
                    host.isRequiresPermission(),
                    host.isJoinDirectly(),
                    host.isFallbackServer(),
                    helloPacket.getOnlinePlayers()));
            if (host.getAddress().equals(hostAddress + ":" + helloPacket.getPort())) {
                serverName = host.getName();
            }
        }

        Preconditions.checkState(serverName != null, "could not find your name");

        ServerInfoPacket serverInfoPacket = new ServerInfoPacket(serverInfos, serverName);
        channel.writeAndFlush(serverInfoPacket).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });

        System.out.println(this + " connected");
    }

    @Override
    public void handle(PingPacket pingPacket) {
        channel.writeAndFlush(pingPacket);
    }

    @Override
    public void handle(PlayerServerPacket playerServerPacket) {
        super.handle(playerServerPacket);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("[");
        if (serverName != null) {
            sb.append(serverName).append(" | ");
        }
        sb.append(channel.remoteAddress()).append("]");
        return sb.toString();
    }
}
