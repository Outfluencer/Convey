package net.outfluencer.convey.api.cookie;

import net.outfluencer.convey.api.cookie.builtint.KickCookie;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CookieRegistry {

    public static final String VERIFY_COOKIE = "convey:verify";
    public static final String FALLBACK_MESSAGE = "convey:fallback_message";

    static {
        COOKIE_NAME_MAP = new HashMap<>();
        COOKIE_CREATION_MAP = new HashMap<>();
        registerCookie(KickCookie.class, KickCookie::new);
        registerCookie(VerifyCookie.class, VerifyCookie::new);
    }

    private static final Map<Class<? extends AbstractCookie>, String> COOKIE_NAME_MAP;

    private static final Map<String, Supplier<? extends AbstractCookie>> COOKIE_CREATION_MAP;

    public static void registerCookie(Class<? extends AbstractCookie> clazz, Supplier<? extends AbstractCookie> creator) {
        AbstractCookie cookie = creator.get();
        COOKIE_NAME_MAP.put(clazz, cookie.getCookieName());
        COOKIE_CREATION_MAP.put(cookie.getCookieName(), creator);
    }

    public static AbstractCookie fromName(String cookieName) {
        return COOKIE_CREATION_MAP.get(cookieName).get();
    }

}
