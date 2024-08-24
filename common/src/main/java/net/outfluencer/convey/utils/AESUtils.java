package net.outfluencer.convey.utils;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {

    @SneakyThrows
    public static SecretKey generateKey() {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    @SneakyThrows
    public static byte[] encrypt(byte[] data, SecretKey key) {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    @SneakyThrows
    public static byte[] decrypt(byte[] encryptedData, SecretKey key) {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    public static SecretKey bytesToSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

}