package com.fileapp.storage;

import com.fileapp.utils.Crypto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class LocalDrive implements StorageStrategy {
    private static String root = "enc_root";

    public
    LocalDrive () {
        if (!(new File(root)).exists()) {
            System.out.println();
            System.out.println();
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("=====>                   ROOT DIRECTORY NOT FOUND                         <=====");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("=====> PLEASE CREATE FOLDER: "              + root +               " <=====");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println();
            System.out.println();
        }
    }

    public boolean isLoaded () {
        return !(new File(root + "/.lock")).exists();
    }

    public boolean isInitialized () {
        File root_dir = new File(root);
        File[] files = root_dir.listFiles();

        return files != null && files.length != 0;
    }

    public JSONArray getFileList (String path) {
        File dir = new File (root + path);

        JSONArray content = new JSONArray();
        File[] files = dir.listFiles();

        if (files != null)
        for (File file : files) {
            JSONObject json = new JSONObject();
            json.put("name", file.getName());
            json.put("path", path + "/" + file.getName());
            json.put("isDirectory", file.isDirectory());

            content.put(json);
        }

        return content;
    }

    public InputStream getInputStream (String path, String key) throws FileNotFoundException {
        File file = new File(root + path);
        FileInputStream fis = new FileInputStream(file);

        return Crypto.getNewInputStream(fis, key);
    }

    public void executeCopy (String directory, String key) {
        String target = "enc_root";
        try {
            FileWriter fw = new FileWriter(target + "/.lock");
            fw.write(target);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File rootDirectory = new File(directory);
        copyFolder(rootDirectory, target, key);

        File lock_file = new File (target + "/.lock");
        if (lock_file.delete()) {
            System.out.println("Directory copied successfully");
        } else {
            System.out.println("Error copying directory");
        }
    }

    private void copyFolder (File dir, String to, String key) {
        File[] files = dir.listFiles();

        if (files != null)
            for (File file : files) {
                String path = to + "/" + file.getName();
                if (file.isDirectory()) {
                    (new File(path)).mkdir();
                    copyFolder(file, path, key);
                } else {
                    Crypto.encrypt(key, file, new File(path));
                }
            }
    }
}
