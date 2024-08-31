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
        this.fromServer = dataInputStream.readUTF();
        this.forServer = dataInputStream.readUTF();
        this.uuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
        this.creationTime = dataInputStream.readLong();
        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            this.clientCookies.add(dataInputStream.readUTF());
        }
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(this.fromServer);
        dataOutputStream.writeUTF(this.forServer);
        dataOutputStream.writeLong(this.uuid.getMostSignificantBits());
        dataOutputStream.writeLong(this.uuid.getLeastSignificantBits());
        dataOutputStream.writeLong(this.creationTime);
        dataOutputStream.writeInt(this.clientCookies.size());
        for (String cookie : this.clientCookies) {
            dataOutputStream.writeUTF(cookie);
        }
    }

    @Override
    public String getCookieName() {
        return CookieRegistry.VERIFY_COOKIE;
    }
}