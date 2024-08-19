package net.outfluencer.convey.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.NamespacedKey;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CookieUtil {

    public static final NamespacedKey VERIFY_COOKIE = NamespacedKey.fromString("convey:verify");

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InternalCookie {

        private String cookieName;
        private String forServer;
        private long creationTime;
        private UUID uuid;
        private byte[] data;

        public void read(DataInputStream dataInputStream) throws IOException {
            cookieName = dataInputStream.readUTF();
            forServer = dataInputStream.readUTF();
            creationTime = dataInputStream.readLong();
            uuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
            data = new byte[dataInputStream.available()];
        }

        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeUTF(cookieName);
            dataOutputStream.writeUTF(forServer);
            dataOutputStream.writeLong(creationTime);
            dataOutputStream.writeLong(uuid.getMostSignificantBits());
            dataOutputStream.writeLong(uuid.getLeastSignificantBits());
            dataOutputStream.write(data);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VerifyCookie {
        private String forServer;
        private UUID uuid;
        private long creationTime;
        private List<String> clientCookies;

        public void read(DataInputStream dataInputStream) throws IOException {
            forServer = dataInputStream.readUTF();
            uuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
            creationTime = dataInputStream.readLong();
            int size = dataInputStream.readInt();
            for(int i = 0; i < size; i++) {
                clientCookies.add(dataInputStream.readUTF());
            }
        }

        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeUTF(forServer);
            dataOutputStream.writeLong(uuid.getMostSignificantBits());
            dataOutputStream.writeLong(uuid.getLeastSignificantBits());
            dataOutputStream.writeLong(creationTime);
            dataOutputStream.writeInt(clientCookies.size());
            for(String cookie : clientCookies) {
                dataOutputStream.writeUTF(cookie);
            }
        }
    }

}
