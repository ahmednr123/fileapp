package com.fileapp.storage;

import com.fileapp.cache.FileInfoCache;
import com.fileapp.utils.Crypto;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * StorageStrategy to use Local file system to store encrypted files
 */
public class LocalDrive extends StorageStrategy {
    private static Logger LOGGER = Logger.getLogger(LocalDrive.class.getName());

    private static String ROOT_PATH = "enc_root";

    public
    LocalDrive () {
        super("LocalDrive");
    }

    @Override
    boolean isCorrupt () {
        return (new File(ROOT_PATH + "/.lock").exists());
    }

    @Override
    boolean checkIfInitialized () {
        File root_dir = new File(ROOT_PATH);
        File[] files = root_dir.listFiles();

        boolean isInitialized = (files != null && files.length != 0);
        LOGGER.info("isInitialized? = " + isInitialized);

        return isInitialized;
    }

    @Override
    public ArrayList<FileInfo> getFileList (String path) {
        // Check for fileInfoList in cache
        ArrayList<FileInfo> fileList = FileInfoCache.getInstance().get(path);
        if (fileList != null) {
            // Send fileInfoList cache
            LOGGER.info("Loaded fileInfoList from Cache");
            return fileList;
        }

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
                            file.length(),
                            file.isDirectory()
                    )
            );
        }

        FileInfoCache.getInstance().set(path, fileInfoList);

        return fileInfoList;
    }

    @Override
    long getFileSize (String path) {
        File file = new File(ROOT_PATH + path);
        return file.length();
    }

    @Override
    public InputStream getInputStream (String path, String key) throws FileNotFoundException {
        File file = new File(ROOT_PATH + path);
        FileInputStream fis = new FileInputStream(file);
        LOGGER.info("Getting FileInputStream");

        return Crypto.getDecryptedInputStream(fis, key);
    }

    @Override
    public void executeEncryption (String directory, String key) {
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
        copyFolder(rootDirectory, null, ROOT_PATH, key);

        File lock_file = new File (ROOT_PATH + "/.lock");
        if (lock_file.delete()) {
            LOGGER.info("Directory copied successfully");
        } else {
            LOGGER.severe("Error copying directory");
        }
    }

    private void copyFolder (File dir, String path, String to, String key) {
        if (path == null)
            path = "";

        LOGGER.info("Copying Folder: " + dir.getName());
        File[] files = dir.listFiles();
        ArrayList<FileInfo> fileList = new ArrayList<>();

        if (files != null)
        for (File file : files) {
            String full_path = to + "/" + file.getName();
            if (file.isDirectory()) {
                (new File(full_path)).mkdir();
                copyFolder(file, path + "/" + file.getName(), full_path, key);

                fileList.add(new FileInfo(file.getName(), path + "/" + file.getName(), (long)-1, true));
            } else {
                LOGGER.info("Encrypting file: " + file.getAbsolutePath());
                createEncryptedFile(
                        Crypto.getEncryptedInputStream(file, key),
                        new File(full_path)
                );

                fileList.add(new FileInfo(file.getName(), path + "/" + file.getName(), file.length(), false));
            }
        }

        FileInfoCache.getInstance().set(path, fileList);
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
    public void reset () {
        LOGGER.info("Executing Factory Reset");

        deleteDirectory(new File(ROOT_PATH));
        FileInfoCache.getInstance().clear();
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