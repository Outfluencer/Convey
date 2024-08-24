package net.outfluencer.convey.utils;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.Convey;
import net.outfluencer.convey.ConveyPlayer;
import net.outfluencer.convey.api.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TransferUtils {

    private final Convey convey;

    public boolean transferPlayer(ConveyPlayer player, Server server, boolean sendMessage) {

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

        CookieUtil.VerifyCookie verifyCookie = new CookieUtil.VerifyCookie();

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
            convey.getCookieUtil().store(bukkitPlayer, internalCookie);
        });

        verifyCookie.setClientCookies(allCookies);
        convey.getCookieUtil().store(bukkitPlayer, verifyCookie);

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
