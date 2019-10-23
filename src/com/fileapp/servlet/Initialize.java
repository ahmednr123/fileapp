package com.fileapp.servlet;

import com.fileapp.storage.GoogleDrive;
import com.fileapp.storage.LocalDrive;
import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ServletCheck;
import com.fileapp.utils.Crypto;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

@WebServlet(urlPatterns = "/initialize")
public class Initialize extends HttpServlet {

    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().println("404");
        response.getWriter().close();
    }

    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        ServletCheck servletCheck = new ServletCheck(response);

        String path = request.getParameter("path");
        String key = request.getParameter("key");

        System.out.println("POST /initialize path=" + path + ", key=" + key);

        servletCheck.areParametersValid(path, key);
        servletCheck.mustBeDirectory(new File(path));

        if ( servletCheck.doesPass() ) {
            StorageStrategy storageStrategy = (StorageStrategy) getServletContext().getAttribute("StorageStrategy");
            request.getSession().setAttribute("key", key);

            ExecutorService executor = (ExecutorService) getServletContext().getAttribute("executor");
            String root_path = (String) getServletContext().getAttribute("root_path");
            executor.execute(new Thread (() -> {
                storageStrategy.executeCopy(path, key);
            }));

            PrintWriter out = response.getWriter();
            out.print(
                    JSONReply.ok("ok")
            );
            out.close();
        }
    }

}