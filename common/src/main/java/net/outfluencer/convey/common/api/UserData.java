package net.outfluencer.convey.common.api;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {

    private String username;
    private UUID uuid;

    public void read(ByteBuf buf) {
        username = AbstractPacket.readString(buf);
        uuid = AbstractPacket.readUUID(buf);
    }

    public void write(ByteBuf buf) {
        AbstractPacket.writeString(username, buf);
        AbstractPacket.writeUUID(uuid, buf);
    }

}
