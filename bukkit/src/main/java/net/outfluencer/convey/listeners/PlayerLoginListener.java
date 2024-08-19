package net.outfluencer.convey.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.net.http.WebSocket;

public class PlayerLoginListener implements WebSocket.Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if(!event.getPlayer().isTransferred()) {
            return;
        }

        player.retrieveCookie(NamespacedKey.fromString("convey:last_server")).thenAcceptAsync(cookie -> {
            if(cookie != null) {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            }
        });



    }

}
