package net.outfluencer.convey;

import lombok.Data;
import net.outfluencer.convey.utils.CookieUtil;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ConveyPlayer {

    private List<CookieUtil.InternalCookie> internalCookies = new ArrayList<>();
    private CookieUtil.VerifyCookie verifyCookie;
    private final Player player;
    private boolean transferred;
    private String lastServer;

    @Nullable
    public String getLastServer() {
        return verifyCookie == null ? null : verifyCookie.getFromServer();
    }


}
