package net.outfluencer.convey.common.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerKickPacket extends AbstractPacket {

    private UserData userData;
    private String reason;

    @Override
    public void read(ByteBuf buf) {
        userData = new UserData();
        userData.read(buf);
        reason = readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        userData.write(buf);
        writeString(reason, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
