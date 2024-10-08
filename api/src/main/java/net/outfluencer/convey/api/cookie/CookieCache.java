package net.outfluencer.convey.api.cookie;

import net.outfluencer.convey.api.cookie.builtin.FriendlyCookie;

import java.util.ArrayList;
import java.util.Collection;

public class CookieCache extends ArrayList<FriendlyCookie> {


    public CookieCache(int initialCapacity) {
        super(initialCapacity);
    }

    public CookieCache() {
        super();
    }

    public CookieCache(Collection<? extends FriendlyCookie> c) {
        super(c);
    }

    @Override
    public boolean add(FriendlyCookie newCookie) {
        boolean removed = super.removeIf(cookie -> cookie.getCookieName().equals(newCookie.getCookieName()));
        super.add(newCookie);
        return removed;
    }

    @Override
    public boolean addAll(Collection<? extends FriendlyCookie> collection) {
        collection.forEach(this::add);
        return true;
    }


}
