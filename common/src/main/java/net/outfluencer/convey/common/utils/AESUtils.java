package net.outfluencer.convey.common.utils;

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
        return keyGenerator.generateKey();
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

    @SneakyThrows
    public AESUtils(SecretKey key) {
        this.decryptCipher = Cipher.getInstance("AES");
        this.decryptCipher.init(Cipher.DECRYPT_MODE, key);
        this.encryptCipher = Cipher.getInstance("AES");
        this.encryptCipher.init(Cipher.ENCRYPT_MODE, key);
    }


    public static SecretKey bytesToSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

}