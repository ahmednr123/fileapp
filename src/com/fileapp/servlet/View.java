package com.fileapp.servlet;

import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ResponseError;
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
 * Get image data of the file path
 */
@WebServlet(urlPatterns = "/view")
public class View extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(View.class.getName());

    /**
     * Request Parameters:
     *      [path] Path/ID of the file
     *
     * Get [key] from session
     *
     * Response:
     *      ServletChecker Error || "application/octet-stream" of the image file
     */
    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("GET /view [HIT]");
        ServletChecker servletChecker = new ServletChecker(response);

        String path = request.getParameter("path");
        LOGGER.info("\nPath = " + path);

        servletChecker
                .areParametersValid(path);
        String key =
                servletChecker
                .getKey(request.getSession(false));

        if ( servletChecker.doesPass() ) {
            StorageStrategy storageStrategy =
                    (StorageStrategy) getServletContext().getAttribute("StorageStrategy");

            if (!storageStrategy.checkFileSize(path, 5000000)) {
                servletChecker.writeErrorOut( ResponseError.FILE_TOO_BIG.name() );
                return;
            }

            InputStream inStream = storageStrategy.getInputStream(path, key);

            response.setContentType("application/octet-stream");

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
