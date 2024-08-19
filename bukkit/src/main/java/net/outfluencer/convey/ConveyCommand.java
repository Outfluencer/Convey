package net.outfluencer.convey;

import net.outfluencer.convey.protocol.packets.ServerInfoPacket;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConveyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.BLUE + "Convey by Outfluencer, current server name " + Convey.getInstance().getServerName());
        for (ServerInfoPacket.Host server : Convey.getInstance().getServers()) {
            sender.sendMessage(ChatColor.BLUE + " - " + server.getName() + " " + server.getAddress() + " " + (server.isRequiresPermission() ? "Requires permission" : "No permission required"));
        }
        return false;
    }
}
