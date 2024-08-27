package net.outfluencer.convey.bukkit.impl;

import lombok.Data;
import lombok.SneakyThrows;
import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.UUID;

@Data
public class ConveyPlayerImplBukkit implements ConveyPlayer {

    // this list need tp be mutable
    private CookieCache internalCookies;

    private VerifyCookie verifyCookie;
    private final Player player;
    private boolean transferred;
    private String lastServer;

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
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


    @Nullable
    public String getLastServer() {
        return verifyCookie == null ? null : verifyCookie.getFromServer();
    }


}
