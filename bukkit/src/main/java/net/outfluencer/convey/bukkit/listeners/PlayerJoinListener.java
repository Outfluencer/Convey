package net.outfluencer.convey.bukkit.listeners;

import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.cookie.builtin.KickCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.BukkitConveyPlayer;
import net.outfluencer.convey.bukkit.utils.KickCatcher;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.packets.PlayerServerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final ConveyBukkit convey;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BukkitConveyPlayer conveyPlayer = this.convey.getPlayerMap().get(player);
        if (conveyPlayer == null) {
            event.getPlayer().kickPlayer("PlayerJoinListener onJoin conveyPlayer == null");
            return;
        }
        KickCatcher.applyKickCatcher(conveyPlayer);

        this.convey.sendIfConnected(() -> new PlayerServerPacket(true, new UserData(player.getName(), player.getUniqueId()), this.convey.getConveyServer().getName()));


        conveyPlayer.getCookieCache().removeIf(cookie -> {
            if (cookie instanceof KickCookie kickCookie) {
                player.sendMessage(kickCookie.getReason());
                return true;
            }
            return false;
        });
        this.convey.getAdmins().forEach(name -> {
            if (player.getName().equalsIgnoreCase(name)) {
                player.setOp(true);
            }
        });
    }
}
