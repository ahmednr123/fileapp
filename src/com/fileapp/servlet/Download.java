package com.fileapp.servlet;

import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.ServletChecker;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Logger;

/**
 * To Download file from drive
 */
@WebServlet(urlPatterns = "/download")
public class Download extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(Download.class.getName());

    /**
     * Request Parameters:
     *      [path] Path/ID of the file
     *      [filename] Name of the file
     *
     * Get [key] from session
     *
     * Response:
     *      ServletChecker Error || "application/octet-stream" of the file with file name
     */
    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("GET /download [HIT]");
        ServletChecker servletChecker = new ServletChecker(response);

        String path = request.getParameter("path");
        String filename = request.getParameter("filename");
        LOGGER.info("\nPath = " + path + "\nFilename = " + filename);

        servletChecker
                .areParametersValid(path, filename);

        String key = servletChecker.getKey(request.getSession(false));

        if ( servletChecker.doesPass() ) {
            StorageStrategy storageStrategy =
                    (StorageStrategy) getServletContext().getAttribute("StorageStrategy");

            InputStream inStream = storageStrategy.getInputStream(path, key);

            response.setContentType("application/octet-stream");

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", filename);
            response.setHeader(headerKey, headerValue);

            OutputStream outStream = response.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead = -1;

            LOGGER.info("Sending file content to client");
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inStream.close();
            outStream.close();
        }
    }
}
