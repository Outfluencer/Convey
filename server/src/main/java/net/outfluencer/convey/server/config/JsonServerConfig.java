package net.outfluencer.convey.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.outfluencer.convey.utils.AESUtils;

import java.util.Base64;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonServerConfig {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SerializedName("bind")
    public String bind = "157.90.241.237:21639";

    @SerializedName("encryption-key")
    public String encryptionKey = Base64.getEncoder().encodeToString(AESUtils.generateKey().getEncoded());

    @SerializedName("hosts")
    public List<Host> hosts = List.of(new Host("lobby-1", "157.90.241.237:25565", false, true, true), new Host("lobby-2", "157.90.241.237:25566", false, true, true));


    @Data
    @AllArgsConstructor
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
    }

    public String toString() {
        return gson.toJson(this);
    }

}
