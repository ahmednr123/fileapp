package com.fileapp.storage;

import com.fileapp.cache.FileInfoCache;
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
public class GoogleDrive extends StorageStrategy {
    private static Logger LOGGER = Logger.getLogger(GoogleDrive.class.getName());

    private static final String ROOT_PATH = "ENC_ROOT";
    private static String ROOT_ID = "";

    private static final String APPLICATION_NAME = "FileApp";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static Drive drive = null;

    public
    GoogleDrive() {
        super("GoogleDrive");
    }

    private
    Drive getDrive () {
        if (drive == null) {
            try {
                // Connect to Google Drive
                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, GoogleDriveUtil.getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                ROOT_ID = GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }
        }

        return drive;
    }

    @Override
    boolean isCorrupt () {
        Drive drive = getDrive();

        if (ROOT_ID != null) {
            return GoogleDriveUtil.doesFileExist(drive, ".lock", ROOT_ID);
        } else {
            return false;
        }
    }

    @Override
    boolean checkIfInitialized () {
        Drive drive = getDrive();
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
        Drive drive = getDrive();

        ArrayList<FileInfo> fileList = FileInfoCache.getInstance().get(ID.equals("") ? ROOT_ID : ID);
        if (fileList != null) {
            // Send fileInfoList cache
            LOGGER.info("Loaded fileInfoList from Cache");
            return fileList;
        }

        ArrayList<FileInfo> fileInfoList = new ArrayList<>();

        // Confirm if the file ID is folder
        boolean isFolder = GoogleDriveUtil.isFolder(drive, ID );
        if (!ID.equals("") && !isFolder) {
            return null;
        }

        // If empty string is passed get files from ROOT_PATH
        if (ID.equals("")) {
            ID = GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
            ROOT_ID = ID;
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

        FileInfoCache.getInstance().set(ID, fileInfoList);

        return fileInfoList;
    }

    @Override
    public InputStream getInputStream (String ID, String key) {
        Drive drive = getDrive();

        InputStream driveInputStream =
                GoogleDriveUtil.getInputStream(drive, ID);
        LOGGER.info("Getting InputStream");

        return Crypto.getDecryptedInputStream(driveInputStream, key);
    }

    @Override
    public void executeEncryption (String directory, String key) {
        Drive drive = getDrive();

        LOGGER.info("Executing copy of files in directory: " + directory);
        
        java.io.File file = new java.io.File(directory);

        if (!GoogleDriveUtil
            .doesFileExist(drive, ROOT_PATH, "root"))
        {
            // Create ROOT_PATH if it doesn't exist
            LOGGER.info("ROOT_DIR NOT FOUND");
            LOGGER.info("Creating ROOT_DIR");
            ROOT_ID =
                    GoogleDriveUtil
                    .createFolder(drive, ROOT_PATH, "root");
        } else {
            ROOT_ID =
                    GoogleDriveUtil.getFileID(drive, ROOT_PATH, "root");
        }

        // .lock is created as a folder because it's easier
        // to create an empty folder than a file
        LOGGER.info("Applying LOCK");
        GoogleDriveUtil.createFolder(drive, ".lock", ROOT_ID);

        copyFolder(file, ROOT_ID, key);

        LOGGER.info("Releasing LOCK");
        String lockFileId = GoogleDriveUtil.getFileID(drive, ".lock", ROOT_ID);
        GoogleDriveUtil.deleteFile(drive, lockFileId);
        LOGGER.info("Directory copied successfully");
    }

    private void copyFolder (java.io.File dir, String folder_id, String key) {
        Drive drive = getDrive();

        LOGGER.info("Copying Folder: " + dir.getName());
        java.io.File[] files = dir.listFiles();
        ArrayList<FileInfo> fileList = new ArrayList<>();

        if (files != null)
        for (java.io.File file : files) {
            if (file.isDirectory()) {
                String newFolder_id =
                        GoogleDriveUtil
                        .createFolder(drive, file.getName(), folder_id);
                copyFolder(file, newFolder_id, key);

                fileList.add(new FileInfo(file.getName(), newFolder_id, true));
            } else {
                LOGGER.info("Encrypting file: " + file.getAbsolutePath());
                String file_id =
                        GoogleDriveUtil
                            .createEncryptedFile(drive, file,  folder_id, key);

                fileList.add(new FileInfo(file.getName(), file_id, false));
            }
        }

        FileInfoCache.getInstance().set(folder_id, fileList);
    }

    @Override
    public void reset() {
        Drive drive = getDrive();

        LOGGER.info("Executing Factory Reset");
        GoogleDriveUtil.deleteFile(drive, ROOT_ID);
    }
}
