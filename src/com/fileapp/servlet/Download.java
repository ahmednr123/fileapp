package com.fileapp.servlet;

import com.fileapp.ResponseError;
import com.fileapp.utils.CryptoUtils;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.XCrypto;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;

@WebServlet(urlPatterns = "/download")
public class Download extends HttpServlet {

    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String path = request.getParameter("path");
        String root = (String) getServletContext().getAttribute("root_path");

        System.out.println("GET /download path=" + path);

        if (path == null || path.equals("")) {
            PrintWriter out = response.getWriter();
            out.print(
                    JSONReply.error(ResponseError.PARAMETERS_NOT_FOUND.name())
            );
            out.close();
            return;
        }

        String key = null;
        HttpSession session = request.getSession(false);
        try {
            key = (String) session.getAttribute("key");
            System.out.println("KEY: " + key);
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            out.print(
                    JSONReply.error(ResponseError.NO_SESSION.name())
            );
            out.close();
            return;
        }

        File file = new File (root + path);
        if (!file.exists() || file.isDirectory()) {
            PrintWriter out = response.getWriter();
            out.print(
                    JSONReply.error("INVALID_PATH")
            );
            out.close();
            return;
        }

        System.out.println("PATH: " + root+path);
        System.out.println("FILE NAME: " + file.getName());

        InputStream inStream = XCrypto.getInputStream(file, key);//new FileInputStream(downloadFile);

        // if you want to use a relative path to context root:
        String relativePath = getServletContext().getRealPath("");
        System.out.println("relativePath = " + relativePath);

        // obtains ServletContext
        ServletContext context = getServletContext();

        // gets MIME type of the file
        String mimeType = context.getMimeType(root + path);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // modifies response
        response.setContentType(mimeType);
        //response.setContentLength((int) file.length());

        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
        response.setHeader(headerKey, headerValue);

        // obtains response's output stream
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead = -1;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }

    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().println("404");
    }

}
