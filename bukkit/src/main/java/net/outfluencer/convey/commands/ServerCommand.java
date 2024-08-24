package net.outfluencer.convey.commands;

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.outfluencer.convey.Convey;
import net.outfluencer.convey.api.Server;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ServerCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Convey convey = Convey.getInstance();
        if (convey.getConveyServer() == null) {
            sender.sendMessage(ChatColor.RED + "Convey is loading...");
            return false;
        }
        if (args.length == 0) {
            ComponentBuilder builder = new ComponentBuilder("Servers you may connect to: ");
            boolean first = true;
            for (Server server : convey.getServers().values()) {
                if (!server.isRequiresPermission() || sender.hasPermission(server.getJoinPermission())) {
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
        Server server = convey.getServers().get(args[0]);
        convey.getTransferUtils().transferPlayer(convey.getPlayers().get(player), server, true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) return Collections.emptyList();
        ArrayList<String> list = new ArrayList();
        Iterables.transform(Iterables.filter(Convey.getInstance().getServers().values(), input ->
                        input.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)) &&
                                (!input.isRequiresPermission() || sender.hasPermission(input.getJoinPermission()))),
                input -> input.getName()).forEach(list::add);
        return list;
    }
}
