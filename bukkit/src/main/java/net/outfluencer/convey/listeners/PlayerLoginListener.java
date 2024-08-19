package net.outfluencer.convey.listeners;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.outfluencer.convey.ConveyPlayer;
import net.outfluencer.convey.utils.CookieUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.http.WebSocket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerLoginListener implements WebSocket.Listener {

    public static final long EXPIRE_AFTER = TimeUnit.SECONDS.toMillis(10);

    private Cache<UUID, Long> verifyCache = CacheBuilder.newBuilder()
            .expireAfterWrite(EXPIRE_AFTER + 5000, TimeUnit.MILLISECONDS).build();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!event.getPlayer().isTransferred()) {
            return;
        }

        ConveyPlayer conveyPlayer = new ConveyPlayer();
        conveyPlayer.setPlayer(player);

        player.retrieveCookie(CookieUtil.VERIFY_COOKIE).thenAccept(data -> {
            CookieUtil.VerifyCookie verifyCookie = new CookieUtil.VerifyCookie();
            try {
                verifyCookie.read(new DataInputStream(new ByteArrayInputStream(data)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Preconditions.checkState(verifyCookie.getUuid().equals(player.getUniqueId()), "invalid uuid verifyCookie");
            Preconditions.checkState(verifyCookie.getCreationTime() + EXPIRE_AFTER > System.currentTimeMillis(), "cookies expired");

            Long last = verifyCache.getIfPresent(player.getUniqueId());
            if (last == null) last = System.currentTimeMillis();
            Preconditions.checkState(last > verifyCookie.getCreationTime(), "illegal verifyCookie");
            verifyCache.put(player.getUniqueId(), verifyCookie.getCreationTime());

            // todo check if the current server is forServer

            AtomicInteger amt = new AtomicInteger(verifyCookie.getClientCookies().size());
            List<CookieUtil.InternalCookie> internalCookies = new CopyOnWriteArrayList<>();
            for (String clientCookie : verifyCookie.getClientCookies()) {
                player.retrieveCookie(NamespacedKey.fromString(clientCookie)).thenAccept(d -> {

                    CookieUtil.InternalCookie internalCookie = new CookieUtil.InternalCookie();
                    try {
                        internalCookie.read(new DataInputStream(new ByteArrayInputStream(d)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Preconditions.checkState(internalCookie.getUuid().equals(player.getUniqueId()), "invalid uuid internalCookie");
                    Preconditions.checkState(internalCookie.getForServer().equals(verifyCookie.getForServer()), "invalid server");
                    Preconditions.checkState(internalCookie.getCreationTime() == verifyCookie.getCreationTime(), "not created at the same time");
                    Preconditions.checkState(amt.get() == internalCookie.getCreationTime(), "not created at the same time");
                    // valid cookie
                    internalCookies.add(internalCookie);

                    if (amt.decrementAndGet() == 0) {
                        // finished
                        finishCookie(player, verifyCookie, internalCookies);
                    }
                });
            }
        });
    }

    public void finishCookie(Player player, CookieUtil.VerifyCookie verifyCookie, List<CookieUtil.InternalCookie> internalCookies) {

    }

}
