package net.outfluencer.convey.bukkit.listeners;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import net.outfluencer.convey.bukkit.utils.KickCatcher;
import net.outfluencer.convey.common.api.Server;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerLoginListener implements Listener {

    public static final long EXPIRE_AFTER = TimeUnit.SECONDS.toMillis(30);

    private Cache<UUID, Long> verifyCache = CacheBuilder.newBuilder()
            .expireAfterWrite(EXPIRE_AFTER + 5000, TimeUnit.MILLISECONDS).build();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConveyBukkit convey = ConveyBukkit.getInstance();

        ConveyPlayerImplBukkit conveyPlayer = new ConveyPlayerImplBukkit(player);
        conveyPlayer.setTransferred(player.isTransferred());

        Server server = convey.getConveyServer();
        if(server.isRequiresPermission()){
            if (!player.hasPermission(server.getJoinPermission())) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, convey.getTranslation("join-permission-required", server.getJoinPermission()));
                return;
            }
        }

        if (!player.isTransferred()) {
            if (server != null && server.isJoinDirectly()) {
                postCookies(conveyPlayer, null, Arrays.asList());
                return;
            }
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, convey.getTranslation("trusted-server-required"));
            return;
        }


        player.retrieveCookie(NamespacedKey.fromString(CookieRegistry.VERIFY_COOKIE)).thenAccept(data -> {
            try {
                if(server.isJoinDirectly() && data == null) {
                    // bypass because we can join directly
                    postCookies(conveyPlayer, null, Arrays.asList());
                    return;
                }
                Preconditions.checkState(data != null && data.length > 0, "empty cookie");
                data = convey.getAesUtils().decrypt(data);
                VerifyCookie verifyCookie = new VerifyCookie();
                verifyCookie.read(new DataInputStream(new ByteArrayInputStream(data)));
                Preconditions.checkState(verifyCookie.getUuid().equals(player.getUniqueId()), "invalid uuid verifyCookie ");
                Preconditions.checkState(verifyCookie.getCreationTime() + EXPIRE_AFTER > System.currentTimeMillis(), "cookies expired");


                Long lastCreationTime = verifyCache.getIfPresent(player.getUniqueId());
                if (lastCreationTime == null) lastCreationTime = 0L;
                Preconditions.checkState(lastCreationTime < verifyCookie.getCreationTime(), "illegal verifyCookie");
                verifyCache.put(player.getUniqueId(), verifyCookie.getCreationTime());


                Preconditions.checkState(convey.getConveyServer() != null && verifyCookie.getForServer().equals(convey.getConveyServer().getName()), "cookie not for current server");

                if (verifyCookie.getClientCookies().isEmpty()) {
                    postCookies(conveyPlayer, verifyCookie, Arrays.asList());
                    return;
                }

                AtomicInteger amt = new AtomicInteger(verifyCookie.getClientCookies().size());
                List<InternalCookie> internalCookies = new CopyOnWriteArrayList<>();
                for (String clientCookie : verifyCookie.getClientCookies()) {
                    player.retrieveCookie(NamespacedKey.fromString(clientCookie)).thenAccept(d -> {
                        try {
                            d = convey.getAesUtils().decrypt(d);
                            InternalCookie internalCookie = new InternalCookie();
                            internalCookie.read(new DataInputStream(new ByteArrayInputStream(d)));
                            Preconditions.checkState(internalCookie.getUuid().equals(player.getUniqueId()), "invalid uuid internalCookie");
                            Preconditions.checkState(internalCookie.getForServer().equals(verifyCookie.getForServer()), "invalid server");
                            Preconditions.checkState(internalCookie.getCreationTime() == verifyCookie.getCreationTime(), "not created at the same time");
                            // valid cookie
                            internalCookies.add(internalCookie);
                            if (amt.decrementAndGet() == 0) {
                                postCookies(conveyPlayer, verifyCookie, new ArrayList<>(internalCookies));
                            }
                        } catch (Throwable throwable) {
                            player.kickPlayer(convey.getTranslation("invalid-cookie", throwable));
                        }

                    });
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                player.kickPlayer(convey.getTranslation("invalid-cookie", throwable));
            }

        });
    }

    public void postCookies(ConveyPlayerImplBukkit conveyPlayer, VerifyCookie verifyCookie, List<InternalCookie> internalCookies) {
        conveyPlayer.setVerifyCookie(verifyCookie);
        conveyPlayer.setInternalCookies(new ArrayList<>(internalCookies));
        ConveyBukkit.getInstance().getPlayers().put(conveyPlayer.getPlayer(), conveyPlayer);
    }

}
