package net.outfluencer.convey.common.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.common.api.Server;
import net.outfluencer.convey.common.protocol.AbstractPacketHandler;
import net.outfluencer.convey.common.api.UserData;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ServerInfoPacket extends AbstractPacket {

    private List<Server> serverInfo;
    private String yourName;

    @Override
    public void read(ByteBuf buf) {
        int len = readVarInt(buf);
        serverInfo = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            String name = readString(buf);
            String address = readString(buf);
            boolean requiresPermission = buf.readBoolean();
            boolean joinDirectly = buf.readBoolean();
            boolean fallbackServer = buf.readBoolean();
            int userLen = readVarInt(buf);
            List<UserData> users = new ArrayList<>();
            for (int j = 0; j < userLen; j++) {
                UserData userData = new UserData();
                userData.read(buf);
                users.add(userData);
            }
            boolean online = buf.readBoolean();
            serverInfo.add(new Server(name, address, requiresPermission, joinDirectly, fallbackServer, users, online));
        }
        yourName = readString(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        writeVarInt(serverInfo.size(), buf);
        serverInfo.forEach(host -> {
            writeString(host.getName(), buf);
            writeString(host.getAddress(), buf);
            buf.writeBoolean(host.isRequiresPermission());
            buf.writeBoolean(host.isJoinDirectly());
            buf.writeBoolean(host.isFallbackServer());

            writeVarInt(host.getConnectedUsers().size(), buf);
            host.getConnectedUsers().forEach(user -> user.write(buf));
            buf.writeBoolean(host.isOnline());
        });
        writeString(yourName, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) {
        handler.handle(this);
    }


}
