package com.fileapp.utils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * To do common checks within a servlet
 *
 * If a check fails the ServletHandler class sends
 * an error message to the client
 *
 * If a single check is failed, doesPass() returns
 * false
 */
public class ServletHandler {
    private boolean passed = true;
    private HttpServletResponse response = null;
    private PrintWriter out = null;

    public ServletHandler (HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Checks if the parameters contain any value or not
     *
     * @param params
     * @return
     */
    public boolean areParametersValid (String... params) {
        for (String param : params) {
            if (param == null || param.isEmpty()) {
                try {
                    out = response.getWriter();
                    out.print(
                            JSONReply.error(ResponseError.PARAMETERS_NOT_FOUND.name())
                    );
                    out.close();
                    passed = false;
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    /**
     * Checks if file is a file and not a directory
     *
     * @param file
     * @return
     */
    public boolean mustBeFile (File file) {
        if (!file.exists() || file.isDirectory()) {
            try {
                out = response.getWriter();
                out.print(
                        JSONReply.error("INVALID_PATH")
                );
                out.close();
                passed = false;
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Checks if the file is a directory
     *
     * @param file
     * @return
     */
    public boolean mustBeDirectory (File file) {
        if (!file.exists() || !file.isDirectory()) {
            try {
                out = response.getWriter();
                out.print(
                        JSONReply.error(ResponseError.INVALID_PATH.name() )
                );
                out.close();
                passed = false;
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Checks if the application is loaded by looking for ".lock"
     * file within the root folder
     *
     * @param ctx
     * @return
     */
    public boolean isApplicationLoaded (ServletContext ctx) {
        String root = (String) ctx.getAttribute("root_path");
        if ((new File(root + ".lock")).exists()) {
            try {
                out = response.getWriter();
                out.print(
                        JSONReply.error(ResponseError.DIRECTORY_NOT_LOADED.name())
                );
                out.close();
                passed = false;
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
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
            try { out = response.getWriter(); }
            catch (IOException ioe) { ioe.printStackTrace(); }
            out.print(
                    JSONReply.error(ResponseError.NO_SESSION.name())
            );
            out.close();
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
    public boolean isApplicationInitialized (ServletContext ctx) {
        try {
            String root = (String) ctx.getAttribute("root_path");
            File root_dir = new File(root);
            File[] files = root_dir.listFiles();

            if (files == null || files.length == 0) {
                try {
                    out = response.getWriter();
                    out.print(
                            JSONReply.error(ResponseError.NOT_INITIALIZED.name())
                    );
                    out.close();
                    passed = false;
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Returns if all checks were passed or not
     *
     * @return
     */
    public boolean doesPass () {
        return passed;
    }
}
