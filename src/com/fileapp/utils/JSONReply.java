package com.fileapp.utils;

import org.json.JSONObject;

public class JSONReply {

    public static String error (String msg) {
        JSONObject obj = new JSONObject();
        obj.put("reply", false);
        obj.put("error", msg);

        return obj.toString();
    }

    public static String ok (String msg) {
        JSONObject obj = new JSONObject();
        obj.put("reply", true);
        obj.put("message", msg);

        return obj.toString();
    }

}