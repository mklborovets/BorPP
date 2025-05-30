package com.auction.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
        public static void handleCriticalError(String errorSource, String errorMessage, Throwable exception) {
        // Форматуємо інформацію про помилку
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String stackTrace = getStackTraceAsString(exception);
        
        // Створюємо детальний опис помилки
        StringBuilder errorDetails = new StringBuilder();
        errorDetails.append("Час: ").append(timestamp).append("\n");
        errorDetails.append("Джерело: ").append(errorSource).append("\n");
        errorDetails.append("Повідомлення: ").append(errorMessage).append("\n");
        errorDetails.append("Тип винятку: ").append(exception.getClass().getName()).append("\n\n");
        errorDetails.append("Stack Trace:\n").append(stackTrace);
        
        // Логуємо помилку
        logger.log(Level.SEVERE, "Критична помилка: {0}", errorMessage);
        logger.log(Level.SEVERE, "Stack Trace:", exception);
        
        // Логуємо у файл
        FileLogger.logSystemAction("КРИТИЧНА ПОМИЛКА: " + errorMessage);
        
        // Відправляємо сповіщення на електронну пошту
        String subject = "Критична помилка в Auction System: " + errorSource;
        EmailSender.sendErrorEmail(subject, errorDetails.toString());
    }
    
        public static void handleCriticalError(String errorSource, String errorMessage) {
        // Форматуємо інформацію про помилку
        String timestamp = LocalDateTime.now().format(FORMATTER);
        
        // Створюємо детальний опис помилки
        StringBuilder errorDetails = new StringBuilder();
        errorDetails.append("Час: ").append(timestamp).append("\n");
        errorDetails.append("Джерело: ").append(errorSource).append("\n");
        errorDetails.append("Повідомлення: ").append(errorMessage);
        
        // Логуємо помилку
        logger.log(Level.SEVERE, "Критична помилка: {0}", errorMessage);
        
        // Логуємо у файл
        FileLogger.logSystemAction("КРИТИЧНА ПОМИЛКА: " + errorMessage);
        
        // Відправляємо сповіщення на електронну пошту
        String subject = "Критична помилка в Auction System: " + errorSource;
        EmailSender.sendErrorEmail(subject, errorDetails.toString());
    }
    
        private static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
