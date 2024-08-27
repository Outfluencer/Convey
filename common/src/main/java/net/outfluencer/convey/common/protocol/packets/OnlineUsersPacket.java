package net.outfluencer.convey.common.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;
import net.outfluencer.convey.common.api.UserData;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUsersPacket extends AbstractPacket {

    private List<UserData> users;

    @Override
    public void read(ByteBuf buf) {
        int len = readVarInt(buf);
        users = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            UserData userData = new UserData();
            userData.read(buf);
            users.add(userData);
        }
    }

    @Override
    public void write(ByteBuf buf) {
        writeVarInt(users.size(), buf);
        users.forEach(user -> user.write(buf));
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {

    }


}
