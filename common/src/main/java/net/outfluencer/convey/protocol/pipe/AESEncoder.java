package net.outfluencer.convey.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import net.outfluencer.convey.utils.AESUtils;

@AllArgsConstructor
public class AESEncoder extends MessageToByteEncoder<ByteBuf> {

    private final AESUtils aesUtils;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf in, ByteBuf out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        out.writeBytes(aesUtils.encrypt(bytes));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
