package net.outfluencer.convey.bukkit.listeners;

import net.outfluencer.convey.api.cookie.InternalCookie;
import net.outfluencer.convey.api.cookie.builtint.KickCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickListener implements Listener {

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (ConveyBukkit.getInstance().fallback(event.getPlayer(), event.getReason())) {
            event.setCancelled(true);
        }
    }

}
