package net.outfluencer.convey.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.server.handler.ServerPacketHandler;
import net.outfluencer.convey.common.utils.AESUtils;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonServerConfig {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SerializedName("bind")
    public String bind = "157.90.241.237:21639";

    @SerializedName("encryption-key")
    public String encryptionKey = Base64.getEncoder().encodeToString(AESUtils.generateKey().getEncoded());

    @SerializedName(value = "hosts")
    public List<Host> hosts = List.of(new Host("lobby-1", "157.90.241.237:25565", false, true, true), new Host("lobby-2", "157.90.241.237:25566", false, true, true));

    @SerializedName("admin-users")
    public List<String> admins = List.of("Outfluencer");


    @Data
    @NoArgsConstructor
    public class Host {
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

    }

    public String toString() {
        return gson.toJson(this);
    }

}
