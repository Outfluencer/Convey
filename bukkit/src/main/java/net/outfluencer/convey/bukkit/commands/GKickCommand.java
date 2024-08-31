package net.outfluencer.convey.bukkit.commands;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import net.outfluencer.convey.api.Convey;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.annotation.command.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Commands(@org.bukkit.plugin.java.annotation.command.Command(name = "gkick", desc = "Kicks a player from the network", permission = "convey.command.gkick", usage = "/gkick <player>"))
public class GKickCommand implements TabExecutor {

    private final ConveyBukkit convey;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (this.convey.getConveyServer() == null) {
            sender.sendMessage(ChatColor.RED + "Convey is loading...");
            return true;
        }

        String playerName = args[0];
        String message = args.length > 1 ? String.join(" ", args).substring(playerName.length() + 1) : this.convey.getTranslation("kick");

        this.convey.getGlobalPlayers().forEach(player -> {
            if (player.getName().equalsIgnoreCase(playerName)) {
                player.kick(message);
            }
        });


        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) return Collections.emptyList();
        ArrayList<String> list = new ArrayList();
        Iterables.transform(Iterables.filter(Convey.getInstance().getGlobalPlayers(), input ->
                        input.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))),
                input -> input.getName()).forEach(list::add);
        return list;
    }
}
