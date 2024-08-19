package net.outfluencer.convey.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.protocol.PacketRegistry;
import net.outfluencer.convey.protocol.packets.AbstractPacket;

@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<AbstractPacket> {

    @Getter
    private final boolean toServer;

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket packet, ByteBuf buf) throws Exception {
        int packetId = PacketRegistry.getPacketId(packet, toServer);
        AbstractPacket.writeVarInt(packetId, buf);
        packet.write(buf);
    }

}
