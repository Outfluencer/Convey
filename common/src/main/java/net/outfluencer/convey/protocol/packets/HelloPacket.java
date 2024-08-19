package net.outfluencer.convey.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.protocol.AbstractPacketHandler;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelloPacket extends AbstractPacket {

    private String key;
    private int port;

    @Override
    public void read(ByteBuf buf) {
        key = readString(buf);
        port = readVarInt(buf);
    }

    @Override
    public void write(ByteBuf buf) {
        writeString(key, buf);
        writeVarInt(port, buf);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

}
