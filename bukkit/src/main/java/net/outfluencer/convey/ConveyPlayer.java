package net.outfluencer.convey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
public class ConveyPlayer {


    Player player;
    HashMap<String, byte[]> data = new HashMap<>();

    boolean transferred;
    private String lastServer;


    // needs to be encrypted


}
