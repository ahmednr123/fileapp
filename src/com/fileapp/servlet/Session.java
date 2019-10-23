package com.fileapp.servlet;

import com.fileapp.storage.GoogleDrive;
import com.fileapp.utils.Crypto;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ServletCheck;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/session")
public class Session extends HttpServlet {
    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        InputStream gIS = GoogleDrive.download();

        if (gIS == null) {
            System.out.println("FUCK!!!!");
            return;
        }

        InputStream inStream = Crypto.getNewInputStream(gIS, "ahmed");
        String mimeType = "application/octet-stream";

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"code.txt\"");
        response.setHeader(headerKey, headerValue);
        response.setContentType(mimeType);

        //byte[] buffer = new byte[8192];
        /*int bytesRead = -1;
        System.out.println("ALMOST THERE!");

        while ((bytesRead = inStream.read()) != -1) {
            outStream.write(bytesRead);
        }*/

        try(OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[8192];

            int numBytesRead;
            while ((numBytesRead = inStream.read(buffer)) > 0) {
                out.write(buffer, 0, numBytesRead);
            }
        }

        inStream.close();
        /*ServletCheck servletCheck = new ServletCheck(response);

        if (servletCheck.getKey(request.getSession(false)) != null) {
            PrintWriter out = response.getWriter();
            out.print(JSONReply.ok("ACTIVE"));
            out.close();
        }*/
    }
}