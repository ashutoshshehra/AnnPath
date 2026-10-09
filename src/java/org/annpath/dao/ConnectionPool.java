package org.annpath.dao;

import org.annpath.config.AppConfig;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * High-reliability thread-safe JDBC Connection Pool.
 * Supports JNDI DataSource lookup if present on GlassFish/Jakarta EE,
 * with automatic fallback to high-efficiency managed internal connection pool.
 */
public class ConnectionPool {

    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class.getName());
    private static ConnectionPool instance;

    private DataSource jndiDataSource;
    private final BlockingQueue<Connection> pool;
    private final int maxTotal;
    private final int minIdle;
    private int currentTotal = 0;
    private boolean isShutdown = false;

    private ConnectionPool() {
        this.minIdle = AppConfig.getDbPoolMinIdle();
        this.maxTotal = AppConfig.getDbPoolMaxTotal();
        this.pool = new ArrayBlockingQueue<>(maxTotal);

        // Attempt JNDI lookup first (standard enterprise practice)
        try {
            InitialContext ctx = new InitialContext();
            this.jndiDataSource = (DataSource) ctx.lookup("jdbc/annpath");
            LOGGER.info("Connected to JNDI DataSource jdbc/annpath successfully.");
            return;
        } catch (NamingException e) {
            LOGGER.info("JNDI DataSource not found; initializing self-healing internal connection pool.");
        }

        // Initialize driver
        try {
            Class.forName(AppConfig.getDbDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found on classpath!", e);
        }

        // Pre-warm minimum idle connections
        initializePool();
    }

    private synchronized void initializePool() {
        for (int i = 0; i < minIdle; i++) {
            try {
                Connection conn = createNewConnection();
                if (conn != null) {
                    pool.offer(conn);
                    currentTotal++;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error initializing DB connection in pool: " + e.getMessage());
            }
        }
        LOGGER.info("Connection pool initialized with " + pool.size() + " connections.");
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    private Connection createNewConnection() throws SQLException {
        String user = AppConfig.getDbUsername();
        String pass = AppConfig.getDbPassword();
        if (user == null || user.trim().isEmpty()) {
            throw new SQLException("Database configuration error: Database username is not configured. Please set the DB_USERNAME environment variable or configure db.username in application.properties.");
        }
        if (pass == null) {
            pass = "";
        }
        return DriverManager.getConnection(
                AppConfig.getDbUrl(),
                user,
                pass
        );
    }

    /**
     * Borrow an active, validated connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Connection pool is shut down.");
        }

        if (jndiDataSource != null) {
            return jndiDataSource.getConnection();
        }

        Connection conn = null;
        try {
            conn = pool.poll(500, TimeUnit.MILLISECONDS);
            if (conn == null) {
                synchronized (this) {
                    if (currentTotal < maxTotal) {
                        conn = createNewConnection();
                        currentTotal++;
                    }
                }
                if (conn == null) {
                    conn = pool.poll(AppConfig.getInt("db.pool.maxWaitMillis", 10000), TimeUnit.MILLISECONDS);
                }
            }

            if (conn != null) {
                if (conn.isClosed() || !conn.isValid(2)) {
                    LOGGER.warning("Stale connection detected, creating fresh connection.");
                    conn = createNewConnection();
                }
                conn.setAutoCommit(true);
                return conn;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Thread interrupted while waiting for database connection.", e);
        }

        throw new SQLException("Timed out waiting for an available database connection. Maximum pool size reached (" + maxTotal + ").");
    }

    /**
     * Release a connection back to the pool.
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) return;

        if (jndiDataSource != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing JNDI connection", e);
            }
            return;
        }

        try {
            if (conn.isClosed() || isShutdown) {
                synchronized (this) {
                    currentTotal = Math.max(0, currentTotal - 1);
                }
                return;
            }

            if (!conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }

            if (!pool.offer(conn)) {
                conn.close();
                synchronized (this) {
                    currentTotal = Math.max(0, currentTotal - 1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error returning connection to pool: " + e.getMessage());
            try {
                conn.close();
            } catch (Exception ignored) {}
            synchronized (this) {
                currentTotal = Math.max(0, currentTotal - 1);
            }
        }
    }

    /**
     * Gracefully close all open connections.
     */
    public synchronized void shutdown() {
        isShutdown = true;
        Connection conn;
        while ((conn = pool.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {}
        }
        currentTotal = 0;
        LOGGER.info("Database connection pool shut down cleanly.");
    }
}
