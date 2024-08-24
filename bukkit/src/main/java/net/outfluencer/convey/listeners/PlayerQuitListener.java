package net.outfluencer.convey.listeners;

import net.outfluencer.convey.Convey;
import net.outfluencer.convey.api.UserData;
import net.outfluencer.convey.protocol.packets.PlayerServerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Convey.getInstance().getPlayers().remove(player);
        if(Convey.getInstance().masterIsConnected()) {
            Convey.getInstance().getMaster().getChannel().writeAndFlush(new PlayerServerPacket(false, new UserData(player.getName(), player.getUniqueId())));
        }
    }

}
