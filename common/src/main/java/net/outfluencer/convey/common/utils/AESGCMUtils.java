package net.outfluencer.convey.common.utils;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESGCMUtils {

    private final static int GCM_IV_LENGTH = 12;

    @SneakyThrows
    public byte[] decrypt(byte[] data) {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, this.key, new GCMParameterSpec(128, data, 0, AESGCMUtils.GCM_IV_LENGTH));
        return cipher.doFinal(data, AESGCMUtils.GCM_IV_LENGTH, data.length - AESGCMUtils.GCM_IV_LENGTH);
    }

    public byte[] encrypt(byte[] data) {
        return this.encrypt(this.generateIv(), data);
    }

    @SneakyThrows
    public byte[] encrypt(byte[] iv, byte[] data) {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, this.key, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(data);
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
        buffer.put(iv);
        buffer.put(encrypted);

        return buffer.array();
    }

    private final SecretKey key;
    private final SecureRandom random;

    @SneakyThrows
    public AESGCMUtils(SecretKey key) {
        this.key = key;
        this.random = new SecureRandom();
    }

    public byte[] generateIv() {
        final byte[] iv = new byte[AESGCMUtils.GCM_IV_LENGTH];
        this.random.nextBytes(iv);
        return iv;
    }

}