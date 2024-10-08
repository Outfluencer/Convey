package net.outfluencer.convey.bukkit.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.annotation.command.Commands;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Commands(@org.bukkit.plugin.java.annotation.command.Command(name = "glist", desc = "Lists all players on the network", permission = "convey.command.glist"))
public class GListCommand implements TabExecutor {

    private final ConveyBukkit convey;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.convey.getConveyServer() == null) {
            sender.sendMessage(ChatColor.RED + "Convey is loading...");
            return true;
        }
        ComponentBuilder builder = new ComponentBuilder("List of all servers: ");
        this.convey.getServers().values().forEach(server -> {
            builder.append("\n")
                    .append(server.getName())
                    .append("(")
                    .append(String.valueOf(server.getConnectedUsers().size()))
                    .append("): ");
            boolean first = true;
            for (ConveyPlayer connectedUser : server.getConnectedUsers()) {
                builder.append(first ? connectedUser.getName() : ", " + connectedUser.getName());
                first = false;
            }
        });
        sender.spigot().sendMessage(builder.create());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
