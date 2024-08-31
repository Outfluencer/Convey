package net.outfluencer.convey.bukkit.commands;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.bukkit.ConveyBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Commands(@org.bukkit.plugin.java.annotation.command.Command(name = "server", desc = "Show and connect to servers", permission = "convey.command.server"))
public class ServerCommand implements TabExecutor {

    private final ConveyBukkit convey;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.convey.getConveyServer() == null) {
            sender.sendMessage(ChatColor.RED + "Convey is loading...");
            return true;
        }
        if (args.length == 0) {
            ComponentBuilder builder = new ComponentBuilder("Servers you may connect to: ");
            boolean first = true;
            for (Server server : this.convey.getServers().values()) {
                if (!server.isPermissionRequired() || sender.hasPermission(server.getJoinPermission())) {
                    TextComponent component = new TextComponent(first ? server.getName() : ", " + server.getName());
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server.getName()));
                    builder.append(component);
                    first = false;
                }
            }
            sender.spigot().sendMessage(builder.create());
            return true;
        }
        if (!(sender instanceof Player player)) {
            return true;
        }
        Server server = this.convey.getServers().get(args[0]);
        this.convey.getPlayerMap().get(player).transfer(server, true, null);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) return Collections.emptyList();
        ArrayList<String> list = new ArrayList();
        Iterables.transform(Iterables.filter(this.convey.getServers().values(), input ->
                        input.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)) &&
                                (!input.isPermissionRequired() || sender.hasPermission(input.getJoinPermission()))),
                input -> input.getName()).forEach(list::add);
        return list;
    }
}
