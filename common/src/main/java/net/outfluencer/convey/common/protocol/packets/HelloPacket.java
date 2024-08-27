package net.outfluencer.convey.common.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelloPacket extends AbstractPacket {

    private int port;
    private List<UserData> onlinePlayers;

    @Override
    public void read(ByteBuf buf) {
        port = readVarInt(buf);
        int len = readVarInt(buf);
        onlinePlayers = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            UserData userData = new UserData();
            userData.read(buf);
            onlinePlayers.add(userData);
        }
    }

    @Override
    public void write(ByteBuf buf) {
        writeVarInt(port, buf);
        writeVarInt(onlinePlayers.size(), buf);
        onlinePlayers.forEach(user -> user.write(buf));
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

}
