package net.outfluencer.convey;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConveyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.BLUE + "Convey by Outfluencer, current server name " + Convey.getInstance().getServerName());
        return false;
    }
}
