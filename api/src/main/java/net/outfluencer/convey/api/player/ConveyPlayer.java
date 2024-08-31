package net.outfluencer.convey.api.player;

import net.outfluencer.convey.api.Server;
import net.outfluencer.convey.api.cookie.InternalCookie;

import java.util.UUID;

public interface ConveyPlayer {

    /**
     * Get the name of the player
     */
    String getName();

    /**
     * Get the uuid of the player
     */
    UUID getUniqueId();

    /**
     * Connects the player to a specified server
     * @param server the server to connect to
     */
    void connect(Server server);

    /**
     * Send a message to the player
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Gets the local player instance of this player if possible
     * or else null
     */
    LocalConveyPlayer getLocalPlayer();

    /**
     * Kicks the player from the network
     * @param message the reason of the kick
     */
    void kick(String message);
}
