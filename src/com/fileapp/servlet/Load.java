package com.fileapp.servlet;

import com.fileapp.storage.FileInfo;
import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.ServletChecker;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Get the FileInfoList of a directory
 */
@WebServlet(urlPatterns = "/load")
public class Load extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(Load.class.getName());

    /**
     * Request Parameters:
     *      [path] Path of the directory to encrypt
     *
     * if [path] is null it sends the ROOT directory fileInfoList
     * Get [key] from session
     *
     * Response:
     *      ServletChecker Error || JSONArray of FileInfo
     */
    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("GET /load [HIT]");
        ServletChecker servletChecker = new ServletChecker(response);

        String path = request.getParameter("path");
        if (path == null) {
            path = "";
            LOGGER.info("\nPath = ROOT");
        } else {
            LOGGER.info("\nPath = " + path);
        }

        servletChecker
                .checkIfApplicationInitialized(getServletContext());
        servletChecker
                .getKey(request.getSession(false));
        servletChecker
                .checkIfApplicationLoaded(getServletContext());

        if (servletChecker.doesPass()) {
            StorageStrategy storageStrategy =
                    (StorageStrategy) getServletContext().getAttribute("StorageStrategy");
            ArrayList<FileInfo> fileInfoList = storageStrategy.getFileList(path);

            LOGGER.info("Sending FileInfoList to client");
            PrintWriter out = response.getWriter();
            out.print(FileInfo.arrayListToString(fileInfoList));
            out.close();
        }
    }

    /**
     * Request Parameters:
     *      [key] Secret Key to decrypt files
     *
     * Add [key] to session if already not present
     *
     * Response:
     *      Error || OK
     */
    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("POST /load [HIT]");

        PrintWriter out = response.getWriter();
        String key = request.getParameter("key");

        if (key != null) {
            request.getSession().setAttribute("key", key);
            out.print("ok");
        } else {
            out.print("err");
        }
    }
}