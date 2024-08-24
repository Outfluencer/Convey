package net.outfluencer.convey.server.handler;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.HelloPacket;
import net.outfluencer.convey.protocol.packets.PingPacket;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;
import net.outfluencer.convey.server.Convey;
import net.outfluencer.convey.server.config.JsonServerConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ServerPacketHandler extends AbstractPacketHandler {

    public static final String authKey = "213r7t6f432e6tfzg28796rt726wtgfd7869g786G/&TG/rfw3g7fgw7rf762wrf72w9783df2qw%&";

    private boolean auth = false;
    private Channel channel;

    @Override
    public void connected(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(HelloPacket helloPacket) {
        System.out.println(helloPacket);
        Preconditions.checkState(!auth, "already authenticated");
        Preconditions.checkState(helloPacket.getKey().equals(authKey), "invalid auth key");
        auth = true;

        JsonServerConfig config = Convey.getConvey().getConfig();


        String yourName = null;

        String hostAddress = ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress();
        ArrayList<ServerInfoPacket.Host> serverInfos = new ArrayList<>();
        for (JsonServerConfig.Host host : config.hosts) {
            ServerInfoPacket.Host serverInfo = new ServerInfoPacket.Host();
            serverInfo.setName(host.getName());
            serverInfo.setAddress(host.getAddress());
            serverInfo.setRequiresPermission(host.isRequiresPermission());
            serverInfo.setJoinDirectly(host.isJoinDirectly());
            serverInfo.setFallbackServer(host.isFallbackServer());
            serverInfos.add(serverInfo);
            if(host.getAddress().equals(hostAddress + ":" + helloPacket.getPort())) {
                yourName = host.getName();
            }
        }

        Preconditions.checkState(yourName != null, "could not find your name");

        ServerInfoPacket serverInfoPacket = new ServerInfoPacket(serverInfos, yourName);
        channel.writeAndFlush(serverInfoPacket).addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });
    }

    @Override
    public void handle(PingPacket pingPacket) {
        System.out.println("received: " + pingPacket);
        channel.writeAndFlush(pingPacket);
    }
}
