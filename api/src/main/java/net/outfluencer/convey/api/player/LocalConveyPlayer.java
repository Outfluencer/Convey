package net.outfluencer.convey.api.player;

import net.outfluencer.convey.api.cookie.VerifyCookie;
import net.outfluencer.convey.api.cookie.builtin.FriendlyCookie;

import java.util.List;

public interface LocalConveyPlayer extends ConveyPlayer {

    @Override
    default LocalConveyPlayer getLocalPlayer() {
        return this;
    }

    /*
     * Adds a cookie to the player's cookie cache.
     * @return true if a cookie of the same type was already present and was replaced, false otherwise
     */
    boolean addCookie(FriendlyCookie cookie);

    VerifyCookie getVerifyCookie();

    List<FriendlyCookie> getCookies();

}
