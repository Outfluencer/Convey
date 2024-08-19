package net.outfluencer.convey.server;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.HelloPacket;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;
import net.outfluencer.convey.server.config.JsonServerConfig;

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
        Preconditions.checkState(!auth, "already authenticated");
        Preconditions.checkState(helloPacket.getKey().equals(authKey), "invalid auth key");
        auth = true;

        ServerInfoPacket serverInfoPacket = new ServerInfoPacket();
        JsonServerConfig config = Convey.getConvey().getConfig();

        serverInfoPacket.setEncryptionKey(config.cookieEncryptionKey);

        ArrayList<ServerInfoPacket.Host> serverInfos = new ArrayList<>();
        config.hosts.forEach( host -> {
            ServerInfoPacket.Host serverInfo = new ServerInfoPacket.Host();
            serverInfo.setName(host.name);
            serverInfo.setAddress(host.address);
            serverInfo.setRequiresPermission(host.requiresPermission);
            serverInfos.add(serverInfo);
        });

        serverInfoPacket.setServerInfo(serverInfos);
        channel.writeAndFlush(serverInfoPacket);
    }
}
