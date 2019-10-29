package com.fileapp.googledrive;

import com.fileapp.utils.Crypto;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.GeneratedIds;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Methods to use the Google Drive API
 */
public class GoogleDriveUtil {
    private static Logger LOGGER = Logger.getLogger(GoogleDriveUtil.class.getName());

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
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

    /**
     * Create a folder within a parent directory
     *
     * @param drive Authenticated drive object
     * @param folder_name Name of the folder
     * @param parent_id ID of the Parent Directory
     * @return Folder ID
     */
    public static String createFolder (Drive drive, String folder_name, String parent_id) {
        File new_folder = new File();
        new_folder.setParents(Collections.singletonList(parent_id));
        new_folder.setName(folder_name);
        new_folder.setMimeType("application/vnd.google-apps.folder");

        LOGGER.info("Creating Folder: " + folder_name + " in " + parent_id);

        try {
            File folder = drive.files().create(new_folder)
                    .setFields("id")
                    .execute();
            return folder.getId();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    /**
     * Create encrypted file in a parent directory
     * The method uses a custom EncryptedFileContent Class
     * to pass in encrypted file InputStream to the API
     *
     * @param drive Authenticated drive object
     * @param file File to be created
     * @param parent_id ID of the Parent Directory
     * @param key Secret Key to encrypt the file with
     * @return File ID
     */
    public static String createEncryptedFile (Drive drive, java.io.File file, String parent_id, String key) {
        EncryptedFileContent fileContent = new EncryptedFileContent(null, file, key);
        File fileMeta = new File();
        fileMeta.setParents(Collections.singletonList(parent_id));
        fileMeta.setName(file.getName());

        LOGGER.info("Creating EncryptedFile: " + file.getName() + " in " + parent_id);

        try {
            File uploadedFile = drive.files().create(fileMeta, fileContent).execute();
            return uploadedFile.getId();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    /**
     * Create a file in a parent directory
     *
     * @param drive Authenticated drive object
     * @param file File to be created
     * @param parent_id ID of the Parent Directory
     * @return File ID
     */
    public static String createFile (Drive drive, java.io.File file, String parent_id) {
        FileContent fileContent = new FileContent(null, file);
        File fileMeta = new File();
        fileMeta.setParents(Collections.singletonList(parent_id));
        fileMeta.setName(file.getName());

        LOGGER.info("Creating File: " + file.getName() + " in " + parent_id);

        try {
            File uploadedFile = drive.files().create(fileMeta, fileContent).execute();
            return uploadedFile.getId();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    /**
     * Checks if a file exists within a parent directory
     * It uses a filename to check
     *
     * @param drive Authenticated drive object
     * @param filename Name of the file/folder
     * @param parent_id ID of the Parent Directory
     * @return doesFileExist?
     */
    public static boolean doesFileExist (Drive drive, String filename, String parent_id) {
        boolean doesFileExist = (getFileID(drive, filename, parent_id) != null);
        LOGGER.info("\"" + filename + "\" exists? = " + doesFileExist);

        return doesFileExist;
    }

    public static long getFileSize (Drive drive, String file_id) {
        try {
            File file = drive.files().get(file_id).setFields("size").execute();
            LOGGER.info("File: " + file_id + " (size = " + file.getSize() + ")");
            return file.getSize();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return -1;
    }

    /**
     * Get the ID of file within a directory using a filename
     *
     * @param drive Authenticated drive object
     * @param filename Name of the file/folder
     * @param parent_id ID of the Parent Directory
     * @return File ID
     */
    public static String getFileID (Drive drive, String filename, String parent_id) {
        LOGGER.info("Getting FileID of \"" + filename + "\" in " + parent_id);
        try {
            FileList result = drive.files().list()
                    .setQ("parents in '" + parent_id + "'")
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                for (File file : files) {
                    if (filename.equals(file.getName())){
                        return file.getId();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    /**
     * Checks if a file ID is of type
     * "application/vnd.google-apps.folder"
     *
     * @param drive Authenticated drive object
     * @param folder_id ID of the Parent Directory
     * @return isFolder?
     */
    public static boolean isFolder (Drive drive, String folder_id) {
        if (folder_id.equals("")){
            LOGGER.info(folder_id + " isFolder? = " + false);
            return false;
        }

        try {
            File file = drive.files().get(folder_id).execute();
            if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                LOGGER.info(folder_id + " isFolder? = " + true);
                return true;
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        LOGGER.info(folder_id + " isFolder? = " + false);
        return false;
    }

    /**
     * Get files info from a directory using folder ID
     *
     * @param drive Authenticated drive object
     * @param folder_id ID of the Parent Directory
     * @return FileInfoList
     */
    public static List<File> getFiles (Drive drive, String folder_id) {
        try {
            LOGGER.info("Getting fileInfoList from GoogleDrive");
            FileList result = drive.files().list()
                    .setQ("parents in '"+ folder_id +"'")
                    .setFields("nextPageToken, files(id, name, size, mimeType)")
                    .execute();
            return result.getFiles();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    /**
     * Get file InputStream from GoogleDrive using file ID
     *
     * @param drive Authenticated drive object
     * @param file_id File ID
     * @return File InputStream from GoogleDrive
     */
    public static InputStream getInputStream (Drive drive, String file_id) {
        try {
            LOGGER.info("Getting file InputStream from GoogleDrive");
            return drive.files().get(file_id).executeMediaAsInputStream();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }

        return null;
    }

    /**
     * Delete a file using file ID
     *
     * @param drive Authenticated drive object
     * @param file_id File/Folder ID
     */
    public static void deleteFile (Drive drive, String file_id) {
        try {
            LOGGER.info("Deleting file: " + file_id);
            drive.files().delete(file_id).execute();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public static ArrayList<String> getFileIDs (Drive drive, int num) {
        GeneratedIds allIds = null;
        try {
            allIds = drive.files().generateIds()
                    .setSpace("drive").setCount(num).execute();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return (ArrayList<String>) allIds.getIds();
    }
}