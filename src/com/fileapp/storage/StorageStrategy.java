package com.fileapp.storage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public interface StorageStrategy {

    /**
     * If the client has not encrypted any files
     *
     * @return isInitialized?
     */
    boolean isInitialized ();

    /**
     * Checks if all the files are encrypted and
     * copied to the application destination
     *
     * @return isLoaded?
     */
    boolean isLoaded ();

    ArrayList<FileInfo> getFileList (String ID);
    InputStream getInputStream (String ID, String key) throws FileNotFoundException;

    /**
     * Used to encrypt files from the directory path
     *
     * @param directory Path to the directory to encrypt
     * @param key Secret key to encrypt the files with
     */
    void executeCopy (String directory, String key);

    void factoryReset();
}