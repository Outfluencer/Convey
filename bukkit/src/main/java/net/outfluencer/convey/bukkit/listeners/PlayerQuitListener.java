package net.outfluencer.convey.bukkit.listeners;

import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.packets.PlayerServerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

    private final ConveyBukkit convey;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.convey.getPlayerMap().remove(player);
        this.convey.sendIfConnected(() -> new PlayerServerPacket(false, new UserData(player.getName(), player.getUniqueId()), this.convey.getConveyServer().getName()));
    }

}
