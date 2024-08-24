package net.outfluencer.convey;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;
import lombok.Setter;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.commands.ConveyCommand;
import net.outfluencer.convey.commands.ServerCommand;
import net.outfluencer.convey.handler.ClientPacketHandler;
import net.outfluencer.convey.listeners.PlayerJoinListener;
import net.outfluencer.convey.listeners.PlayerKickListener;
import net.outfluencer.convey.listeners.PlayerLoginListener;
import net.outfluencer.convey.listeners.PlayerQuitListener;
import net.outfluencer.convey.protocol.pipe.*;
import net.outfluencer.convey.utils.AESUtils;
import net.outfluencer.convey.utils.CookieUtil;
import net.outfluencer.convey.utils.TransferUtils;
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
import java.util.logging.Level;

public class Convey extends JavaPlugin {

    @Getter
    private static Convey instance;
    @Getter
    private final Map<Player, ConveyPlayer> players = new HashMap<>();
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
    @Getter
    private CookieUtil cookieUtil;

    public boolean masterIsConnected() {
        return master != null && master.isActive();
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        try {
            secretKey = AESUtils.bytesToSecretKey(Base64.getDecoder().decode(getConfig().getString("encryption_key")));
        } catch (Exception exception) {
            getLogger().severe("Please set the encryption key in the config.yml to the same as in the convey server config.json");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;
        aesUtils = new AESUtils(secretKey);
        cookieUtil = new CookieUtil();

        reloadMessages();

        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerKickListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);



        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (!masterIsConnected()) {
                master = null;
                connectToMaster();
            }
        }, 0, 20 * 30);

        getCommand("convey").setExecutor(new ConveyCommand());
        getCommand("server").setExecutor(new ServerCommand());

        Bukkit.getOnlinePlayers().forEach(player -> players.put(player, new ConveyPlayer(player)));
    }

    @Override
    public void onDisable() {
        if (master != null) {
            master.close();
        }
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
            ch.pipeline().addLast(new PacketHandler(new ClientPacketHandler(Convey.this)));
        }
    };

    public void connectToMaster() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(CHANNEL_CHANNEL_INITIALIZER_CLIENT);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.connect("157.90.241.237", 21639).addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                getLogger().warning("Could not connect to master server");
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
                getLogger().log(Level.SEVERE, "Could not load custom messages.properties", ex);
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

    public boolean fallback(Player player) {
        for (Server server : servers.values()) {
            if(server.isFallbackServer() && transferUtils.transferPlayer(getPlayers().get(player), server, false)) {
                return true;
            }
        }
        return false;
    }

}
