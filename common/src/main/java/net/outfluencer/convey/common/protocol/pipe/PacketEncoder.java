package net.outfluencer.convey.common.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.common.protocol.PacketRegistry;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;

@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<AbstractPacket> {

    @Getter
    private final boolean toServer;

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket packet, ByteBuf buf) {
        int packetId = PacketRegistry.getPacketId(packet, toServer);
        AbstractPacket.writeVarInt(packetId, buf);
        packet.write(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
