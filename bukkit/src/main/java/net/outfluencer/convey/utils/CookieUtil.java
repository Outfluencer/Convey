package net.outfluencer.convey.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.outfluencer.convey.Convey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CookieUtil {

    public static final NamespacedKey VERIFY_COOKIE = NamespacedKey.fromString("convey:verify");


    @SneakyThrows
    public static void store(Player player, CookieUtil.VerifyCookie verifyCookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        verifyCookie.write(dataOutputStream);
        byte[] encrypted = AESUtils.encrypt(byteArrayOutputStream.toByteArray(), Convey.getInstance().getSecretKey());
        player.storeCookie(CookieUtil.VERIFY_COOKIE, encrypted);
    }

    @SneakyThrows
    public static void store(Player player, CookieUtil.InternalCookie internalCookie) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        internalCookie.write(dataOutputStream);
        byte[] encrypted = AESUtils.encrypt(byteArrayOutputStream.toByteArray(), Convey.getInstance().getSecretKey());
        player.storeCookie(NamespacedKey.fromString(internalCookie.getCookieName()), encrypted);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InternalCookie {

        private String cookieName;
        private String forServer;
        private long creationTime;
        private UUID uuid;
        private byte[] data;

        @SneakyThrows
        public void read(DataInputStream dataInputStream) {
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

        private String fromServer;
        private String forServer;
        private UUID uuid;
        private long creationTime;
        private List<String> clientCookies = new ArrayList<>();

        @SneakyThrows
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
    }

}
