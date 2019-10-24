package com.fileapp.servlet;

import com.fileapp.storage.FileInfo;
import com.fileapp.storage.StorageStrategy;
import com.fileapp.utils.ServletCheck;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(urlPatterns = "/load")
public class Load extends HttpServlet {

    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        ServletCheck servletCheck = new ServletCheck(response);

        String path = request.getParameter("path");
        String root = (String) getServletContext().getAttribute("root_path");
        if (path == null) path = "";

        System.out.println("GET /load path=" + path);

        servletCheck.isApplicationInitialized(getServletContext());
        servletCheck.getKey(request.getSession(false));
        servletCheck.isApplicationLoaded(getServletContext());

        if (servletCheck.doesPass()) {
            StorageStrategy storageStrategy = (StorageStrategy) getServletContext().getAttribute("StorageStrategy");
            ArrayList<FileInfo> fileInfoList = storageStrategy.getFileList(path);

            PrintWriter out = response.getWriter();
            out.print(FileInfo.arrayListToString(fileInfoList));
            out.close();
        }
    }

    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        String key = request.getParameter("key");

        System.out.println("POST /load key=" + key);

        if (key != null) {
            request.getSession().setAttribute("key", key);
            out.print("ok");
        } else {
            out.print("err");
        }
    }
}