package com.fileapp.servlet;

import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ResponseError;
import com.fileapp.utils.ServletChecker;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Deletes all encrypted files and resets application state
 */
@WebServlet(urlPatterns = "/reset")
public class FactoryReset extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(FactoryReset.class.getName());

    /**
     * Get [key] from session
     *
     * Response:
     *      ServletChecker Error || NOT_INITIALIZED
     */
    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("POST /reset [HIT]");
        ServletChecker servletChecker = new ServletChecker(response);

        servletChecker.getKey(request.getSession());

        if (servletChecker.doesPass()) {
            StorageStrategy storageStrategy =
                    (StorageStrategy) getServletContext().getAttribute("StorageStrategy");

            storageStrategy.factoryReset();
            request.getSession(false).invalidate();

            PrintWriter out = response.getWriter();
            out.print(
                JSONReply.error(
                    ResponseError.NOT_INITIALIZED.name()
                )
            );
            out.close();
        }
    }
}