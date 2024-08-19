package net.outfluencer.convey.protocol;

import io.netty.channel.Channel;
import net.outfluencer.convey.protocol.packets.HelloPacket;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;

public class AbstractPacketHandler {

    public void connected(Channel channel) {

    }
    public void handle(HelloPacket helloPacket) {
    }

    public void handle(ServerInfoPacket serverInfoPacket) {
        System.out.println(serverInfoPacket);
    }

    public void disconnected(Channel channel) {
    }
}
