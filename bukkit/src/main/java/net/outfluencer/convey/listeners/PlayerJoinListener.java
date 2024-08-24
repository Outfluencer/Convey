package net.outfluencer.convey.listeners;

import net.outfluencer.convey.Convey;
import net.outfluencer.convey.api.UserData;
import net.outfluencer.convey.protocol.packets.PlayerServerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(Convey.getInstance().masterIsConnected()) {
            Convey.getInstance().getMaster().getChannel().writeAndFlush(new PlayerServerPacket(true, new UserData(player.getName(), player.getUniqueId())));
        }
    }
}
