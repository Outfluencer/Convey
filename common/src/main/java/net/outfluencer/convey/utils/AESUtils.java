package net.outfluencer.convey.utils;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    @SneakyThrows
    public static SecretKey generateKey() {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    @SneakyThrows
    public byte[] encrypt(byte[] data) {
        return encryptCipher.doFinal(data);
    }

    @SneakyThrows
    public byte[] decrypt(byte[] encryptedData) {
        return decryptCipher.doFinal(encryptedData);
    }

    private final Cipher decryptCipher;
    private final Cipher encryptCipher;

    public AESUtils(SecretKey key) {
        try {
            decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static SecretKey bytesToSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

}