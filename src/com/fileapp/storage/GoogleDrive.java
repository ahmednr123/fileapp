package com.fileapp.storage;

import com.fileapp.utils.Crypto;
import com.fileapp.utils.GoogleDriveUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.util.Collections;
import java.util.List;

public class GoogleDrive implements StorageStrategy {
    private static final String ROOT = "ENC_ROOT";

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static Drive drive = null;
    private static boolean LOCK = false;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\inc-611\\Documents\\Learning\\credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public
    GoogleDrive() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isLoaded () {
        return !LOCK;
    }

    public boolean isInitialized () {
        try {
            return GoogleDriveUtil.doesFileExist(drive, ROOT, "root");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONArray getFileList (String ID) {
        JSONArray content = new JSONArray();

        try {
            boolean isFolder = GoogleDriveUtil.isFolder(drive, ID);
            if (!ID.equals("") && !isFolder) {
                return null;
            }

            if (ID.equals("")) {
                ID = GoogleDriveUtil.getFileID(drive, ROOT, "root");
            }

            FileList result = drive.files().list()
                    .setQ("parents in '"+ ID +"'")
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .execute();

            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File sub_file : files) {
                    JSONObject json = new JSONObject();
                    json.put("name", sub_file.getName());
                    json.put("path", sub_file.getId());
                    json.put("isDirectory", (sub_file.getMimeType().equals("application/vnd.google-apps.folder")));

                    System.out.printf("%s (%s) - %s\n", sub_file.getName(), sub_file.getId(), sub_file.getMimeType());

                    content.put(json);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    public InputStream getInputStream (String ID, String key) throws FileNotFoundException {
        try {
            InputStream driveInputStream = drive.files().get(ID).executeMediaAsInputStream();

            return Crypto.getNewInputStream(driveInputStream, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void executeCopy (String directory, String key) {
        java.io.File file = new java.io.File(directory);
        String root_id = "";

        try {
            if (!GoogleDriveUtil
                .doesFileExist(drive, ROOT, "root"))
            {
                root_id =
                        GoogleDriveUtil
                        .createFolder(drive, ROOT, "root");
            }

            LOCK = true;

            copyFolder(file, root_id, key);

            LOCK = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFolder (java.io.File dir, String folder_id, String key) {
        java.io.File[] files = dir.listFiles();

        if (files != null)
        for (java.io.File file : files) {
            if (file.isDirectory()) {
                String newFolder_id =
                        GoogleDriveUtil
                        .createFolder(drive, file.getName(), folder_id);
                copyFolder(file, newFolder_id, key);
            } else {
                GoogleDriveUtil
                .createEncryptedFile(drive, file,  folder_id, key);
            }
        }
    }
}
