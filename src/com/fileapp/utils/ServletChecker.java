package com.fileapp.utils;

import com.fileapp.storage.StorageStrategy;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * To do common checks within a servlet
 *
 * If a check fails the ServletCheck class sends
 * an error message to the client
 *
 * If a single check is failed, doesPass() returns
 * false
 */
public class ServletChecker {
    private static Logger LOGGER = Logger.getLogger(ServletChecker.class.getName());

    private boolean passed = true;
    private HttpServletResponse response = null;

    public ServletChecker(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Checks if the parameters contain any value or not
     *
     * @param params
     * @return
     */
    public void areParametersValid (String... params) {
        LOGGER.info("Checking parameters");
        for (String param : params) {
            if (param == null || param.isEmpty()) {
                writeErrorOut (JSONReply.error(ResponseError.PARAMETERS_NOT_FOUND.name()));
                passed = false;
                LOGGER.info("Some Parameters missing");
                return;
            }
        }
        LOGGER.info("All Parameter Found");
    }

    /**
     * Checks if file is a file and not a directory
     *
     * @param file
     */
    public void mustBeFile (File file) {
        LOGGER.info("Checking if \"" + file.getName() + "\" is a file");
        if (!file.exists() || file.isDirectory()) {
            writeErrorOut (JSONReply.error(ResponseError.INVALID_PATH.name()));
            passed = false;
            LOGGER.info("\"" + file.getName() + "\" IS NOT a file");
            return;
        }
        LOGGER.info("\"" + file.getName() + "\" IS a file");
    }

    /**
     * Checks if the file is a directory
     *
     * @param file
     */
    public void mustBeDirectory (File file) {
        LOGGER.info("Checking if \"" + file.getName() + "\" is a directory");
        if (!file.exists() || !file.isDirectory()) {
            writeErrorOut (JSONReply.error(ResponseError.INVALID_PATH.name()));
            passed = false;
            LOGGER.info("\"" + file.getName() + "\" IS NOT a directory");
            return;
        }
        LOGGER.info("\"" + file.getName() + "\" IS a directory");
    }

    /**
     * Checks if the application is loaded
     *
     * @param ctx Servlet Context
     */
    public void checkIfApplicationLoaded (ServletContext ctx) {
        LOGGER.info("Checking if application has loaded");
        StorageStrategy storageStrategy =
                (StorageStrategy) ctx.getAttribute("StorageStrategy");

        if (!storageStrategy.isLoaded()) {
            writeErrorOut (JSONReply.error(ResponseError.DIRECTORY_NOT_LOADED.name()));
            passed = false;
            LOGGER.info("Application HAS NOT loaded");
            return;
        }
        LOGGER.info("Application HAS loaded");
    }

    /**
     * Checks if a session key exists and returns it
     *
     * @param session HttpSession
     * @return Secret Key
     */
    public String getKey (HttpSession session) {
        String key = null;
        LOGGER.info("Checking for a session key");

        try {
            key = (String) session.getAttribute("key");
            LOGGER.info("Session key FOUND");
        } catch (Exception e) {
            writeErrorOut (JSONReply.error(ResponseError.NO_SESSION.name()));
            passed = false;
            LOGGER.info("Session key NOT FOUND");
            return null;
        }

        return key;
    }

    /**
     * Checks if the application is initialized.
     * ie. if the user has encrypted files
     *
     * @param ctx Servlet Context
     */
    public void checkIfApplicationInitialized (ServletContext ctx) {
        LOGGER.info("Checking if application has been initialized");
        StorageStrategy storageStrategy =
                (StorageStrategy) ctx.getAttribute("StorageStrategy");

        if (!storageStrategy.isInitialized()) {
            writeErrorOut (JSONReply.error(ResponseError.NOT_INITIALIZED.name()));
            passed = false;
            LOGGER.info("Application HAS NOT been initialized");
            return;
        }
        LOGGER.info("Application HAS been initialized");
    }

    /**
     * Returns if all checks were passed or not
     *
     * @return doesPass?
     */
    public boolean doesPass () {
        return passed;
    }

    /**
     * Respond to the request
     *
     * @param data Data to write to client
     */
    private void writeErrorOut (String data) {
        try {
            PrintWriter out = response.getWriter();
            out.print(data);
            out.close();
            LOGGER.info("Error message sent to client");
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}
