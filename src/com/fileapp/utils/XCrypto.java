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

public class XCrypto {

    public static void encrypt (String key, File inputFile, File outputFile) {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            CipherOutputStream cos = new CipherOutputStream(outputStream, cipher);
            byte[] buf = new byte[1024];
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

}
