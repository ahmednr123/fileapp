package com.fileapp.storage;

import java.util.ArrayList;

/**
 * Stores Information of the file.
 * This is to force the similar information format
 * within all Storage Strategies
 */
public class FileInfo {

    private String name;
    private String path;
    private boolean isDirectory;

    public
    FileInfo (String name, String path, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
    }

    public String
    toJSONString ()
    {
        return "{\"name\":\"" + this.name + "\"," +
                "\"path\":\"" + this.path + "\"," +
                "\"isDirectory\":" + this.isDirectory + "}";
    }

    public static
    String arrayListToString (ArrayList<FileInfo> fileList)
    {
        String arrString = "[ ";

        for (FileInfo fileInfo : fileList) {
            arrString += fileInfo.toJSONString() + ",";
        }

        arrString = arrString.substring(0, arrString.length()-1);

        return arrString + "]";
    }

}
