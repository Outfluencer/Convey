package net.outfluencer.convey.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Data;
import net.outfluencer.convey.utils.AESUtils;

import java.util.List;

@Data
public class AESDecoder extends ByteToMessageDecoder {

    private final AESUtils aesUtils;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(aesUtils.decrypt(bytes));
        list.add(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
