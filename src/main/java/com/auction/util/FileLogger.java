package com.auction.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileLogger {
    private static final Logger logger = Logger.getLogger(FileLogger.class.getName());
    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE = "user_actions.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    static {
        // Створюємо директорію для логів, якщо вона не існує
        File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists()) {
            if (!logDir.mkdir()) {
                logger.log(Level.SEVERE, "Не вдалося створити директорію для логів: {0}", LOG_DIRECTORY);
            }
        }
    }
    
        public static void logAction(Long userId, String action) {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(FORMATTER);
        String logMessage = String.format("[%s] User ID: %d - %s", formattedTime, userId, action);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_DIRECTORY + File.separator + LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Помилка при записі в лог-файл", e);
        }
    }
    
        public static void logSystemAction(String message) {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(FORMATTER);
        String logMessage = String.format("[%s] SYSTEM - %s", formattedTime, message);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_DIRECTORY + File.separator + LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Помилка при записі в лог-файл", e);
        }
    }
}
