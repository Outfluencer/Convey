package net.outfluencer.convey.api;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.outfluencer.convey.api.player.ConveyPlayer;
import net.outfluencer.convey.api.player.LocalConveyPlayer;

import java.util.List;

public abstract class Convey {

    @Getter
    private static Convey instance;

    public static void setInstance(Convey instance) {
        Preconditions.checkNotNull( instance, "instance" );
        Preconditions.checkArgument( Convey.instance == null, "Instance already set" );
        Convey.instance = instance;
    }

    /**
     * Gets the List of local convey players
     *
     * @return the list
     */
    protected abstract List<LocalConveyPlayer> getLocalPlayers();

    /**
     * Gets the List of all convey players on the network
     *
     * @return the list
     */
    protected abstract List<ConveyPlayer> getGlobalPlayers();
}
