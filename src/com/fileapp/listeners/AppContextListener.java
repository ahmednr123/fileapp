package com.fileapp.listeners;

import com.fileapp.storage.GoogleDrive;
import com.fileapp.storage.LocalDrive;
import com.fileapp.storage.StorageStrategy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void
    contextInitialized(ServletContextEvent servletContextEvent)
    {
        ServletContext ctx = servletContextEvent.getServletContext();

        StorageStrategy storageStrategy = new LocalDrive();
        ctx.setAttribute("StorageStrategy", storageStrategy);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        ctx.setAttribute("executor", executor);
    }

    @Override
    public void
    contextDestroyed(ServletContextEvent servletContextEvent)
    {
        ExecutorService executor =
                (ExecutorService)
                        servletContextEvent.getServletContext().getAttribute("executor");

        executor.shutdown();
    }

}
