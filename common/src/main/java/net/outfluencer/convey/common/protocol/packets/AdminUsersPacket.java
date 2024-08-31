package net.outfluencer.convey.common.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUsersPacket extends AbstractPacket {

    private List<String> users;

    @Override
    public void read(ByteBuf buf) {
        int len = readVarInt(buf);
        users = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            users.add(readString(buf));
        }
    }

    @Override
    public void write(ByteBuf buf) {
        writeVarInt(users.size(), buf);
        users.forEach(user -> writeString(user, buf));
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }


}
