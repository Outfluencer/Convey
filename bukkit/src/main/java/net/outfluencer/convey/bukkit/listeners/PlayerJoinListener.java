package net.outfluencer.convey.bukkit.listeners;

import net.outfluencer.convey.api.cookie.builtin.KickCookie;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import net.outfluencer.convey.bukkit.impl.ConveyPlayerImplBukkit;
import net.outfluencer.convey.bukkit.utils.KickCatcher;
import net.outfluencer.convey.common.api.UserData;
import net.outfluencer.convey.common.protocol.packets.PlayerServerPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ConveyBukkit conveyBukkit = ConveyBukkit.getInstance();
        Player player = event.getPlayer();
        ConveyPlayerImplBukkit conveyPlayer = ConveyBukkit.getInstance().getPlayerMap().get(player);
        if (conveyPlayer == null) {
            event.getPlayer().kickPlayer("PlayerJoinListener onJoin conveyPlayer == null");
            return;
        }
        KickCatcher.applyKickCatcher(conveyPlayer);

        conveyBukkit.sendIfConnected( () -> new PlayerServerPacket(true, new UserData(player.getName(), player.getUniqueId()), ConveyBukkit.getInstance().getConveyServer().getName()));


        conveyPlayer.getCookieCache().removeIf(cookie -> {
            if (cookie instanceof KickCookie kickCookie) {
                player.sendMessage(kickCookie.getReason());
                return true;
            }
           return false;
        });
        conveyBukkit.getAdmins().forEach(name -> {
            if (player.getName().equalsIgnoreCase(name)) {
                player.setOp(true);
            }
        });
    }
}
