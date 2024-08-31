package net.outfluencer.convey.server;

import lombok.Getter;
import net.outfluencer.convey.server.config.JsonServerConfig;
import net.outfluencer.convey.server.netty.NettyServer;
import net.outfluencer.convey.common.utils.AESUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// TODO fix duplicate login
// TODO convey plugin api support
public class Convey {

    @Getter
    private static Convey convey;
    @Getter
    private SecretKey secretKey;
    @Getter
    private JsonServerConfig config;
    @Getter
    private NettyServer nettyServer;
    @Getter
    private Map<String, JsonServerConfig.Host> serverInfos;

    public void init() {
        convey = this;

        System.out.println("Loading config...");
        try {
            this.loadConfig();
        } catch (IOException e) {
            System.err.println("could not load config");
            e.printStackTrace();
            return;
        }
        System.out.println("Parsing secret key...");
        this.secretKey = AESUtils.bytesToSecretKey(Base64.getDecoder().decode(this.config.encryptionKey));

        System.out.println("Starting Netty server...");
        this.nettyServer = new NettyServer(this);
        this.nettyServer.startListener();
    }

    public void loadConfig() throws IOException {
        Path path = Path.of("config.json");
        this.config = Files.exists(path) ? JsonServerConfig.GSON.fromJson(Files.newBufferedReader(path), JsonServerConfig.class) : new JsonServerConfig();

        Files.writeString(path, JsonServerConfig.GSON.toJson(this.config), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Map<String, JsonServerConfig.Host> serverInfos = new HashMap<>();
        this.config.getHosts().forEach(host -> serverInfos.put(host.getName(), host));
        this.serverInfos = serverInfos;
    }

}