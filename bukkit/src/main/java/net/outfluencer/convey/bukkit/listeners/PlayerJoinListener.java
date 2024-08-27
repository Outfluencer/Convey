package net.outfluencer.convey.bukkit.listeners;

import net.outfluencer.convey.api.cookie.CookieRegistry;
import net.outfluencer.convey.api.cookie.builtint.KickCookie;
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
        ConveyPlayerImplBukkit conveyPlayer = ConveyBukkit.getInstance().getPlayers().get(player);
        KickCatcher.applyKickCatcher(conveyPlayer);
        if(conveyBukkit.masterIsConnected()) {
            conveyBukkit.getMaster().getChannel().writeAndFlush(new PlayerServerPacket(true, new UserData(player.getName(), player.getUniqueId()), ConveyBukkit.getInstance().getConveyServer().getName()));
        }

        conveyPlayer.getInternalCookies().removeIf(cookie -> {
            if (cookie.getCookie() instanceof KickCookie kickCookie) {
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
