package net.outfluencer.convey.api.player;

import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.InternalCookie;

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

    void connect(Server server);

    void sendMessage(String message);

    /*
     * Gets the local player instance of this player if possible
     */
    LocalConveyPlayer getLocalPlayer();
}
