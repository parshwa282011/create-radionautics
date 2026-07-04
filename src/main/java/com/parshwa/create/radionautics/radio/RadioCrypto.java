package com.parshwa.create.radionautics.radio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class RadioCrypto {
    private RadioCrypto() {
    }

    public static byte[] encrypt(String type, byte[] data, String key) {
        return switch (normalize(type)) {
            case "none" -> data;
            case "xor" -> xor(data, key);
            case "aes" -> aes(Cipher.ENCRYPT_MODE, data, key);
            case "base64" -> Base64.getEncoder().encode(data);
            default -> throw new IllegalArgumentException("Unknown crypto type: " + type);
        };
    }

    public static byte[] decrypt(String type, byte[] data, String key) {
        return switch (normalize(type)) {
            case "none" -> data;
            case "xor" -> xor(data, key);
            case "aes" -> aes(Cipher.DECRYPT_MODE, data, key);
            case "base64" -> Base64.getDecoder().decode(data);
            default -> throw new IllegalArgumentException("Unknown crypto type: " + type);
        };
    }

    public static String encryptToString(String type, String data, String key) {
        return Base64.getEncoder().encodeToString(encrypt(type, data.getBytes(StandardCharsets.UTF_8), key));
    }

    public static String decryptToString(String type, String data, String key) {
        byte[] decoded = Base64.getDecoder().decode(data);
        return new String(decrypt(type, decoded, key), StandardCharsets.UTF_8);
    }

    private static String normalize(String type) {
        return type == null || type.isBlank() ? "none" : type.trim().toLowerCase();
    }

    private static byte[] xor(byte[] data, String key) {
        byte[] keyBytes = key == null || key.isEmpty() ? new byte[] {0} : key.getBytes(StandardCharsets.UTF_8);
        byte[] out = Arrays.copyOf(data, data.length);
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) (out[i] ^ keyBytes[i % keyBytes.length]);
        }
        return out;
    }

    private static byte[] aes(int mode, byte[] data, String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = Arrays.copyOf(digest.digest((key == null ? "" : key).getBytes(StandardCharsets.UTF_8)), 16);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, secretKey);
            return cipher.doFinal(data);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AES crypto failed", exception);
        }
    }
}
