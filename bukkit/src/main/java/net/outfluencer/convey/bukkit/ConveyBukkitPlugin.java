package net.outfluencer.convey.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public class ConveyBukkitPlugin extends JavaPlugin {

   private ConveyBukkit convey;

    @Override
    public void onEnable() {
        this.convey = new ConveyBukkit(this);
        this.convey.onEnable();
    }

    @Override
    public void onDisable() {
        this.convey.onDisable();
    }
}
