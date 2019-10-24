package com.fileapp.storage;

import com.fileapp.utils.Crypto;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * StorageStrategy to use Local file system to store encrypted files
 */
public class LocalDrive implements StorageStrategy {
    private static Logger LOGGER = Logger.getLogger(LocalDrive.class.getName());

    private static String ROOT_PATH = "enc_root";

    public
    LocalDrive () {
        // Check if .lock exists which would mean the previous copy execution wasn't
        // completed. The application is factoryReset to overcome the problem
        if ((new File(ROOT_PATH + "/.lock").exists())) {
            LOGGER.info("Previous executeCopy failed");
            factoryReset();
        }
    }

    @Override
    public boolean isLoaded () {
        boolean isLoaded = !(new File(ROOT_PATH + "/.lock")).exists();
        LOGGER.info("isLoaded? = " + isLoaded);

        return isLoaded;
    }

    @Override
    public boolean isInitialized () {
        File root_dir = new File(ROOT_PATH);
        File[] files = root_dir.listFiles();
        boolean isInitialized = (files != null && files.length != 0);
        LOGGER.info("isInitialized? = " + isInitialized);

        return isInitialized;
    }

    @Override
    public ArrayList<FileInfo> getFileList (String path) {
        File dir = new File (ROOT_PATH + path);

        ArrayList<FileInfo> fileInfoList = new ArrayList<>();
        File[] files = dir.listFiles();

        LOGGER.info("Getting fileInfoList");

        System.out.println("Files:");
        if (files != null)
        for (File file : files) {
            System.out.printf("%s - %s - %s\n", file.getName(), path + "/" + file.getName(), file.isDirectory());

            fileInfoList.add(
                    new FileInfo(
                            file.getName(),
                            path + "/" + file.getName(),
                            file.isDirectory()
                    )
            );
        }

        return fileInfoList;
    }

    @Override
    public InputStream getInputStream (String path, String key) throws FileNotFoundException {
        File file = new File(ROOT_PATH + path);
        FileInputStream fis = new FileInputStream(file);
        LOGGER.info("Getting FileInputStream");

        return Crypto.getDecryptedInputStream(fis, key);
    }

    @Override
    public void executeCopy (String directory, String key) {
        LOGGER.info("Executing copy of files in directory: " + directory);
        File root_file = new File(ROOT_PATH);
        if (!root_file.exists()) {
            // Create ROOT_PATH if it doesn't exist
            LOGGER.info("ROOT_DIR NOT FOUND");
            LOGGER.info("Creating ROOT_DIR");
            root_file.mkdir();
        }
        
        try {
            LOGGER.info("Creating .lock file");
            FileWriter fw = new FileWriter(ROOT_PATH + "/.lock");
            fw.write(ROOT_PATH);
            fw.close();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        File rootDirectory = new File(directory);
        copyFolder(rootDirectory, ROOT_PATH, key);

        File lock_file = new File (ROOT_PATH + "/.lock");
        if (lock_file.delete()) {
            LOGGER.info("Directory copied successfully");
        } else {
            LOGGER.info("Error copying directory");
        }
    }

    private void copyFolder (File dir, String to, String key) {
        LOGGER.info("Copying Folder: " + dir.getName());
        File[] files = dir.listFiles();

        if (files != null)
            for (File file : files) {
                String path = to + "/" + file.getName();
                if (file.isDirectory()) {
                    (new File(path)).mkdir();
                    copyFolder(file, path, key);
                } else {
                    LOGGER.info("Encrypting file: " + file.getAbsolutePath());
                    createEncryptedFile(
                            Crypto.getEncryptedInputStream(file, key),
                            new File(path)
                    );
                }
            }
    }

    private void createEncryptedFile (InputStream inputStream, File target) {
        try {
            FileOutputStream fos = new FileOutputStream(target);

            byte[] buf = new byte[8192];
            int read;
            while((read=inputStream.read(buf))!=-1){
                fos.write(buf,0,read);
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    @Override
    public void factoryReset () {
        LOGGER.info("Executing Factory Reset");
        deleteDirectory(new File(ROOT_PATH));
    }

    private void deleteDirectory (File directory) {
        LOGGER.info("Deleting directory: " + directory.getName());
        File[] files = directory.listFiles();
        if (files!=null)
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                LOGGER.info("Deleting file: " + file.getName());
                file.delete();
            }
        }

        directory.delete();
    }
}
