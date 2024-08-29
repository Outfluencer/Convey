package net.outfluencer.convey.common.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageToPlayerPacket extends AbstractPacket {

    private List<UUID> players;
    private String message;


    @Override
    public void read(ByteBuf buf) {
        int len = readVarInt(buf);
        players = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            players.add(new UUID(buf.readLong(), buf.readLong()));
        }
        message = readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        writeVarInt(players.size(), buf);
        players.forEach(player -> {
            buf.writeLong(player.getMostSignificantBits());
            buf.writeLong(player.getLeastSignificantBits());
        });
        writeString(message, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}