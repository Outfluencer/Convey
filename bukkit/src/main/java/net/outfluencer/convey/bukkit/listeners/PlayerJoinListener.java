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
        Player player = event.getPlayer();
        ConveyPlayerImplBukkit conveyPlayer = ConveyBukkit.getInstance().getPlayers().get(player);
        KickCatcher.applyKickCatcher(conveyPlayer);
        if(ConveyBukkit.getInstance().masterIsConnected()) {
            ConveyBukkit.getInstance().getMaster().getChannel().writeAndFlush(new PlayerServerPacket(true, new UserData(player.getName(), player.getUniqueId()), ConveyBukkit.getInstance().getConveyServer().getName()));
        }

        conveyPlayer.getInternalCookies().stream()
                .filter( internalCookie -> internalCookie.getCookie() instanceof KickCookie)
                .map(internalCookie -> (KickCookie) internalCookie.getCookie())
                .findAny().ifPresent(kickCookie -> player.sendMessage(kickCookie.getReason()));
    }
}
