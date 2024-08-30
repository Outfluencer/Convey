package net.outfluencer.convey.bukkit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.outfluencer.convey.api.Convey;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.api.player.LocalConveyPlayer;
import net.outfluencer.convey.bukkit.commands.ConveyCommand;
import net.outfluencer.convey.bukkit.commands.GListCommand;
import net.outfluencer.convey.bukkit.commands.ServerCommand;
import net.outfluencer.convey.bukkit.handler.ClientPacketHandler;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import net.outfluencer.convey.bukkit.listeners.PlayerJoinListener;
import net.outfluencer.convey.bukkit.listeners.PlayerKickListener;
import net.outfluencer.convey.bukkit.listeners.PlayerLoginListener;
import net.outfluencer.convey.bukkit.listeners.PlayerQuitListener;
import net.outfluencer.convey.bukkit.utils.KickCatcher;
import net.outfluencer.convey.bukkit.utils.TransferUtils;
import net.outfluencer.convey.common.protocol.packets.AbstractPacket;
import net.outfluencer.convey.common.protocol.pipe.*;
import net.outfluencer.convey.common.utils.AESUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Format;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ConveyBukkit extends Convey {

    public static ConveyBukkit getInstance() {
        return (ConveyBukkit) Convey.getInstance();
    }

    @Getter
    private final JavaPlugin plugin;
    @Getter
    private final Map<Player, ConveyPlayerImplBukkit> playerMap = new HashMap<>();
    @Setter
    @Getter
    private ClientPacketHandler master;
    @Getter
    @Setter
    public Server conveyServer;
    @Getter
    @Setter
    private Map<String, Server> servers = new HashMap<>();
    private Map<String, Format> messageFormats;
    @Getter
    private TransferUtils transferUtils = new TransferUtils(this);
    @Getter
    private SecretKey secretKey;
    @Getter
    private AESUtils aesUtils;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    @Getter
    @Setter
    private List<String> admins = new ArrayList<>();

    @Override
    public List<LocalConveyPlayer> getLocalPlayers() {
        Collection collection = playerMap.values();
        return (List<LocalConveyPlayer>) List.copyOf(collection);
    }

    @Override
    public List<ConveyPlayer> getGlobalPlayers() {
        List<ConveyPlayer> list = new ArrayList<>();
        servers.values().forEach(server -> list.addAll(server.getConnectedUsers()));
        return List.copyOf(list);
    }

    public boolean masterIsConnected() {
        return master != null && master.isActive();
    }

    public boolean sendIfConnected(Supplier<AbstractPacket> packet) {
        if (masterIsConnected()) {
            master.getChannel().writeAndFlush(packet.get());
            return true;
        }
        return false;
    }

    public void onEnable() {
        setInstance(this);

        // preload
        CookieRegistry.VERIFY_COOKIE.length();
        KickCatcher.VERSION.length();

        plugin.saveDefaultConfig();
        try {
            secretKey = AESUtils.bytesToSecretKey(Base64.getDecoder().decode(plugin.getConfig().getString("encryption_key")));
        } catch (Exception exception) {
            plugin.getLogger().severe("Please set the encryption key in the config.yml to the same as in the convey server config.json");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        aesUtils = new AESUtils(secretKey);
        reloadMessages();

        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerKickListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);


        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!masterIsConnected()) {
                master = null;
                connectToMaster();
            }
        }, 0, 20 * 30);

        plugin.getCommand("convey").setExecutor(new ConveyCommand());
        plugin.getCommand("server").setExecutor(new ServerCommand());
        plugin.getCommand("glist").setExecutor(new GListCommand());

        Bukkit.getOnlinePlayers().forEach(player -> playerMap.put(player, new ConveyPlayerImplBukkit(player)));
    }

    public void onDisable() {
        if (master != null) {
            master.close();
        }
        eventLoopGroup.shutdownGracefully();
    }


    public ChannelInitializer<io.netty.channel.Channel> CHANNEL_CHANNEL_INITIALIZER_CLIENT = new ChannelInitializer<>() {
        @Override
        protected void initChannel(Channel ch) {
            AESUtils aes = new AESUtils(secretKey);

            ch.pipeline().addLast(new Varint21FrameDecoder());
            ch.pipeline().addLast(new AESDecoder(aes));
            ch.pipeline().addLast(new ReadTimeoutHandler(30));
            ch.pipeline().addLast(new PacketDecoder(false));
            ch.pipeline().addLast(new Varint21LengthFieldPrepender());
            ch.pipeline().addLast(new AESEncoder(aes));
            ch.pipeline().addLast(new PacketEncoder(true));
            ch.pipeline().addLast(new PacketHandler(new ClientPacketHandler(ConveyBukkit.this)));
        }
    };

    public void connectToMaster() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(CHANNEL_CHANNEL_INITIALIZER_CLIENT);
        bootstrap.group(eventLoopGroup);
        new NioEventLoopGroup().close();
        bootstrap.connect("157.90.241.237", 21639).addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                channelFuture.cause().printStackTrace();
                plugin.getLogger().warning("Could not connect to master server");
            }
        });
    }

    public final void reloadMessages() {
        Map<String, Format> cachedFormats = new HashMap<>();
        File file = new File("messages.properties");
        if (file.isFile()) {
            try (FileReader rd = new FileReader(file)) {
                cacheResourceBundle(cachedFormats, new PropertyResourceBundle(rd));
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not load custom messages.properties", ex);
            }
        }
        ResourceBundle baseBundle = ResourceBundle.getBundle("messages");
        cacheResourceBundle(cachedFormats, baseBundle);
        messageFormats = Collections.unmodifiableMap(cachedFormats);
    }

    private void cacheResourceBundle(Map<String, Format> map, ResourceBundle resourceBundle) {
        Enumeration<String> keys = resourceBundle.getKeys();
        while (keys.hasMoreElements()) {
            map.computeIfAbsent(keys.nextElement(), (key) -> new MessageFormat(resourceBundle.getString(key)));
        }
    }

    public String getTranslation(String name, Object... args) {
        Format format = messageFormats.get(name);
        return (format != null) ? format.format(args) : "<translation '" + name + "' missing>";
    }

    public boolean fallback(Player player, String reason) {
        for (Server server : servers.values()) {
            if (server.isFallbackServer() && transferUtils.transferPlayer(this.getPlayerMap().get(player), server, false, reason)) {
                return true;
            }
        }
        return false;
    }

    public Server fallbackServerName(Player player) {
        for (Server server : servers.values()) {
            if (server.isFallbackServer() && !server.getName().equals(getConveyServer().getName())) {
                if (server.isPermissionRequired()) {
                    if (!player.hasPermission(server.getJoinPermission())) {
                        continue;
                    }
                }
                return server;
            }
        }
        return null;
    }

}
