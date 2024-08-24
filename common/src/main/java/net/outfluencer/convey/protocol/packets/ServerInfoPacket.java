package net.outfluencer.convey.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.protocol.AbstractPacketHandler;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ServerInfoPacket extends AbstractPacket {

    private List<Host> serverInfo;
    private String yourName;

    @Override
    public void read(ByteBuf buf) {
        int len = readVarInt(buf);
        serverInfo = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            serverInfo.add(new Host(readString(buf), readString(buf), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));
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
        });
        writeString(yourName, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) {
        handler.handle(this);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Host {
        private String name;
        private String address;
        private boolean requiresPermission;
        private boolean joinDirectly;
        private boolean fallbackServer;
    }

}
