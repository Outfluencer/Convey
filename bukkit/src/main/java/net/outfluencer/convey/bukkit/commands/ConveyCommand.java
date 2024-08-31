package net.outfluencer.convey.bukkit.commands;

import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;

@RequiredArgsConstructor
@Commands(@org.bukkit.plugin.java.annotation.command.Command(name = "convey", desc = "Convey command"))
public class ConveyCommand implements CommandExecutor {

    private final ConveyBukkit convey;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.convey.getConveyServer() == null) {
            sender.sendMessage(ChatColor.RED + "Convey is loading...");
            return true;
        }
        sender.sendMessage(ChatColor.BLUE + "Convey by Outfluencer, current server name " + this.convey.getConveyServer().getName());
        for (Server server : this.convey.getServers().values()) {
            sender.sendMessage(ChatColor.BLUE + " - " + server);
        }
        if (this.convey.masterIsConnected()) {
            sender.sendMessage(ChatColor.BLUE + "Master is connected with last ping " + this.convey.getMaster().getPing() + "ms");
        }
        return false;
    }
}
