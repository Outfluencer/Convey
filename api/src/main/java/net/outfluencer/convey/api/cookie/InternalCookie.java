package net.outfluencer.convey.api.cookie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalCookie extends AbstractCookie {

    private String forServer;
    private long creationTime;
    private UUID uuid;
    private AbstractCookie cookie;

    @SneakyThrows
    @Override
    public void read(DataInputStream dataInputStream) {
        String cookieName = dataInputStream.readUTF();
        forServer = dataInputStream.readUTF();
        creationTime = dataInputStream.readLong();
        uuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
        (cookie = CookieRegistry.fromName(cookieName)).read(dataInputStream);
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(cookie.getCookieName());
        dataOutputStream.writeUTF(forServer);
        dataOutputStream.writeLong(creationTime);
        dataOutputStream.writeLong(uuid.getMostSignificantBits());
        dataOutputStream.writeLong(uuid.getLeastSignificantBits());
        cookie.write(dataOutputStream);
    }

    @Override
    public String getCookieName() {
        return cookie.getCookieName();
    }
}