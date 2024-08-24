package net.outfluencer.convey.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.api.UserData;
import net.outfluencer.convey.protocol.AbstractPacketHandler;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerServerPacket extends AbstractPacket {

    boolean join;
    private UserData userData;

    @Override
    public void read(ByteBuf buf) {
        join = buf.readBoolean();
        userData = new UserData();
        userData.read(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(join);
        userData.write(buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
