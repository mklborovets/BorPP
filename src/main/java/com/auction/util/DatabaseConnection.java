package com.auction.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String URL = "jdbc:postgresql://ep-autumn-cake-a9lckxoy-pooler.gwc.azure.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_Cd6uv7krRGQy";
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private static ThreadLocal<Boolean> isInTransaction = ThreadLocal.withInitial(() -> false);
    
    // Лічильник помилок підключення до бази даних
    private static int connectionErrorCount = 0;
    private static final int MAX_CONNECTION_ERRORS = 1; // Максимальна кількість помилок перед відправленням сповіщення
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
            
        } catch (ClassNotFoundException e) {
            String errorMessage = "PostgreSQL JDBC Driver не знайдено";
            logger.log(Level.SEVERE, errorMessage, e);
            // Відправляємо сповіщення про критичну помилку
            ErrorHandler.handleCriticalError("Database", errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null || conn.isClosed()) {
            try {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(true);
                connectionHolder.set(conn);
                // Скидаємо лічильник помилок при успішному підключенні
                connectionErrorCount = 0;
                
            } catch (SQLException e) {
                connectionErrorCount++;
                String errorMessage = "Помилка підключення до БД: " + e.getMessage();
                logger.log(Level.SEVERE, errorMessage, e);
                
                // Логуємо у файл
                FileLogger.logSystemAction("КРИТИЧНА ПОМИЛКА: " + errorMessage);
                
                // Якщо кількість помилок перевищує поріг, відправляємо сповіщення
                if (connectionErrorCount >= MAX_CONNECTION_ERRORS) {
                    ErrorHandler.handleCriticalError("Database", 
                        "Критична помилка підключення до бази даних. Кількість спроб: " + connectionErrorCount, e);
                }
                throw e;
            }
        }
        return conn;
    }
    
    public static synchronized void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
            isInTransaction.set(true);
        }
    }
    
    public static synchronized void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        if (!conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
            isInTransaction.set(false);
        }
    }
    
    public static synchronized void rollbackTransaction() throws SQLException {
        try {
            Connection conn = getConnection();
            if (!conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
                isInTransaction.set(false);
                
            }
        } catch (SQLException e) {
            String errorMessage = "Помилка при відкаті транзакції: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            
            // Логуємо у файл
            FileLogger.logSystemAction("ПОМИЛКА: " + errorMessage);
            
            // Відправляємо сповіщення про критичну помилку
            ErrorHandler.handleCriticalError("Database", errorMessage, e);
            throw e;
        }
    }
    
    public static boolean isInTransaction() throws SQLException {
        return isInTransaction.get() != null && isInTransaction.get();
    }
    
    public static void closeConnection() {
        try {
            Connection conn = connectionHolder.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                
            }
        } catch (SQLException e) {
            String errorMessage = "Помилка при закритті з'єднання з базою даних: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            
            // Логуємо у файл
            FileLogger.logSystemAction("ПОМИЛКА: " + errorMessage);
        } finally {
            connectionHolder.remove();
            isInTransaction.remove();
        }
    }
} 