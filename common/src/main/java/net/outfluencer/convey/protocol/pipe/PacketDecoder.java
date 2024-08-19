package net.outfluencer.convey.protocol.pipe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.protocol.PacketRegistry;
import net.outfluencer.convey.protocol.packets.AbstractPacket;

import java.util.List;

@RequiredArgsConstructor
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Getter
    private final boolean toServer;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            return;
        }
        int packetId = AbstractPacket.readVarInt(buf);
        System.out.println("Packet ID " + toServer + ": " + packetId);
        AbstractPacket packet = PacketRegistry.createPacket(packetId, toServer);
        packet.read(buf);
        out.add(packet);
    }
}
