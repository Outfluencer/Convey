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
public class ServerSyncPacket extends AbstractPacket {

    private String server;

    private boolean disconnect;

    private boolean playerSync;
    private List<UserData> players;

    @Override
    public void read(ByteBuf buf) {
        server = readString(buf);
        disconnect = buf.readBoolean();
        if(disconnect) {
            return;
        }

        playerSync = buf.readBoolean();
        if (playerSync) {
            int len = readVarInt(buf);
            players = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                UserData userData = new UserData();
                userData.read(buf);
                players.add(userData);
            }
        }

    }

    @Override
    public void write(ByteBuf buf) {
        writeString(server, buf);
        buf.writeBoolean(disconnect);
        if(disconnect) {
            return;
        }

        buf.writeBoolean(playerSync);
        if (playerSync) {
            writeVarInt(players.size(), buf);
            players.forEach(user -> user.write(buf));
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

}
