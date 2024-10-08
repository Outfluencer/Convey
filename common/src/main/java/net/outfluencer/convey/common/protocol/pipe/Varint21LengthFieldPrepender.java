package net.outfluencer.convey.common.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;

@ChannelHandler.Sharable
public class Varint21LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        int bodyLen = msg.readableBytes();
        int headerLen = varintSize(bodyLen);
        out.ensureWritable(headerLen + bodyLen);

        AbstractPacket.writeVarInt(bodyLen, out);
        out.writeBytes(msg);
    }

    static int varintSize(int i) {
        if ((i & 0xFFFFFF80) == 0) {
            return 1;
        }
        if ((i & 0xFFFFC000) == 0) {
            return 2;
        }
        if ((i & 0xFFE00000) == 0) {
            return 3;
        }
        if ((i & 0xF0000000) == 0) {
            return 4;
        }
        return 5;
    }
}