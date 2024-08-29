package net.outfluencer.convey.api.cookie;

import net.outfluencer.convey.api.cookie.builtint.FriendlyCookie;
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
    }

    private static final Map<Class<? extends FriendlyCookie>, String> COOKIE_NAME_MAP;

    private static final Map<String, Supplier<? extends FriendlyCookie>> COOKIE_CREATION_MAP;

    public static void registerCookie(Class<? extends FriendlyCookie> clazz, Supplier<? extends FriendlyCookie> creator) {
        if (isRegistered(clazz)) {
            throw new IllegalArgumentException("Cookie already registered " + clazz);
        }
        FriendlyCookie cookie = creator.get();
        COOKIE_NAME_MAP.put(clazz, cookie.getCookieName());
        COOKIE_CREATION_MAP.put(cookie.getCookieName(), creator);
    }

    public static FriendlyCookie fromName(String cookieName) {
        return COOKIE_CREATION_MAP.get(cookieName).get();
    }

    public static boolean isRegistered(Class<? extends FriendlyCookie> clazz) {
        return COOKIE_NAME_MAP.containsKey(clazz);
    }

}
