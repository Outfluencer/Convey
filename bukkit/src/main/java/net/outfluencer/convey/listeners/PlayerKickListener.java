package net.outfluencer.convey.listeners;

import net.outfluencer.convey.Convey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickListener implements Listener {

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (Convey.getInstance().fallback(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

}
