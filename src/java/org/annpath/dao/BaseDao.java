package org.annpath.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base DAO providing safe resource lifecycle and connection management.
 */
public abstract class BaseDao {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    protected Connection getConnection() throws SQLException {
        return ConnectionPool.getInstance().getConnection();
    }

    protected void releaseConnection(Connection conn) {
        ConnectionPool.getInstance().releaseConnection(conn);
    }

    protected void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ex) {
                    logger.log(Level.FINE, "Failed to close resource quietly: " + ex.getMessage());
                }
            }
        }
    }

    protected void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Failed to rollback transaction: " + ex.getMessage());
            }
        }
    }
}
