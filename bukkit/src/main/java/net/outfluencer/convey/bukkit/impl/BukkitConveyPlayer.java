package net.outfluencer.convey.bukkit.impl;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.SneakyThrows;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.AbstractCookie;
import net.outfluencer.convey.api.cookie.CookieCache;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtin.FriendlyCookie;
import net.outfluencer.convey.api.cookie.builtin.KickCookie;
import net.outfluencer.convey.api.player.LocalConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class BukkitConveyPlayer implements LocalConveyPlayer {

    private final ConveyBukkit convey;

    // this list need to be mutable
    private CookieCache cookieCache;
    private VerifyCookie verifyCookie;

    private final Player player;

    private volatile boolean catchKicks = true;

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    public boolean transfer(Server server, boolean sendMessage, String reason) {
        if (server.getName().equals(this.convey.getConveyServer().getName())) {
            if (sendMessage) {
                this.player.sendMessage(this.convey.getTranslation("already-connected"));
            }
            return false;
        }

        if (!server.isConnected()) {
            if (sendMessage) {
                this.player.sendMessage(this.convey.getTranslation("currently-offline", server.getName()));
            }
            return false;
        }

        if (server.isPermissionRequired()) {
            if (!this.player.hasPermission(server.getJoinPermission())) {
                if (sendMessage) {
                    this.player.sendMessage(this.convey.getTranslation("join-permission-required", server.getJoinPermission()));
                }
                return false;
            }
        }

        if (reason != null) {
            this.addCookie(new KickCookie(this.convey.getTranslation("fallback", this.convey.getConveyServer().getName(), reason)));
        }

        VerifyCookie verifyCookie = new VerifyCookie();

        long creationTime = System.currentTimeMillis();
        UUID uuid = this.player.getUniqueId();
        verifyCookie.setUuid(uuid);
        verifyCookie.setFromServer(this.convey.getConveyServer().getName());
        verifyCookie.setCreationTime(creationTime);
        verifyCookie.setForServer(server.getName());

        List<String> allCookies = new ArrayList<>();
        this.getCookieCache().forEach(cookie -> {
            InternalCookie internalCookie = new InternalCookie(server.getName(), creationTime, uuid, cookie);
            allCookies.add(internalCookie.getCookieName());
            this.sendCookie(internalCookie);
        });

        verifyCookie.setClientCookies(allCookies);
        this.sendCookie(verifyCookie);

        this.player.transfer(server.getHostname(), server.getPort());
        this.convey.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(this.convey.getPlugin(), () -> {
            if (this.player.isOnline()) {
                this.player.kickPlayer("");
            }
        }, 20);
        return true;
    }
    
    @Override
    public void connect(Server server) {
        this.transfer(server, false, null);
    }

    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(message);
    }

    @Override
    public void kick(String message) {
        this.catchKicks = false;
        this.player.kickPlayer(message);
    }
    
    @SneakyThrows
    public void sendCookie(AbstractCookie cookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        cookie.write(dataOutputStream);
        byte[] encrypted = this.convey.getAesUtils().encrypt(byteArrayOutputStream.toByteArray());
        this.player.storeCookie(NamespacedKey.fromString(cookie.getCookieName()), encrypted);
    }

    @Override
    public List<FriendlyCookie> getCookies() {
        return List.copyOf(this.cookieCache);
    }

    @Override
    public boolean addCookie(FriendlyCookie cookie) {
        Preconditions.checkState(CookieRegistry.isRegistered(cookie.getClass()), "cookie not registered");
        return this.cookieCache.add(cookie);
    }
}
