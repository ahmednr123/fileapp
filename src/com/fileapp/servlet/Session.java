package com.fileapp.servlet;

import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ServletChecker;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Check if user session exists
 */
@WebServlet(urlPatterns = "/session")
public class Session extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(Session.class.getName());

    /**
     * Response:
     *      ServletChecker Error || OK message
     */
    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        LOGGER.info("GET /session [HIT]");
        ServletChecker servletChecker = new ServletChecker(response);

        if (servletChecker.getKey(request.getSession(false)) != null) {
            PrintWriter out = response.getWriter();
            out.print(JSONReply.ok("ACTIVE"));
            out.close();
        }
    }
}