package net.outfluencer.convey.bukkit.impl;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.SneakyThrows;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.CookieCache;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtint.FriendlyCookie;
import net.outfluencer.convey.api.player.LocalConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.UUID;

@Data
public class ConveyPlayerImplBukkit implements LocalConveyPlayer {

    // this list need tp be mutable
    private CookieCache cookieCache;
    private VerifyCookie verifyCookie;

    private final Player player;

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void connect(Server server) {
        ConveyBukkit.getInstance().getTransferUtils().transferPlayer(this, server, false, null);
    }


    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @SneakyThrows
    public void sendVerifyCookie(VerifyCookie cookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        cookie.write(dataOutputStream);
        byte[] encrypted = ConveyBukkit.getInstance().getAesUtils().encrypt(byteArrayOutputStream.toByteArray());
        player.storeCookie(NamespacedKey.fromString(CookieRegistry.VERIFY_COOKIE), encrypted);
    }

    @SneakyThrows
    public void sendInternalCookie(InternalCookie cookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        cookie.write(dataOutputStream);
        byte[] encrypted = ConveyBukkit.getInstance().getAesUtils().encrypt(byteArrayOutputStream.toByteArray());
        player.storeCookie(NamespacedKey.fromString(cookie.getCookieName()), encrypted);
    }

    @Override
    public List<FriendlyCookie> getCookies() {
        return List.copyOf(cookieCache);
    }

    @Override
    public boolean addCookie(FriendlyCookie cookie) {
        Preconditions.checkState(CookieRegistry.isRegistered(cookie.getClass()), "cookie not registered");
        return cookieCache.add(cookie);
    }
}
