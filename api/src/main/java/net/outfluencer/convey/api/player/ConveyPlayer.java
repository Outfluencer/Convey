package net.outfluencer.convey.api.player;

import java.util.UUID;

public interface ConveyPlayer {

    /*
     * Get the name of the player
     */
    String getName();

    /*
     * Get the uuid of the player
     */
    UUID getUniqueId();

    /*
     * Get if the player is transferred
     */
    boolean isTransferred();

    /*
     * Get the server name of the server the player was transferred from to this server
     */
    String getLastServer();

}
