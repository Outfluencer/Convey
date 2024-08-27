package net.outfluencer.convey.server;

import lombok.Getter;
import net.outfluencer.convey.server.config.JsonServerConfig;
import net.outfluencer.convey.server.netty.NettyServer;
import net.outfluencer.convey.common.utils.AESUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
            loadConfig();
        } catch (IOException e) {
            System.err.println("could not load config");
            e.printStackTrace();
            return;
        }
        System.out.println("Parsing secret key...");
        this.secretKey = AESUtils.bytesToSecretKey(Base64.getDecoder().decode(config.encryptionKey));

        System.out.println("Starting Netty server...");
        nettyServer = new NettyServer(this);
        nettyServer.startListener();
    }

    public void loadConfig() throws IOException {
        File f = new File("config.json");
        if (f.exists()) {
            config = JsonServerConfig.gson.fromJson(new FileReader(f), JsonServerConfig.class);
        } else {
            config = new JsonServerConfig();
        }
        FileWriter writer = new FileWriter(f);
        writer.write(JsonServerConfig.gson.toJson(config));
        writer.flush();
        writer.close();
        Map<String, JsonServerConfig.Host> serverInfos = new HashMap<>();
        config.getHosts().forEach(host -> serverInfos.put(host.getName(), host));
        this.serverInfos = serverInfos;
    }

}