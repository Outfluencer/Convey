package net.outfluencer.convey.protocol.pipe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.outfluencer.convey.protocol.AbstractPacketHandler;
import net.outfluencer.convey.protocol.packets.AbstractPacket;

@AllArgsConstructor
public class PacketHandler extends ChannelInboundHandlerAdapter {

    @Getter
    private AbstractPacketHandler handler;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(ctx  + " Packet received: " + msg);
        if(msg instanceof AbstractPacket packet) {
            packet.handle(handler);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handler.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handler.disconnected(ctx.channel());
    }
}
