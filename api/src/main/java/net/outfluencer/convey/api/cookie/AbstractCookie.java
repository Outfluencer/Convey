package net.outfluencer.convey.api.cookie;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractCookie {

    public abstract void read(DataInputStream dataInputStream) throws IOException;

    public abstract void write(DataOutputStream dataOutputStream) throws IOException;

    public abstract String getCookieName();

}
