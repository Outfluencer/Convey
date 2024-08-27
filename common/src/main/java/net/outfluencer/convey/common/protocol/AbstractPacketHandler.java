package net.outfluencer.convey.common.protocol;

import io.netty.channel.Channel;
import net.outfluencer.convey.common.protocol.packets.HelloPacket;
import net.outfluencer.convey.common.protocol.packets.PingPacket;
import net.outfluencer.convey.common.protocol.packets.PlayerServerPacket;
import net.outfluencer.convey.common.protocol.packets.ServerInfoPacket;

public class AbstractPacketHandler {

    public void connected(Channel channel) {

    }
    public void handle(HelloPacket helloPacket) {
    }

    public void handle(ServerInfoPacket serverInfoPacket) {
    }

    public void disconnected(Channel channel) {
    }

    public void handle(PingPacket pingPacket) {
    }

    public void handle(PlayerServerPacket playerServerPacket) {
    }
}
