package com.fileapp.servlet;

import com.fileapp.utils.CryptoUtils;
import com.fileapp.utils.JSONReply;
import com.fileapp.ResponseError;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        PrintWriter out = response.getWriter();
        String path = request.getParameter("path");
        String root = (String) getServletContext().getAttribute("root_path");
        if (path == null) path = "";

        System.out.println("GET /load path=" + path);

        // HANDLE NULL POINTER
        System.out.println("Root dir: " + root);
        if ((new File(root)).listFiles().length == 0) {
            out.print(
                    JSONReply.error(ResponseError.NOT_INITIALIZED.name())
            );
            out.close();
            return;
        }

        String key = null;
        HttpSession session = request.getSession(false);
        try {
            key = (String) session.getAttribute("key");
        } catch (Exception e) {
            out.print(
                    JSONReply.error(ResponseError.NO_SESSION.name())
            );
            out.close();
            return;
        }

        if ((new File(root + ".lock")).exists()) {
            out.print(
                    JSONReply.error(ResponseError.DIRECTORY_NOT_LOADED.name())
            );
            out.close();
            return;
        }

        File dir = new File (root + path);
        if (!dir.exists()) {
            out.print(
                    JSONReply.error( ResponseError.INVALID_PATH.name() )
            );
            out.close();
            return;
        } else if (!dir.isDirectory()) {
            out.print(
                    JSONReply.error( ResponseError.PATH_NOT_DIRECTORY.name() )
            );
            out.close();
            return;
        }

        JSONArray content = new JSONArray();
        File[] files = dir.listFiles();
        for (File file : files) {
            JSONObject json = new JSONObject();
            json.put("name", file.getName());
            json.put("isDirectory", file.isDirectory());

            content.put(json);
        }

        out.print(content.toString());
        out.close();
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
            key = CryptoUtils.applyPadding(key);
            request.getSession().setAttribute("key", key);
            out.print("ok");
        } else {
            out.print("err");
        }
    }
}
