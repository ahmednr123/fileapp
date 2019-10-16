package com.fileapp.servlet;

import com.fileapp.utils.CryptoException;
import com.fileapp.utils.CryptoUtils;
import com.fileapp.utils.JSONReply;
import com.fileapp.utils.XCrypto;
import org.json.JSONObject;
import org.json.xjson.XJSONObject;
import com.fileapp.ResponseError;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebServlet(urlPatterns = "/initialize")
public class Initialize extends HttpServlet {

    @Override
    protected void
    doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().println("404");
    }

    @Override
    protected void
    doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String path = request.getParameter("path");
        String key = request.getParameter("key");

        System.out.println("POST /initialize path=" + path + ", key=" + key);

        PrintWriter out = response.getWriter();

        if (path == null || key == null) {
            out.print(
                    JSONReply.error( ResponseError.PARAMETERS_NOT_FOUND.name() )
            );
            out.close();
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            out.print(
                    JSONReply.error( ResponseError.PATH_DOESNT_EXIST.name() )
            );
            out.close();
            return;
        } else if (!file.isDirectory()) {
            out.print(
                    JSONReply.error( ResponseError.PATH_NOT_DIRECTORY.name() )
            );
            out.close();
            return;
        }

        String final_key = CryptoUtils.applyPadding(key);
        request.getSession().setAttribute("key", final_key);

        ExecutorService executor = (ExecutorService) getServletContext().getAttribute("executor");
        String root_path = (String) getServletContext().getAttribute("root_path");
        executor.execute(new Thread (() -> {
            ExecuteCopy(path, root_path, final_key);
        }));

        out.print(
                JSONReply.ok("ok")
        );
        out.close();
    }

    static void ExecuteCopy (String directory, String target, String key) {
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

    //C:\Users\Administrator\Pictures

    static void CopyFolder (File dir, String to, String key) {
        File[] files = dir.listFiles();
        for (File file : files) {
            String path = to + "/" + file.getName();
            if (file.isDirectory()) {
                (new File(path)).mkdir();
                CopyFolder(file, path, key);
            } else {
                //try {
                    XCrypto.encrypt(key, file, new File(path));
                    //CryptoUtils.encrypt(key, file, new File(path));
                //} catch (CryptoException e) {
                 //   e.printStackTrace();
                //}
            }
        }
    }

}