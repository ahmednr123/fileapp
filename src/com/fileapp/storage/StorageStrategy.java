package com.fileapp.storage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class StorageStrategy {
    private static Logger LOGGER = Logger.getLogger(StorageStrategy.class.getName());

    // Set isLoaded once the encrypting of files is completed
    private static boolean isLoaded = false;

    // Set isInitialized if the client hasn't encrypted files
    private static boolean isInitialized = false;

    private String name;

    StorageStrategy (String name) {
        this.name = name;

        // Check if .lock exists which would mean the previous copy execution wasn't
        // completed. The application is factoryReset to overcome the problem
        if (this.isCorrupt()) {
            LOGGER.info("Previous executeCopy had failed");
            factoryReset();
        } else {
            boolean isInitialized = this.checkIfInitialized();
            StorageStrategy.isInitialized =
                    StorageStrategy.isLoaded = isInitialized;
        }
    }

    /**
     * Check if client has encrypted files.
     * This can usually be a time consuming task.
     */
    abstract boolean checkIfInitialized ();

    /**
     * Check if .lock exists which would mean the
     * previous copy execution wasn't completed.
     *
     * @return isCorrupt?
     */
    abstract boolean isCorrupt ();

    abstract long getFileSize (String ID);
    public abstract ArrayList<FileInfo> getFileList (String ID);
    public abstract InputStream getInputStream (String ID, String key) throws FileNotFoundException;

    /**
     * Used to encrypt files from the directory path
     *
     * @param directory Path to the directory to encrypt
     * @param key Secret key to encrypt the files with
     */
    abstract void executeEncryption (String directory, String key);
    abstract void reset ();

    // ==================================================== //

    public String getName() { return name; };
    public boolean isInitialized () { return isInitialized; }
    public boolean isLoaded () { return isLoaded; }

    public boolean checkFileSize (String ID, long maxSize) {
        return (this.getFileSize(ID) <= maxSize);
    }

    public void encrypt (String directory, String key) {
        isInitialized = true;

        this.executeEncryption (directory, key);

        isLoaded = true;
    }

    public void factoryReset() {
        isInitialized =
                isLoaded = false;
        this.reset();
    }
}