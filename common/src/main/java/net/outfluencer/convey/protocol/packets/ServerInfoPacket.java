package net.outfluencer.convey.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.protocol.AbstractPacketHandler;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServerInfoPacket extends AbstractPacket {

    private String encryptionKey;
    private List<Host> serverInfo;

    @Override
    public void read(ByteBuf buf) {
        encryptionKey = readString(buf);
        int len = readVarInt(buf);
        serverInfo = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            serverInfo.add(new Host(readString(buf), readString(buf), buf.readBoolean()));
        }
    }

    @Override
    public void write(ByteBuf buf) {
        writeString(encryptionKey, buf);
        writeVarInt(serverInfo.size(), buf);
        serverInfo.forEach(host -> {
            writeString(host.getName(), buf);
            writeString(host.getAddress(), buf);
            buf.writeBoolean(host.isRequiresPermission());
        });
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Host {
        public String name;
        public String address;
        public boolean requiresPermission;
    }

}
