package com.fileapp.servlet;

import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ServletChecker;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * To encrypt the files within a directory
 */
@WebServlet(urlPatterns = "/initialize")
public class Initialize extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(Initialize.class.getName());

    /**
     * Request Parameters:
     *      [path] Path of the directory to encrypt
     *      [key] Secret Key to encrypt the file with
     *
     * Response:
     *      ServletChecker Error || OK message
     */
    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("POST /initialize [HIT]");
        ServletChecker servletChecker = new ServletChecker(response);

        String path = request.getParameter("path");
        String key = request.getParameter("key");
        LOGGER.info("\nPath = " + path);

        servletChecker
                .areParametersValid(path, key);
        servletChecker
                .mustBeDirectory(new File(path));

        if ( servletChecker.doesPass() ) {
            StorageStrategy storageStrategy =
                    (StorageStrategy) getServletContext().getAttribute("StorageStrategy");

            request.getSession().setAttribute("key", key);

            ExecutorService executor = (ExecutorService) getServletContext().getAttribute("executor");
            executor.execute(new Thread (() -> {
                storageStrategy.encrypt(path, key);
            }));

            PrintWriter out = response.getWriter();
            out.print(
                    JSONReply.ok("ok")
            );
            out.close();
        }
    }

}