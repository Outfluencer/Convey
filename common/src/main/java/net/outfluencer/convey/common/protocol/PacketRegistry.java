package net.outfluencer.convey.common.protocol;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;
import net.outfluencer.convey.common.protocol.packets.AdminUsersPacket;
import net.outfluencer.convey.common.protocol.packets.HelloPacket;
import net.outfluencer.convey.common.protocol.packets.PingPacket;
import net.outfluencer.convey.common.protocol.packets.PlayerKickPacket;
import net.outfluencer.convey.common.protocol.packets.PlayerServerPacket;
import net.outfluencer.convey.common.protocol.packets.SendMessageToPlayerPacket;
import net.outfluencer.convey.common.protocol.packets.ServerInfoPacket;
import net.outfluencer.convey.common.protocol.packets.ServerSyncPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketRegistry {

    private static final Supplier<? extends AbstractPacket>[] TO_SERVER;
    private static final Supplier<? extends AbstractPacket>[] TO_CLIENT;
    private static final Map<Class<? extends AbstractPacket>, Integer> PACKET_IDS_TO_SERVER;
    private static final Map<Class<? extends AbstractPacket>, Integer> PACKET_IDS_TO_CLIENT;


    public static AbstractPacket createPacket(int id, boolean toServer) {
        Supplier<? extends AbstractPacket>[] packets = toServer ? TO_SERVER : TO_CLIENT;
        if (id < 0 || id >= packets.length) {
            throw new DecoderException("Bad packet id " + id);
        }
        return packets[id].get();
    }

    public static int getPacketId(AbstractPacket packet, boolean toServer) {
        Map<Class<? extends AbstractPacket>, Integer> packetIds = toServer ? PACKET_IDS_TO_SERVER : PACKET_IDS_TO_CLIENT;
        Integer id = packetIds.get(packet.getClass());
        if (id == null) {
            throw new EncoderException("Bad packet " + packet);
        }
        return id;
    }


    static {
        PACKET_IDS_TO_SERVER = new HashMap<>();
        PACKET_IDS_TO_CLIENT = new HashMap<>();

        TO_SERVER = new Supplier[]{
                HelloPacket::new, PingPacket::new, PlayerServerPacket::new, ServerSyncPacket::new, PlayerKickPacket::new
        };
        TO_CLIENT = new Supplier[]{
                ServerInfoPacket::new, PingPacket::new, PlayerServerPacket::new, AdminUsersPacket::new, SendMessageToPlayerPacket::new, ServerSyncPacket::new, PlayerKickPacket::new
        };

        for (int i = 0; i < TO_SERVER.length; i++) {
            PACKET_IDS_TO_SERVER.put(TO_SERVER[i].get().getClass(), i);
        }

        for (int i = 0; i < TO_CLIENT.length; i++) {
            PACKET_IDS_TO_CLIENT.put(TO_CLIENT[i].get().getClass(), i);
        }
    }
}
