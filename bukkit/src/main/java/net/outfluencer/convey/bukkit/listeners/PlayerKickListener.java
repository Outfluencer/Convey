package net.outfluencer.convey.bukkit.listeners;

import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.BukkitConveyPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

@RequiredArgsConstructor
public class PlayerKickListener implements Listener {

    private final ConveyBukkit convey;

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        BukkitConveyPlayer player = this.convey.getPlayerMap().get(event.getPlayer());
        if (!player.isCatchKicks()) {
            return;
        }
        if (this.convey.fallback(player, event.getReason())) {
            event.setCancelled(true);
        }
    }

}
