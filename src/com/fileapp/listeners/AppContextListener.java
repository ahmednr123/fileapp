package com.fileapp.listeners;

import com.fileapp.cache.FileInfoCache;
import com.fileapp.storage.FileInfo;
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

        StorageStrategy storageStrategy = new LocalDrive();
        ctx.setAttribute("StorageStrategy", storageStrategy);
        LOGGER.info("Initialized StorageStrategy and added to ServletContext");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        ctx.setAttribute("executor", executor);
        LOGGER.info("Initialized ExecutorService and added to ServletContext");

        FileInfoCache.getInstance().initialize(storageStrategy.getName());
        LOGGER.info("Initialized FileInfoCache");
    }

    /**
     * Shutdown ExecutorService
     */
    @Override
    public void
    contextDestroyed(ServletContextEvent servletContextEvent)
    {
        ServletContext ctx = servletContextEvent.getServletContext();

        ExecutorService executor = (ExecutorService) ctx.getAttribute("executor");
        executor.shutdown();
        LOGGER.info("ExecutorService has been shutdown");

        FileInfoCache.getInstance().destroy();
        LOGGER.info("FileInfoCache has been destroyed");

        LOGGER.info("ServletContext Destroyed");
    }

}
