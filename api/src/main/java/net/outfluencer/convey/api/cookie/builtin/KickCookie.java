package net.outfluencer.convey.api.cookie.builtin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.outfluencer.convey.api.cookie.CookieRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KickCookie extends FriendlyCookie {

    private String reason;

    @SneakyThrows
    public void read(DataInputStream dataInputStream) {
        reason = dataInputStream.readUTF();
    }

    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(reason);
    }

    @Override
    public String getCookieName() {
        return CookieRegistry.FALLBACK_MESSAGE;
    }
}
