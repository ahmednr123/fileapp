package com.fileapp.servlet;

import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ServletCheck;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/session")
public class Session extends HttpServlet {
    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        ServletCheck servletCheck = new ServletCheck(response);

        if (servletCheck.getKey(request.getSession(false)) != null) {
            PrintWriter out = response.getWriter();
            out.print(JSONReply.ok("ACTIVE"));
            out.close();
        }
    }
}