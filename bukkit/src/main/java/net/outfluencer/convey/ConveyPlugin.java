package net.outfluencer.convey;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConveyPlugin extends JavaPlugin {

    @Getter
    private final Map<Player, ConveyPlayer> players = new HashMap<>();

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
