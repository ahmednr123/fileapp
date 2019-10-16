package com.fileapp.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void
    contextInitialized(ServletContextEvent servletContextEvent)
    {
        ServletContext ctx = servletContextEvent.getServletContext();

        ctx.setAttribute("root_path", "C:/Users/Administrator/Documents/fileapp_dir");

        ExecutorService executor = Executors.newFixedThreadPool(100);
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
