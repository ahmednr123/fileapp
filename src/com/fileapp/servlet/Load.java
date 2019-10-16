package com.fileapp.servlet;

import com.fileapp.utils.ServletHandler;
import com.fileapp.utils.Crypto;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/load")
public class Load extends HttpServlet {

    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        ServletHandler servletHandler = new ServletHandler(response);

        String path = request.getParameter("path");
        String root = (String) getServletContext().getAttribute("root_path");
        if (path == null) path = "";

        System.out.println("GET /load path=" + path);

        servletHandler.isApplicationInitialized(getServletContext());
        servletHandler.getKey(request.getSession());
        servletHandler.isApplicationLoaded(getServletContext());

        File dir = new File (root + path);
        servletHandler.mustBeDirectory(dir);

        if (servletHandler.doesPass()) {
            JSONArray content = new JSONArray();
            File[] files = dir.listFiles();
            for (File file : files) {
                JSONObject json = new JSONObject();
                json.put("name", file.getName());
                json.put("isDirectory", file.isDirectory());

                content.put(json);
            }

            PrintWriter out = response.getWriter();
            out.print(content.toString());
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
            key = Crypto.applyPadding(key);
            request.getSession().setAttribute("key", key);
            out.print("ok");
        } else {
            out.print("err");
        }
    }
}
