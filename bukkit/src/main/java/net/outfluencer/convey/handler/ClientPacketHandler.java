package net.outfluencer.convey.handler;

import io.netty.channel.Channel;
import net.outfluencer.convey.Convey;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;

public class ClientPacketHandler extends AbstractPacketHandler {


    private Channel channel;

    @Override
    public void connected(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handle(ServerInfoPacket serverInfoPacket) {
        Convey.getInstance().setServers(serverInfoPacket.getServerInfo());
        Convey.getInstance().setServerName(serverInfoPacket.getYourName());
    }
}
