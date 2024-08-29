package net.outfluencer.convey.api;

import net.outfluencer.convey.api.player.ConveyPlayer;

import java.util.List;

public interface Server {

    String getName();

    String getAddress();

    boolean isPermissionRequired();

    boolean isDirectJoinAllowed();

    boolean isFallbackServer();

    String getHostname();

    int getPort();

    List<ConveyPlayer> getConnectedUsers();

    default String getJoinPermission() {
        return "convey.join." + getName();
    }

    boolean isConnected();


}
