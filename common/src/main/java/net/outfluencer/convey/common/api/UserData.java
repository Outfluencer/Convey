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

    private String name;
    private UUID uniqueId;

    public void read(ByteBuf buf) {
        this.name = AbstractPacket.readString(buf);
        this.uniqueId = AbstractPacket.readUUID(buf);
    }

    public void write(ByteBuf buf) {
        AbstractPacket.writeString(this.name, buf);
        AbstractPacket.writeUUID(this.uniqueId, buf);
    }

}
