package net.outfluencer.convey.bukkit.listeners;

import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickListener implements Listener {

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        ConveyPlayerImplBukkit player = ConveyBukkit.getInstance().getPlayerMap().get(event.getPlayer());
        if(!player.isCatchKicks()) {
            return;
        }
        if (ConveyBukkit.getInstance().fallback(player, event.getReason())) {
            event.setCancelled(true);
        }
    }

}
