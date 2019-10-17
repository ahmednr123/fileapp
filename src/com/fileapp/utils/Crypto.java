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

public class Crypto {

    /**
     * Encrypt a file and copy it to the new destination
     *
     * @param key
     * @param inputFile
     * @param outputFile
     */
    public static void encrypt (String key, File inputFile, File outputFile) {
        key = applyPadding(key);
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            CipherOutputStream cos = new CipherOutputStream(outputStream, cipher);
            byte[] buf = new byte[8192];
            int read;
            while((read=inputStream.read(buf))!=-1){
                cos.write(buf,0,read);
            }

            inputStream.close();
            outputStream.flush();
            cos.close();
        } catch (Exception e) {
            System.out.println("Not allowed to access: " + inputFile.getAbsolutePath());
        }
    }

    /**
     * Get decrypted InputStream of a file
     *
     * @param file
     * @param key
     * @return
     */
    public static InputStream getInputStream (File file, String key) {
        System.out.println("[Before]");
        key = applyPadding(key);
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            System.out.println("[Inside crypto]");
            System.out.println("key: " + key);
            System.out.println("file_exists: " + file.exists());
            System.out.println("some cipher info: " + cipher.toString());

            return (new CipherInputStream(new FileInputStream(file), cipher));
        } catch (FileNotFoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            System.out.println("[Weird exception]");
            e.printStackTrace();
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
