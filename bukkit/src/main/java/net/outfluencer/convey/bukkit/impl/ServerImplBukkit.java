package net.outfluencer.convey.bukkit.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.bukkit.ConveyBukkit;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class ServerImplBukkit implements Server {

    private final String name;
    private final String address;
    private final boolean permissionRequired;
    private final boolean directJoinAllowed;
    private final String hostname;
    private final int port;
    private boolean fallbackServer;
    private List<ConveyPlayer> connectedUsers;

    public void setConnectedUsers(List<ConveyPlayer> connectedUsers) {
        this.connectedUsers = new ArrayList<>(connectedUsers);
    }

    private boolean connected;

    public List<ConveyPlayer> getConnectedUsers() {
        if (ConveyBukkit.getInstance().getConveyServer() != null && ConveyBukkit.getInstance().getConveyServer().getName().equals(getName())) {
            return (List) ConveyBukkit.getInstance().getPlayers();
        }
        return connectedUsers;
    }
}
