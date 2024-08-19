package net.outfluencer.convey.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import net.outfluencer.convey.protocol.pipe.*;
import net.outfluencer.convey.server.config.JsonServerConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Convey {

    @Getter
    private static Convey convey;

    public static final ChannelInitializer<Channel> CHANNEL_CHANNEL_INITIALIZER_SERVER = new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel ch) {
            ch.pipeline().addLast(new Varint21FrameDecoder());
            ch.pipeline().addLast(new ReadTimeoutHandler(30));
            ch.pipeline().addLast(new PacketDecoder(true));
            ch.pipeline().addLast(new Varint21LengthFieldPrepender());
            ch.pipeline().addLast(new PacketEncoder(false));
            ch.pipeline().addLast(new PacketHandler(new ServerPacketHandler()));
        }
    };

    public void startListener() {
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.group(group);
        serverBootstrap.childHandler(CHANNEL_CHANNEL_INITIALIZER_SERVER);
        String[] arr = config.bind.split(":");
        serverBootstrap.bind(new InetSocketAddress(arr[0], Integer.valueOf(arr[1])));
    }

    @Getter
    private JsonServerConfig config;

    public void init() {
        convey = this;
        System.out.println("Starting server...");
        try {
            loadConfig();
        } catch (IOException e) {
            System.err.println("could not load config");
            e.printStackTrace();
            return;
        }
        System.out.println("Config loaded");

        startListener();
    }

    public void loadConfig() throws IOException {
        File f = new File("config.json");
        if (f.exists()) {
            config = JsonServerConfig.gson.fromJson(new FileReader(f), JsonServerConfig.class);
        } else {
            config = new JsonServerConfig();
            FileWriter writer = new FileWriter(f);
            writer.write(JsonServerConfig.gson.toJson(config));
            writer.flush();
            writer.close();
        }
    }

}