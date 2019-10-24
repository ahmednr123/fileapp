package com.fileapp.storage;

import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public interface StorageStrategy {
    boolean isInitialized ();
    boolean isLoaded ();

    ArrayList<FileInfo> getFileList (String ID);
    InputStream getInputStream (String ID, String key) throws FileNotFoundException;
    void executeCopy (String directory, String key);
}
