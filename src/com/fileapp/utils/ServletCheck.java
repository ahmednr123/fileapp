package com.fileapp.utils;

import com.fileapp.storage.GoogleDrive;
import com.fileapp.storage.LocalDrive;
import com.fileapp.storage.StorageStrategy;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * To do common checks within a servlet
 *
 * If a check fails the ServletCheck class sends
 * an error message to the client
 *
 * If a single check is failed, doesPass() returns
 * false
 */
public class ServletCheck {
    private boolean passed = true;
    private HttpServletResponse response = null;

    public ServletCheck(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Checks if the parameters contain any value or not
     *
     * @param params
     * @return
     */
    public void areParametersValid (String... params) {
        for (String param : params) {
            if (param == null || param.isEmpty()) {
                writeOut (JSONReply.error(ResponseError.PARAMETERS_NOT_FOUND.name()));
                passed = false;
            }
        }
    }

    /**
     * Checks if file is a file and not a directory
     *
     * @param file
     * @return
     */
    public void mustBeFile (File file) {
        if (!file.exists() || file.isDirectory()) {
            writeOut (JSONReply.error(ResponseError.INVALID_PATH.name()));
            passed = false;
        }
    }

    /**
     * Checks if the file is a directory
     *
     * @param file
     * @return
     */
    public void mustBeDirectory (File file) {
        if (!file.exists() || !file.isDirectory()) {
            writeOut (JSONReply.error(ResponseError.INVALID_PATH.name()));
            passed = false;
        }
    }

    /**
     * Checks if the application is loaded by looking for ".lock"
     * file within the root folder
     *
     * @param ctx
     * @return
     */
    public void isApplicationLoaded (ServletContext ctx) {
        StorageStrategy storageStrategy = (StorageStrategy) ctx.getAttribute("StorageStrategy");
        if (!storageStrategy.isLoaded()) {
            System.out.println("APPLICATION IS NOT LOADED");
            writeOut (JSONReply.error(ResponseError.DIRECTORY_NOT_LOADED.name()));
            passed = false;
        }
    }

    /**
     * Checks if a session key exists and returns it
     *
     * @param session
     * @return
     */
    public String getKey (HttpSession session) {
        String key = null;

        try {
            key = (String) session.getAttribute("key");
        } catch (Exception e) {
            writeOut (JSONReply.error(ResponseError.NO_SESSION.name()));
            passed = false;
            return null;
        }

        return key;
    }

    /**
     * Checks if the application is initialized.
     * ie. if the user has encrypted files
     *
     * @param ctx
     * @return
     */
    public void isApplicationInitialized (ServletContext ctx) {
        StorageStrategy storageStrategy = (StorageStrategy) ctx.getAttribute("StorageStrategy");
        System.out.println("APPLICATION IS NOT INITIALIZED");
        if (!storageStrategy.isInitialized()) {
            writeOut (JSONReply.error(ResponseError.NOT_INITIALIZED.name()));
            passed = false;
        }
    }

    /**
     * Returns if all checks were passed or not
     *
     * @return
     */
    public boolean doesPass () {
        return passed;
    }

    /**
     * Respond to the request
     *
     * @param data
     */
    private void writeOut (String data) {
        try {
            PrintWriter out = response.getWriter();
            out.print(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
