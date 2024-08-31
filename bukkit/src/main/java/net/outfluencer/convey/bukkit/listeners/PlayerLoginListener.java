package net.outfluencer.convey.bukkit.listeners;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.CookieCache;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtin.FriendlyCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerLoginListener implements Listener {

    public static final long EXPIRE_AFTER = TimeUnit.SECONDS.toMillis(30);

    private final Cache<UUID, Long> verifyCache = CacheBuilder.newBuilder()
            .expireAfterWrite(EXPIRE_AFTER + 5000, TimeUnit.MILLISECONDS).build();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConveyBukkit convey = ConveyBukkit.getInstance();

        ConveyPlayerImplBukkit conveyPlayer = new ConveyPlayerImplBukkit(player);

        Server server = convey.getConveyServer();
        if (server == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, convey.getTranslation("not-loaded"));
            return;
        }

        if(server.isPermissionRequired()) {
            if (!player.hasPermission(server.getJoinPermission())) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, convey.getTranslation("join-permission-required", server.getJoinPermission()));
                return;
            }
        }

        if (!player.isTransferred()) {
            if (server.isDirectJoinAllowed()) {
                this.postCookies(conveyPlayer, null, null);
                return;
            }
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, convey.getTranslation("trusted-server-required"));
            return;
        }

        // this is not exploitable >:)
        player.retrieveCookie(NamespacedKey.fromString(CookieRegistry.VERIFY_COOKIE)).thenAccept(verifyCookieData -> {
            try {
                if (server.isDirectJoinAllowed() && verifyCookieData == null) {
                    // bypass because we can join directly
                    this.postCookies(conveyPlayer, null, null);
                    return;
                }
                Preconditions.checkState(verifyCookieData != null && verifyCookieData.length > 0, "empty cookie");
                verifyCookieData = convey.getAesUtils().decrypt(verifyCookieData);
                VerifyCookie verifyCookie = new VerifyCookie();
                verifyCookie.read(new DataInputStream(new ByteArrayInputStream(verifyCookieData)));
                Preconditions.checkState(verifyCookie.getUuid().equals(player.getUniqueId()), "invalid uuid verifyCookie");
                Preconditions.checkState(verifyCookie.getCreationTime() + EXPIRE_AFTER > System.currentTimeMillis(), "cookies expired");

                Long lastCreationTime = this.verifyCache.getIfPresent(player.getUniqueId());
                if (lastCreationTime == null) {
                    lastCreationTime = 0L;
                }

                Preconditions.checkState(lastCreationTime < verifyCookie.getCreationTime(), "illegal verifyCookie");
                this.verifyCache.put(player.getUniqueId(), verifyCookie.getCreationTime());

                Preconditions.checkState(convey.getConveyServer() != null && verifyCookie.getForServer().equals(convey.getConveyServer().getName()), "cookie not for current server");

                if (verifyCookie.getClientCookies().isEmpty()) {
                    this.postCookies(conveyPlayer, verifyCookie, null);
                    return;
                }

                AtomicInteger amt = new AtomicInteger(verifyCookie.getClientCookies().size());
                List<FriendlyCookie> cookies = new CopyOnWriteArrayList<>();
                for (String clientCookie : verifyCookie.getClientCookies()) {
                    player.retrieveCookie(NamespacedKey.fromString(clientCookie)).thenAccept(cookieData -> {
                        try {
                            cookieData = convey.getAesUtils().decrypt(cookieData);
                            InternalCookie internalCookie = new InternalCookie();
                            internalCookie.read(new DataInputStream(new ByteArrayInputStream(cookieData)));
                            Preconditions.checkState(internalCookie.getUuid().equals(player.getUniqueId()), "invalid uuid internalCookie");
                            Preconditions.checkState(internalCookie.getForServer().equals(verifyCookie.getForServer()), "invalid server");
                            Preconditions.checkState(internalCookie.getCreationTime() == verifyCookie.getCreationTime(), "not created at the same time");
                            // valid cookie
                            cookies.add(internalCookie.getCookie());
                            if (amt.decrementAndGet() == 0) {
                                this.postCookies(conveyPlayer, verifyCookie, new CookieCache(cookies));
                            }
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
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

    public void postCookies(ConveyPlayerImplBukkit conveyPlayer, VerifyCookie verifyCookie, CookieCache internalCookies) {
        conveyPlayer.setVerifyCookie(verifyCookie);
        conveyPlayer.setCookieCache(internalCookies == null ? new CookieCache() : internalCookies);
        ConveyBukkit.getInstance().getPlayerMap().put(conveyPlayer.getPlayer(), conveyPlayer);
    }

}
