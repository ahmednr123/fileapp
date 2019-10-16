package com.fileapp.servlet;

import com.fileapp.utils.JSONReply;
import com.fileapp.utils.ServletHandler;
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
        ServletHandler servletHandler = new ServletHandler(response);

        String path = request.getParameter("path");
        String key = request.getParameter("key");

        System.out.println("POST /initialize path=" + path + ", key=" + key);

        servletHandler.areParametersValid(path, key);
        servletHandler.mustBeDirectory(new File(path));

        if ( servletHandler.doesPass() ) {
            String final_key = Crypto.applyPadding(key);
            request.getSession().setAttribute("key", final_key);

            ExecutorService executor = (ExecutorService) getServletContext().getAttribute("executor");
            String root_path = (String) getServletContext().getAttribute("root_path");
            executor.execute(new Thread (() -> {
                ExecuteCopy(path, root_path, final_key);
            }));

            PrintWriter out = response.getWriter();
            out.print(
                    JSONReply.ok("ok")
            );
            out.close();
        }
    }

    private static void ExecuteCopy (String directory, String target, String key) {
        try {
            FileWriter fw = new FileWriter(target + "/.lock");
            fw.write(target);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File rootDirectory = new File(directory);
        CopyFolder(rootDirectory, target, key);

        File lock_file = new File (target + "/.lock");
        if (lock_file.delete()) {
            System.out.println("Directory copied successfully");
        } else {
            System.out.println("Error copying directory");
        }
    }

    static void CopyFolder (File dir, String to, String key) {
        File[] files = dir.listFiles();
        for (File file : files) {
            String path = to + "/" + file.getName();
            if (file.isDirectory()) {
                (new File(path)).mkdir();
                CopyFolder(file, path, key);
            } else {
                Crypto.encrypt(key, file, new File(path));
            }
        }
    }

}