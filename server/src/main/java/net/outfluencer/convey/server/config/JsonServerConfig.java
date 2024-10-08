package net.outfluencer.convey.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;
import net.outfluencer.convey.common.utils.AESUtils;
import net.outfluencer.convey.server.handler.ServerPacketHandler;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonServerConfig {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SerializedName("bind")
    public String bind = "0.0.0.0:21639";

    @SerializedName("encryption-key")
    public String encryptionKey = Base64.getEncoder().encodeToString(AESUtils.generateKey().getEncoded());

    @SerializedName(value = "hosts")
    public List<Host> hosts = List.of(new Host("lobby-1", "127.0.0.1:25565", false, true, true), new Host("lobby-2", "127.0.0.1:25566", false, true, true));

    @SerializedName("admin-users")
    public List<String> admins = List.of("Outfluencer");


    @Data
    @NoArgsConstructor
    public static class Host {
        @SerializedName("name")
        private String name;
        @SerializedName("address")
        private String address;
        @SerializedName("requires-permission")
        private boolean requiresPermission;
        @SerializedName("can-join-directly")
        private boolean joinDirectly;
        @SerializedName("is-fallback-server")
        private boolean fallbackServer;


        public Host(String name, String address, boolean requiresPermission, boolean joinDirectly, boolean fallbackServer) {
            this.name = name;
            this.address = address;
            this.requiresPermission = requiresPermission;
            this.joinDirectly = joinDirectly;
            this.fallbackServer = fallbackServer;
        }

        private transient List<UserData> connectedUsers = new ArrayList<>();
        private transient boolean connected = false;

        @Getter
        @Setter
        private transient ServerPacketHandler packetHandler;

        public boolean isActive() {
            return packetHandler != null && packetHandler.isConnected();
        }

        public void trySendPacket(AbstractPacket packet) {
            if (isActive()) {
                packetHandler.getChannel().eventLoop().execute(() -> packetHandler.getChannel().writeAndFlush(packet));
            }
        }

    }

    public String toString() {
        return GSON.toJson(this);
    }

}
