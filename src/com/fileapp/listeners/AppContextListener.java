package com.fileapp.listeners;

import com.fileapp.storage.GoogleDrive;
import com.fileapp.storage.LocalDrive;
import com.fileapp.storage.StorageStrategy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    /**
     * Initializing StorageStrategy and ExecutorServer
     * to be used within the ServletContext
     */
    @Override
    public void
    contextInitialized(ServletContextEvent servletContextEvent)
    {
        ServletContext ctx = servletContextEvent.getServletContext();
        LOGGER.info("ServletContext Initialized");

        StorageStrategy storageStrategy = new GoogleDrive();
        ctx.setAttribute("StorageStrategy", storageStrategy);
        LOGGER.info("Initialized StorageStrategy and added to ServletContext");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        ctx.setAttribute("executor", executor);
        LOGGER.info("Initialized ExecutorService and added to ServletContext");
    }

    /**
     * Shutdown ExecutorService
     */
    @Override
    public void
    contextDestroyed(ServletContextEvent servletContextEvent)
    {
        ExecutorService executor =
                (ExecutorService)
                        servletContextEvent.getServletContext().getAttribute("executor");

        executor.shutdown();
        LOGGER.info("ExecutorService has been shutdown");

        LOGGER.info("ServletContext Destroyed");
    }

}
