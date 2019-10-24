package com.fileapp.storage;

import com.fileapp.utils.Crypto;
import com.fileapp.googledrive.GoogleDriveUtil;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * StorageStrategy to use GoogleDrive to store encrypted files
 */
public class GoogleDrive implements StorageStrategy {
    private static Logger LOGGER = Logger.getLogger(GoogleDrive.class.getName());

    private static final String ROOT_PATH = "ENC_ROOT";

    private static final String APPLICATION_NAME = "FileApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static Drive drive = null;

    public
    GoogleDrive() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, GoogleDriveUtil.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            String ROOT_ID = GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
            if (GoogleDriveUtil.doesFileExist(drive, ".lock", ROOT_ID)) {
                LOGGER.info("Previous executeCopy failed");
                factoryReset();
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    @Override
    public boolean isLoaded () {
        String ROOT_ID = GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
        boolean isLoaded = !GoogleDriveUtil.doesFileExist(drive, ".lock", ROOT_ID);
        LOGGER.info("isLoaded? = " + isLoaded);

        return isLoaded;
    }

    @Override
    public boolean isInitialized () {
        boolean isInitialized = false;
        try {
            isInitialized = GoogleDriveUtil.doesFileExist(drive, ROOT_PATH, "root");
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        LOGGER.info("isInitialized? = " + isInitialized);

        return isInitialized;
    }

    @Override
    public ArrayList<FileInfo> getFileList (String ID) {
        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        try {
            boolean isFolder = GoogleDriveUtil.isFolder(drive, ID);
            if (!ID.equals("") && !isFolder) {
                return null;
            }

            if (ID.equals("")) {
                ID = GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
            }

            LOGGER.info("Getting fileInfoList");

            List<File> files = GoogleDriveUtil.getFiles(drive, ID);
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File sub_file : files) {
                    System.out.printf("%s - %s - %s\n",
                            sub_file.getName(), sub_file.getId(),
                            sub_file.getMimeType().equals("application/vnd.google-apps.folder"));

                    fileInfoList.add(
                            new FileInfo(
                                    sub_file.getName(),
                                    sub_file.getId(),
                                    (sub_file.getMimeType().equals("application/vnd.google-apps.folder"))
                            )
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return fileInfoList;
    }

    @Override
    public InputStream getInputStream (String ID, String key) throws FileNotFoundException {
        InputStream driveInputStream =
                GoogleDriveUtil.getInputStream(drive, ID);
        LOGGER.info("Getting InputStream");

        return Crypto.getDecryptedInputStream(driveInputStream, key);
    }

    @Override
    public void executeCopy (String directory, String key) {
        LOGGER.info("Executing copy of files in directory: " + directory);
        
        java.io.File file = new java.io.File(directory);
        String ROOT_ID = "";

        try {
            if (!GoogleDriveUtil
                .doesFileExist(drive, ROOT_PATH, "root"))
            {
                LOGGER.info("ROOT_DIR NOT FOUND");
                LOGGER.info("Creating ROOT_DIR");
                ROOT_ID =
                        GoogleDriveUtil
                        .createFolder(drive, ROOT_PATH, "root");
            } else {
                ROOT_ID =
                        GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
            }

            LOGGER.info("Applying LOCK");
            GoogleDriveUtil.createFolder(drive, ".lock", ROOT_ID);

            copyFolder(file, ROOT_ID, key);

            LOGGER.info("Releasing LOCK");
            String lockFileId = GoogleDriveUtil.getFileID(drive, ".lock", ROOT_ID);
            GoogleDriveUtil.deleteFile(drive, lockFileId);
            LOGGER.info("Directory copied successfully");
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void copyFolder (java.io.File dir, String folder_id, String key) {
        LOGGER.info("Copying Folder: " + dir.getName());
        java.io.File[] files = dir.listFiles();

        if (files != null)
        for (java.io.File file : files) {
            if (file.isDirectory()) {
                String newFolder_id =
                        GoogleDriveUtil
                        .createFolder(drive, file.getName(), folder_id);
                copyFolder(file, newFolder_id, key);
            } else {
                LOGGER.info("Encrypting file: " + file.getAbsolutePath());
                GoogleDriveUtil
                .createEncryptedFile(drive, file,  folder_id, key);
            }
        }
    }

    @Override
    public void factoryReset() {
        LOGGER.info("Executing Factory Reset");
        String ROOT_ID = GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
        GoogleDriveUtil.deleteFile(drive, ROOT_ID);
    }
}
