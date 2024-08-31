package net.outfluencer.convey.bukkit.utils;

import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtin.KickCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class TransferUtils {

    private final ConveyBukkit convey;

    public boolean transferPlayer(ConveyPlayerImplBukkit player, Server server, boolean sendMessage, String reason) {

        if (server.getName().equals(convey.getConveyServer().getName())) {
            if (sendMessage) {
                player.getPlayer().sendMessage(convey.getTranslation("already-connected"));
            }
            return false;
        }

        if (!server.isConnected()) {
            if (sendMessage) {
                player.getPlayer().sendMessage(convey.getTranslation("currently-offline", server.getName()));
            }
            return false;
        }

        Player bukkitPlayer = player.getPlayer();

        if (server.isPermissionRequired()) {
            if (!bukkitPlayer.hasPermission(server.getJoinPermission())) {
                if (sendMessage) {
                    bukkitPlayer.sendMessage(convey.getTranslation("join-permission-required", server.getJoinPermission()));
                }
                return false;
            }
        }

        if (reason != null) {
            player.addCookie(new KickCookie(convey.getTranslation("fallback", convey.getConveyServer().getName(), reason)));
        }

        VerifyCookie verifyCookie = new VerifyCookie();

        long creationTime = System.currentTimeMillis();
        UUID uuid = bukkitPlayer.getUniqueId();
        verifyCookie.setUuid(uuid);
        verifyCookie.setFromServer(convey.getConveyServer().getName());
        verifyCookie.setCreationTime(creationTime);
        verifyCookie.setForServer(server.getName());

        List<String> allCookies = new ArrayList<>();
        player.getCookieCache().forEach(cookie -> {
            InternalCookie internalCookie = new InternalCookie(server.getName(), creationTime, uuid, cookie);
            allCookies.add(internalCookie.getCookieName());
            player.sendInternalCookie(internalCookie);
        });

        verifyCookie.setClientCookies(allCookies);
        player.sendVerifyCookie(verifyCookie);

        bukkitPlayer.transfer(server.getHostname(), server.getPort());
        convey.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(convey.getPlugin(), () -> {
            if (bukkitPlayer.isOnline()) {
                bukkitPlayer.kickPlayer("");
            }
        }, 20);
        return true;
    }

}
