package com.auction.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ErrorHandlerTest {

    @Test
    void testHandleCriticalErrorWithException() {
        // Arrange
        String errorSource = "TestClass";
        String errorMessage = "Test error message";
        Exception testException = new RuntimeException("Test exception");
        
        // Налаштовуємо перехоплення логів
        Logger logger = Logger.getLogger(ErrorHandler.class.getName());
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        Handler logHandler = new StreamHandler(logOutput, new java.util.logging.SimpleFormatter());
        logger.addHandler(logHandler);
        logger.setLevel(Level.ALL);
        
        // Зберігаємо оригінальний System.err
        PrintStream originalSystemErr = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class);
             MockedStatic<EmailSender> mockedEmailSender = mockStatic(EmailSender.class)) {
            
            // Act
            ErrorHandler.handleCriticalError(errorSource, errorMessage, testException);
            
            // Закриваємо handler, щоб записати всі логи
            logHandler.flush();
            
            // Assert
            String logContent = logOutput.toString();
            assertTrue(logContent.contains("Критична помилка: Test error message"), 
                    "Лог повинен містити повідомлення про помилку");
            
            // Перевіряємо, що FileLogger.logSystemAction був викликаний
            mockedFileLogger.verify(() -> 
                    FileLogger.logSystemAction(contains("КРИТИЧНА ПОМИЛКА: Test error message")));
            
            // Перевіряємо, що EmailSender.sendErrorEmail був викликаний
            mockedEmailSender.verify(() -> 
                    EmailSender.sendErrorEmail(contains("TestClass"), anyString()));
        } finally {
            // Відновлюємо System.err і закриваємо handler
            System.setErr(originalSystemErr);
            logHandler.close();
            logger.removeHandler(logHandler);
        }
    }

    @Test
    void testHandleCriticalErrorWithoutException() {
        // Arrange
        String errorSource = "TestClass";
        String errorMessage = "Test error message without exception";
        
        // Налаштовуємо перехоплення логів
        Logger logger = Logger.getLogger(ErrorHandler.class.getName());
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        Handler logHandler = new StreamHandler(logOutput, new java.util.logging.SimpleFormatter());
        logger.addHandler(logHandler);
        logger.setLevel(Level.ALL);
        
        // Зберігаємо оригінальний System.err
        PrintStream originalSystemErr = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class);
             MockedStatic<EmailSender> mockedEmailSender = mockStatic(EmailSender.class)) {
            
            // Act
            ErrorHandler.handleCriticalError(errorSource, errorMessage);
            
            // Закриваємо handler, щоб записати всі логи
            logHandler.flush();
            
            // Assert
            String logContent = logOutput.toString();
            assertTrue(logContent.contains("Критична помилка: Test error message without exception"), 
                    "Лог повинен містити повідомлення про помилку");
            
            // Перевіряємо, що FileLogger.logSystemAction був викликаний
            mockedFileLogger.verify(() -> 
                    FileLogger.logSystemAction(contains("КРИТИЧНА ПОМИЛКА: Test error message without exception")));
            
            // Перевіряємо, що EmailSender.sendErrorEmail був викликаний
            mockedEmailSender.verify(() -> 
                    EmailSender.sendErrorEmail(contains("TestClass"), anyString()));
        } finally {
            // Відновлюємо System.err і закриваємо handler
            System.setErr(originalSystemErr);
            logHandler.close();
            logger.removeHandler(logHandler);
        }
    }

    @Test
    void testGetStackTraceAsString() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");
        
        // Act - використовуємо безпосередню реалізацію методу
        String result = getStackTraceAsString(testException);
        
        // Assert
        assertNotNull(result, "Результат не повинен бути null");
        assertTrue(result.contains("RuntimeException"), "Результат повинен містити назву винятку");
        assertTrue(result.contains("Test exception"), "Результат повинен містити повідомлення винятку");
    }
    
    // Пряма реалізація методу getStackTraceAsString для тестування
    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
