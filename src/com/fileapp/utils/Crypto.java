package com.fileapp.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;

public class Crypto {

    /**
     * Encrypt a file and copy it to the new destination
     *
     * @param key
     * @param inputFile
     * @param outputFile
     */
    public static void encrypt (String key, File inputFile, File outputFile) {
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
            e.printStackTrace();
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
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            return (new CipherInputStream(new FileInputStream(file), cipher));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String applyPadding (String key) {
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
