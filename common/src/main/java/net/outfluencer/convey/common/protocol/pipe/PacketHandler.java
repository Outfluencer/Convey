package net.outfluencer.convey.common.protocol.pipe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;

@AllArgsConstructor
public class PacketHandler extends ChannelInboundHandlerAdapter {

    @Getter
    private AbstractPacketHandler handler;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof AbstractPacket packet) {
            packet.handle(handler);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handler.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        handler.disconnected(ctx.channel());
    }
}
