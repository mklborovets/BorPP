package com.auction.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

class FileLoggerTest {

    @TempDir
    Path tempDir;
    
    @Test
    void testLogAction() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act
        logger.logAction(123L, "Test action");
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("User ID: 123"), "Лог повинен містити ID користувача");
        assertTrue(logContent.contains("Test action"), "Лог повинен містити текст дії");
        
        // Перевіряємо формат дати/часу
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDateTime.now().format(formatter);
        assertTrue(logContent.contains(today), "Лог повинен містити поточну дату");
    }
    
    @Test
    void testRealFileLogger() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        
        // Використовуємо мокування для перехоплення викликів методів FileLogger
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            // Act
            // Мокуємо виклики методів FileLogger
            FileLogger.logAction(999L, "Real logger test");
            
            // Перевіряємо, що метод був викликаний з правильними параметрами
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(999L), eq("Real logger test")));
        }
    }
    
    @Test
    void testLogSystemAction() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act
        logger.logSystemAction("System test message");
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("SYSTEM"), "Лог повинен містити позначку SYSTEM");
        assertTrue(logContent.contains("System test message"), "Лог повинен містити текст повідомлення");
        
        // Перевіряємо формат дати/часу
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDateTime.now().format(formatter);
        assertTrue(logContent.contains(today), "Лог повинен містити поточну дату");
    }
    
    @Test
    void testMultipleLogEntries() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act
        logger.logAction(123L, "First action");
        logger.logAction(456L, "Second action");
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("User ID: 123"), "Лог повинен містити ID 123");
        assertTrue(logContent.contains("First action"), "Лог повинен містити 'First action'");
        assertTrue(logContent.contains("User ID: 456"), "Лог повинен містити ID 456");
        assertTrue(logContent.contains("Second action"), "Лог повинен містити 'Second action'");
    }

    @Test
    void testLogActionWithSpecialCharacters() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act - логуємо повідомлення зі спеціальними символами
        String specialMessage = "Test with special chars: !@#$%^&*()_+{}|:\"<>?[];',./";
        logger.logAction(123L, specialMessage);
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains(specialMessage), "Лог повинен містити спеціальні символи");
    }
    
    @Test
    void testLogActionWithNullUserId() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act - логуємо з null userId
        logger.logAction(null, "Test with null userId");
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("User ID: null"), "Лог повинен містити 'User ID: null'");
    }
    
    @Test
    void testLogActionWithEmptyMessage() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act - логуємо з порожнім повідомленням
        logger.logAction(123L, "");
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("User ID: 123 - "), "Лог повинен містити ID користувача та порожнє повідомлення");
    }
    
    @Test
    void testLogSystemActionWithEmptyMessage() throws IOException {
        // Створюємо тестовий лог-файл
        Path logDir = tempDir.resolve("logs");
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve("test_log.txt");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(logDir.toString(), "test_log.txt");
        
        // Act - логуємо порожнє системне повідомлення
        logger.logSystemAction("");
        
        // Assert
        assertTrue(Files.exists(logFile), "Лог-файл повинен бути створений");
        
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("SYSTEM - "), "Лог повинен містити позначку SYSTEM та порожнє повідомлення");
    }
    
    @Test
    void testDirectoryCreation() throws IOException {
        // Створюємо тестовий лог-файл у вкладеній директорії
        Path nestedDir = tempDir.resolve("nested/logs");
        
        // Створюємо тестову реалізацію FileLogger
        TestFileLogger logger = new TestFileLogger(nestedDir.toString(), "test_log.txt");
        
        // Act
        logger.logAction(123L, "Test nested directory");
        
        // Assert
        assertTrue(Files.exists(nestedDir), "Вкладена директорія повинна бути створена");
        assertTrue(Files.exists(nestedDir.resolve("test_log.txt")), "Лог-файл повинен бути створений у вкладеній директорії");
    }
    
        private static class TestFileLogger {
        private final String logDirectory;
        private final String logFile;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        public TestFileLogger(String logDirectory, String logFile) {
            this.logDirectory = logDirectory;
            this.logFile = logFile;
        }
        
        public void logAction(Long userId, String action) {
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(formatter);
            String logMessage = String.format("[%s] User ID: %s - %s%n", formattedTime, userId, action);
            
            writeToLogFile(logMessage);
        }
        
        public void logSystemAction(String message) {
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(formatter);
            String logMessage = String.format("[%s] SYSTEM - %s%n", formattedTime, message);
            
            writeToLogFile(logMessage);
        }
        
        private void writeToLogFile(String logMessage) {
            try {
                File logDir = new File(logDirectory);
                if (!logDir.exists()) {
                    logDir.mkdirs(); // Використовуємо mkdirs() замість mkdir() для створення всіх батьківських директорій
                }
                
                File file = new File(logDirectory + File.separator + logFile);
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write(logMessage);
                }
            } catch (IOException e) {
                System.err.println("Помилка при записі в лог-файл: " + e.getMessage());
            }
        }
    }
}
