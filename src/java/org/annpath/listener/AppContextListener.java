package org.annpath.listener;

import org.annpath.dao.ConnectionPool;
import org.annpath.service.UrgencyEngine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("AnnPath Platform initialized. Starting background engines...");

        try {
            javax.servlet.SessionCookieConfig scc = sce.getServletContext().getSessionCookieConfig();
            if (scc != null) {
                scc.setHttpOnly(true);
                scc.setSecure(org.annpath.config.AppConfig.isCookieSecure());
                LOGGER.info("SessionCookieConfig applied: HttpOnly=true, Secure=" + org.annpath.config.AppConfig.isCookieSecure());
            }
        } catch (Exception e) {
            LOGGER.warning("Could not set SessionCookieConfig: " + e.getMessage());
        }

        scheduler = Executors.newScheduledThreadPool(2);
        UrgencyEngine urgencyEngine = new UrgencyEngine();

        // Run food urgency & expiry scanner every 60 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                urgencyEngine.runUrgencyScan();
            } catch (Throwable t) {
                LOGGER.log(java.util.logging.Level.WARNING, "Exception in background urgency scheduler: " + t.getMessage(), t);
            }
        }, 10, 60, TimeUnit.SECONDS);

        LOGGER.info("AnnPath background scheduler registered successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("AnnPath Platform shutting down...");
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        ConnectionPool.getInstance().shutdown();
        LOGGER.info("AnnPath Platform shutdown complete.");
    }
}
