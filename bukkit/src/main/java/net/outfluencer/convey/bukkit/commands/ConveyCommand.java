package net.outfluencer.convey.bukkit.commands;

import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConveyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConveyBukkit convey = ConveyBukkit.getInstance();
        if(convey.getConveyServer() == null) {
            sender.sendMessage(ChatColor.RED + "Convey is loading...");
            return false;
        }
        sender.sendMessage(ChatColor.BLUE + "Convey by Outfluencer, current server name " + convey.getConveyServer().getName());
        for (Server server : convey.getServers().values()) {
            sender.sendMessage(ChatColor.BLUE + " - " + server);
        }
        if (convey.masterIsConnected()) {
            sender.sendMessage(ChatColor.BLUE + "Master is connected with last ping " + convey.getMaster().getPing() + "ms");
        }
        return false;
    }
}
