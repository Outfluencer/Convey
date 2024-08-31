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
public class PlayerServerPacket extends AbstractPacket {

    private boolean join;
    private UserData userData;
    private String serverName;

    @Override
    public void read(ByteBuf buf) {
        join = buf.readBoolean();
        userData = new UserData();
        userData.read(buf);
        serverName = readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(join);
        userData.write(buf);
        writeString(serverName, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
