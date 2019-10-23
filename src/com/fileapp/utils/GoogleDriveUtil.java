package com.fileapp.utils;

import com.fileapp.storage.EncryptedFileContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.Collections;
import java.util.List;

public class GoogleDriveUtil {
    public static String createFolder (Drive drive, String folder_name, String parent_id) {
        File new_folder = new File();
        new_folder.setParents(Collections.singletonList(parent_id));
        new_folder.setName(folder_name);
        new_folder.setMimeType("application/vnd.google-apps.folder");

        try {
            File folder = drive.files().create(new_folder)
                    .setFields("id")
                    .execute();
            return folder.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String createEncryptedFile (Drive drive, java.io.File file, String parent_id, String key) {
        EncryptedFileContent fileContent = new EncryptedFileContent(null, file, key);
        File fileMeta = new File();
        fileMeta.setParents(Collections.singletonList(parent_id));
        fileMeta.setName(file.getName());

        try {
            File uploadedFile = drive.files().create(fileMeta, fileContent).execute();
            return uploadedFile.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String createFile (Drive drive, java.io.File file, String parent_id) {
        FileContent fileContent = new FileContent(null, file);
        File fileMeta = new File();
        fileMeta.setParents(Collections.singletonList(parent_id));
        fileMeta.setName(file.getName());

        try {
            File uploadedFile = drive.files().create(fileMeta, fileContent).execute();
            return uploadedFile.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean doesFileExist (Drive drive, String filename, String parent_id) {
        return (getFileID(drive, filename, parent_id) != null);
    }

    public static String getFileID (Drive drive, String filename, String parent_id) {
        try {
            FileList result = drive.files().list()
                    .setQ("parents in '" + parent_id + "'")
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                System.out.println("Files:");
                for (File file : files) {
                    if (filename.equals(file.getName())){
                        return file.getId();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isFolder (Drive drive, String folder_id) {
        try {
            File file = drive.files().get(folder_id).execute();
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static List<File> getFiles (Drive drive, String folder_id) {
        return null;
    }
}