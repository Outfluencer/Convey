package net.outfluencer.convey;

import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class ConveyPlayer {


    Player player;
    boolean transferred;
    private String lastServer;

}
