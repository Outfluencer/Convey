package net.outfluencer.convey.protocol.packets;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.protocol.AbstractPacketHandler;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PingPacket extends AbstractPacket {

    private long time;

    @Override
    public void read(ByteBuf buf) {
        time = buf.readLong();
    }

    @Override
    public void write(ByteBuf buf) {
       buf.writeLong(time);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }

}
