package net.outfluencer.convey.bukkit.impl;

import net.outfluencer.convey.api.cookie.InternalCookie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CookieCache extends ArrayList<InternalCookie> {


    public CookieCache(int initialCapacity) {
        super(initialCapacity);
    }

    public CookieCache() {
        super();
    }

    public CookieCache(Collection<? extends InternalCookie> c) {
        super(c);
    }


    @Override
    public boolean add(InternalCookie newCookie) {
        removeIf(cookie -> cookie.getCookieName().equals(newCookie.getCookieName()));
        return super.add(newCookie);
    }

    @Override
    public boolean addAll(Collection<? extends InternalCookie> c) {
        c.forEach(this::add);
        return true;
    }
}
