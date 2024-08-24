package net.outfluencer.convey.listeners;

import net.outfluencer.convey.Convey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Convey.getInstance().getPlayers().remove(event.getPlayer());
    }

}
