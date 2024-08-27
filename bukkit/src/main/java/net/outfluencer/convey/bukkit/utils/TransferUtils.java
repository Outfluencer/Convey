package net.outfluencer.convey.bukkit.utils;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtint.KickCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import net.outfluencer.convey.common.api.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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

        Player bukkitPlayer = player.getPlayer();

        if (server.isRequiresPermission()) {
            if (!bukkitPlayer.hasPermission(server.getJoinPermission())) {
                if (sendMessage) {
                    bukkitPlayer.sendMessage(convey.getTranslation("join-permission-required", server.getJoinPermission()));
                }
                return false;
            }
        }

        if(reason != null) {
            InternalCookie kickCookie = new InternalCookie(server.getName(), System.currentTimeMillis(), bukkitPlayer.getUniqueId(), new KickCookie(reason));
            player.getInternalCookies().add(kickCookie);
        }

        VerifyCookie verifyCookie = new VerifyCookie();

        long creationTime = System.currentTimeMillis();
        verifyCookie.setUuid(bukkitPlayer.getUniqueId());
        verifyCookie.setFromServer(convey.getConveyServer().getName());
        verifyCookie.setCreationTime(creationTime);
        verifyCookie.setForServer(server.getName());

        List<String> allCookies = new ArrayList<>();
        player.getInternalCookies().forEach(internalCookie -> {
            Preconditions.checkState(internalCookie.getUuid().equals(bukkitPlayer.getUniqueId()), "invalid uuid");
            internalCookie.setForServer(server.getName());
            internalCookie.setCreationTime(creationTime);
            allCookies.add(internalCookie.getCookieName());
            player.sendInternalCookie(internalCookie);
        });

        verifyCookie.setClientCookies(allCookies);
        player.sendVerifyCookie(verifyCookie);

        bukkitPlayer.transfer(server.getHostname(), server.getPort());
        convey.getServer().getScheduler().scheduleSyncDelayedTask(convey, () -> {
            if (bukkitPlayer.isOnline()) {
                // todo hackclient detection
                bukkitPlayer.kickPlayer("Hacking can result in a ban");
            }
        }, 20);
        return true;
    }

}
