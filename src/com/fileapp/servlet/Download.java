package com.fileapp.servlet;

import com.fileapp.utils.ServletHandler;
import com.fileapp.utils.Crypto;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet(urlPatterns = "/download")
public class Download extends HttpServlet {

    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        ServletHandler servletHandler = new ServletHandler(response);

        String path = request.getParameter("path");
        String root = (String) getServletContext().getAttribute("root_path");

        System.out.println("GET /download path=" + path);

        servletHandler.areParametersValid(path);
        String key = servletHandler.getKey(request.getSession());

        File file = new File (root + path);
        servletHandler.mustBeFile(file);

        if ( servletHandler.doesPass() ) {
            System.out.println("PATH: " + root+path);
            System.out.println("FILE NAME: " + file.getName());

            InputStream inStream = Crypto.getInputStream(file, key);
            ServletContext context = getServletContext();

            String mimeType = context.getMimeType(root + path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            System.out.println("MIME type: " + mimeType);

            response.setContentType(mimeType);

            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
            response.setHeader(headerKey, headerValue);

            OutputStream outStream = response.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead = -1;

            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inStream.close();
            outStream.close();
        }
    }

    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().println("404");
    }

}
