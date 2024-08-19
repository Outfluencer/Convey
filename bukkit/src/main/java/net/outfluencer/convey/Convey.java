package net.outfluencer.convey;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.Setter;
import net.outfluencer.convey.handler.ClientPacketHandler;
import net.outfluencer.convey.protocol.packets.HelloPacket;
import net.outfluencer.convey.protocol.packets.ServerInfoPacket;
import net.outfluencer.convey.protocol.pipe.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Convey extends JavaPlugin {

    public static final String MASTER = "157.90.241.237:21639";
    @Getter
    public static Convey instance;

    @Getter
    private final Map<Player, ConveyPlayer> players = new HashMap<>();

    private Channel masterChannel;

    @Getter
    @Setter
    public String serverName;

    @Getter
    @Setter
    public List<ServerInfoPacket.Host> servers = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (masterChannel == null || !masterChannel.isActive()) {
                connectToMaster();
            }
        }, 0, 20 * 30);

        getCommand("convey").setExecutor(new ConveyCommand());
    }

    @Override
    public void onDisable() {

    }


    public static final ChannelInitializer<io.netty.channel.Channel> CHANNEL_CHANNEL_INITIALIZER_CLIENT = new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline().addLast(new Varint21FrameDecoder());
            ch.pipeline().addLast(new ReadTimeoutHandler(30));
            ch.pipeline().addLast(new PacketDecoder(false));
            ch.pipeline().addLast(new Varint21LengthFieldPrepender());
            ch.pipeline().addLast(new PacketEncoder(true));
            ch.pipeline().addLast(new PacketHandler(new ClientPacketHandler()));
        }
    };

    public void connectToMaster() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(CHANNEL_CHANNEL_INITIALIZER_CLIENT);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.connect("157.90.241.237", 21639).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                masterChannel = channelFuture.channel();
                masterChannel.writeAndFlush(new HelloPacket("213r7t6f432e6tfzg28796rt726wtgfd7869g786G/&TG/rfw3g7fgw7rf762wrf72w9783df2qw%&", Bukkit.getPort()));
            } else {
                masterChannel = null;
                getLogger().warning("Could not connect to master server");
            }
        });
    }

}
