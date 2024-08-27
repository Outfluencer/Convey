package net.outfluencer.convey.bukkit.listeners;

import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.packets.PlayerServerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ConveyBukkit convey = ConveyBukkit.getInstance();
        Player player = event.getPlayer();
        convey.getPlayers().remove(player);
        if (convey.masterIsConnected()) {
            convey.getMaster().getChannel().writeAndFlush(new PlayerServerPacket(false, new UserData(player.getName(), player.getUniqueId()), convey.getConveyServer().getName()));
        }

    }

}
