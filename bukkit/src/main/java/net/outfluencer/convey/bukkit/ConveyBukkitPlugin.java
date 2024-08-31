package net.outfluencer.convey.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "Convey", version = "1.0-ALPHA")
@Description("A plugin that connects multiple minecraft servers")
@Author("Outfluencer")
@Website("https://github.com/Outfluencer/Convey")
@ApiVersion(ApiVersion.Target.v1_20)
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
