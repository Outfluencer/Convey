package net.outfluencer.convey.api;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static net.outfluencer.convey.protocol.packets.AbstractPacket.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {

    private String username;
    private UUID uuid;

    public void read(ByteBuf buf) {
        username = readString(buf);
        uuid = readUUID(buf);
    }

    public void write(ByteBuf buf) {
        writeString(username, buf);
        writeUUID(uuid, buf);
    }

}
