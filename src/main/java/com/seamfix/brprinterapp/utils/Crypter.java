package com.seamfix.brprinterapp.utils;

import com.seamfix.brprinterapp.config.AppConfig;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by banks on 4/13/2017.
 */

@Log4j
public class Crypter {

    public static final String KEYGEN_TYPE = "DES";
    public static final String ENC_TYPE = "DES/ECB/PKCS5Padding";

    private static SecretKey key = getKey();
    private Cipher eCipher;
    private Cipher dCipher;

    public Crypter() {
        dCipher = getDecryptCipher();
        eCipher = getEncryptCipher();
    }

    public String encrypt(String plainText) {
        try {
            byte[] encrypted = eCipher.doFinal(plainText.getBytes());
            return CommonUtils.encodeBytesToBase64String(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
            log.error("Error while encrypting", e);
        }
        return plainText;
    }

    public String decrypt(String encryptedText) {
        try {
            if (StringUtils.isBlank(encryptedText)) {
                return encryptedText;
            }
            byte[] plain = dCipher.doFinal(CommonUtils.decodeBase64StringToBytes(encryptedText));
            String plainText = new String(plain, "UTF8");

            //a recursive call to ensure proper clean up where recursive encryption may have occurred on a string value
            boolean isEncrypted = isEncrypted(plainText);
            if (isEncrypted) {
                return decrypt(plainText);
            }
            return plainText;
//            the following exceptions are expected when a valid String is finally encountered in one of the recursive call stack
//            hence the hiding of the exception stacktrace
//            it simply indicates that a valid non encrypted text has been encountered and so terminate and return the decrypted String
        } catch (IllegalArgumentException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            log.error("decryptException " + encryptedText + " " + e.getMessage());
        }

        return encryptedText;
    }

    public String decryptWithUnhandledException(String encrypted) throws BadPaddingException, IllegalBlockSizeException {
        byte[] original = dCipher.doFinal(CommonUtils.decodeBase64StringToBytes(encrypted));
        return new String(original);
    }

    private Cipher getEncryptCipher() {
        try {
            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        } catch (Exception e) {
            log.error("Error while getting encrypt cipher", e);
        }
        return null;
    }

    private Cipher getCipherInstance() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(ENC_TYPE);
    }

    private Cipher getDecryptCipher() {
        try {
            Cipher cipher = getCipherInstance();
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher;
        } catch (Exception e) {
            log.error("Error while getting decrypt cipher", e);
        }

        return null;
    }

    private static SecretKey getKey() {
        try {
            byte[] bytes = AppConfig.getEncSeed().getBytes();
            SecureRandom sr = new SecureRandom(bytes);
            KeyGenerator keyGen = KeyGenerator.getInstance(KEYGEN_TYPE);
            keyGen.init(sr);
            // If you do not initialize the KeyGenerator, each provider supply a default initialization.
            return keyGen.generateKey();
        } catch (Exception e) {
            log.error("Error while generating key", e);
        }
        return null;
    }

    public boolean isEncrypted(String text) {
        try {
            if (StringUtils.isBlank(text)) {
                return false;
            }
            if (!isAscii(text)) {
                return true;
            }
            CommonUtils.decodeBase64StringToBytes(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isAscii(String plainText) {
        for (int x = 0; x < plainText.length(); x++) {
            char ch = plainText.charAt(x);
            if (!CharUtils.isAsciiAlphanumeric(ch)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        Crypter crypter = new Crypter();
        log.debug(crypter.decrypt("U2FsdGVkX19wz36zlsTpauzG8yIj/FhCI6Uzpdi9KTM="));
        String enc = crypter.decrypt("zloDNOOvAcGHkk8W/RHtiA==");
        String four = crypter.encrypt(crypter.encrypt(crypter.encrypt(crypter.encrypt("Bankole"))));
        log.debug(four);
        log.debug(enc);
        log.debug(crypter.decrypt(four));
    }
}
