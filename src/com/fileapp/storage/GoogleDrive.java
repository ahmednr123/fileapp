package com.fileapp.storage;

import com.fileapp.utils.Crypto;
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

public class GoogleDrive {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static Drive drive = null;
    private static boolean lock = false;

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

    private static String root = "enc_root";

    public static boolean isLoaded () {
        return !lock;
    }

    public static boolean isInitialized () {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            FileList result = drive.files().list()
                    .setQ("parents in 'root'")
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                System.out.println("Files:");
                for (File root_file : files) {
                    System.out.printf("FROM ROOT: %s (%s) - %s\n", root_file.getName(), root_file.getId(), root_file.getMimeType());
                    if ("ENC_ROOT".equals(root_file.getName())){
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public static JSONArray getFileList (String ID) {
        JSONArray content = new JSONArray();

        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            if (ID != null && !ID.equals("")) {
                File file = drive.files().get(ID).execute();
                if ( !file.getMimeType().equals("application/vnd.google-apps.folder") ) {
                    return content;
                }
            }

            if (ID.equals("")) {
                FileList result = drive.files().list()
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .execute();
                List<File> files = result.getFiles();
                if (files != null && !files.isEmpty()) {
                    System.out.println("Files:");
                    for (File root_file : files) {
                        if ("ENC_ROOT".equals(root_file.getName())){
                            ID = root_file.getId();
                            break;
                        }
                    }
                }
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

    public static InputStream getInputStream (String ID, String key) throws FileNotFoundException {
        try {// Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            InputStream driveInputStream = drive.files().get(ID).executeMediaAsInputStream();

            return Crypto.getNewInputStream(driveInputStream, key);
            //drive.files().get("1ZJhk3XS-nCw4hF9oV3Z0twxlhBe3G32Z").executeMediaAndDownloadTo(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void executeCopy (String directory, String key) {
        java.io.File file = new java.io.File(directory);
        String root_id = "";

        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            FileList result = drive.files().list()
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                System.out.println("Files:");
                for (File root_file : files) {
                    System.out.printf("%s (%s) - %s\n", root_file.getName(), root_file.getId(), root_file.getMimeType());
                    if ("ENC_ROOT".equals(root_file.getName())){
                        root_id = root_file.getId();
                        break;
                    }
                }
            }

            if (root_id.equals("")) {
                System.out.println("ENC_ROOT NOT FOUND");
                System.out.println("CREATING ENC_ROOT");

                File new_folder = new File();
                new_folder.setName("ENC_ROOT");
                new_folder.setMimeType("application/vnd.google-apps.folder");

                File folder = drive.files().create(new_folder)
                        .setFields("id")
                        .execute();
                root_id = folder.getId();
            }

            lock = true;

            copyFolder(file, root_id, key);

            lock = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFolder (java.io.File dir, String folder_id, String key) {
        java.io.File[] files = dir.listFiles();

        if (files != null)
        for (java.io.File file : files) {
            String path = folder_id + "/" + file.getName();
            if (file.isDirectory()) {
                File new_folder = new File();
                new_folder.setParents(Collections.singletonList(folder_id));
                new_folder.setName(file.getName());
                new_folder.setMimeType("application/vnd.google-apps.folder");

                try {
                    File folder = drive.files().create(new_folder)
                            .setFields("id")
                            .execute();
                    copyFolder(file, folder.getId(), key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                EncryptedFileContent encFile = new EncryptedFileContent(null, file, key);
                File encFileMeta = new File();
                encFileMeta.setParents(Collections.singletonList(folder_id));
                encFileMeta.setName(file.getName());

                try {
                    drive.files().create(encFileMeta, encFile).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static InputStream download () {
        try {// Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            return drive.files().get("1q-FgkfM-_EOHECXPVv8HUhLfAB0z9RG3").executeMediaAsInputStream();
            //drive.files().get("1ZJhk3XS-nCw4hF9oV3Z0twxlhBe3G32Z").executeMediaAndDownloadTo(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
