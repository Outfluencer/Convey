package net.outfluencer.convey.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonServerConfig {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @SerializedName("bind")
    public String bind = "157.90.241.237:21639";

    @SerializedName("cookie-encryption-key")
    public String cookieEncryptionKey = "213r7t6f432e6tfzg28796rt726wtgfd7869g786G/&TG/rfw3g7fgw7rf762wrf72w9783df2qw%&";

    @SerializedName("hosts")
    public List<Host> hosts = List.of(new Host("lobby-1", "157.90.241.237:25565", false), new Host("lobby-2", "157.90.241.237:25566", false));


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Host {
        @SerializedName("name")
        public String name;
        @SerializedName("address")
        public String address;
        @SerializedName("requires-permission")
        public boolean requiresPermission;
    }

    public String toString() {
        return gson.toJson(this);
    }

}
