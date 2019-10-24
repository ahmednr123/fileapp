package com.fileapp.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Crypto {
    private static Logger LOGGER = Logger.getLogger(Crypto.class.getName());

    public static InputStream getDecryptedInputStream (InputStream is, String key) {
        key = applyPadding(key);
        try {
            LOGGER.info("Decrypting InputStream");
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            LOGGER.info("Sending Decrypted InputStream");
            return (new CipherInputStream(is, cipher));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    public static InputStream getEncryptedInputStream (File file, String key) {
        key = applyPadding(key);
        try {
            LOGGER.info("Encrypting InputStream");
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            LOGGER.info("Sending Encrypted InputStream");
            return (new CipherInputStream(new FileInputStream(file), cipher));
        } catch (FileNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    private static String applyPadding (String key) {
        if ((key.length() % 16) != 0) {
            int padding_length = 16 - (key.length() % 16);
            char pad = 'x';
            for (int i = 0; i < padding_length; i++) {
                key += pad;
            }
        }
        return key;
    }

}
