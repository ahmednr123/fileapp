package com.fileapp.storage;

import org.json.JSONArray;
import org.json.JSONObject;

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
    FileInfo (String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        this.name = jsonObject.getString("name");
        this.path = jsonObject.getString("path");
        this.isDirectory = jsonObject.getBoolean("isDirectory");
    }

    public
    FileInfo (JSONObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.path = jsonObject.getString("path");
        this.isDirectory = jsonObject.getBoolean("isDirectory");
    }

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

    public static
    ArrayList<FileInfo> stringToArrayList (String listString) {
        ArrayList<FileInfo> fileList = new ArrayList<>();
        JSONArray array = new JSONArray(listString);

        for (Object object : array) {
            JSONObject jsonObject = (JSONObject) object;
            fileList.add(new FileInfo(jsonObject));
        }

        return fileList;
    }
}