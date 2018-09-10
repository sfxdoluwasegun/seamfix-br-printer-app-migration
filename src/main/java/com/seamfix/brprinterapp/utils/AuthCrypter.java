package com.seamfix.brprinterapp.utils;

import lombok.extern.log4j.Log4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.TimeZone;

@Log4j
public class AuthCrypter {

    public static void main(String[] args) throws BadPaddingException {
        String key = "bankole";
        String clean = "WIN-12345:" + System.currentTimeMillis() + ":" + TimeZone.getDefault().getID();

        try {
            String encrypted = encrypt(clean, key);
            log.debug(encrypted);
            long st = System.currentTimeMillis();
            String decrypted = decrypt(encrypted, key);
            log.debug("Time taken to decrypt: " + (System.currentTimeMillis() - st) + "ms");
            log.debug(decrypted);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            log.error("GeneralSecurityException", e);
        }
    }

    public static String encrypt(String plainText, String key) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] clean = plainText.getBytes();

        // Generating IV.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        digest.update(key.getBytes("UTF-8"));
        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    public static String decrypt(String cipherText, String key) throws GeneralSecurityException {
        int ivSize = 16;
        int keySize = 16;
        byte[] encryptedIvTextBytes = Base64.getDecoder().decode(cipherText);

        // Extract IV.
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(key.getBytes());
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
    }
}