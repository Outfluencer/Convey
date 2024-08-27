package net.outfluencer.convey.api.cookie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCookie extends AbstractCookie {

    private String fromServer;
    private String forServer;
    private UUID uuid;
    private long creationTime;
    private List<String> clientCookies = new ArrayList<>();

    @SneakyThrows
    @Override
    public void read(DataInputStream dataInputStream) {
        fromServer = dataInputStream.readUTF();
        forServer = dataInputStream.readUTF();
        uuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
        creationTime = dataInputStream.readLong();
        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            clientCookies.add(dataInputStream.readUTF());
        }
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(fromServer);
        dataOutputStream.writeUTF(forServer);
        dataOutputStream.writeLong(uuid.getMostSignificantBits());
        dataOutputStream.writeLong(uuid.getLeastSignificantBits());
        dataOutputStream.writeLong(creationTime);
        dataOutputStream.writeInt(clientCookies.size());
        for (String cookie : clientCookies) {
            dataOutputStream.writeUTF(cookie);
        }
    }

    @Override
    public String getCookieName() {
        return CookieRegistry.VERIFY_COOKIE;
    }
}