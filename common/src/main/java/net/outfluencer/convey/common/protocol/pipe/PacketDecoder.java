package net.outfluencer.convey.common.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.common.protocol.PacketRegistry;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;

import java.util.List;

@RequiredArgsConstructor
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Getter
    private final boolean toServer;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        if (!ctx.channel().isActive()) {
            return;
        }
        AbstractPacket packet = PacketRegistry.createPacket(AbstractPacket.readVarInt(buf), toServer);
        packet.read(buf);
        out.add(packet);
        System.out.println(packet);
    }
}
