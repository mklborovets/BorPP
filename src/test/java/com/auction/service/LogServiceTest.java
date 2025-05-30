package com.auction.service;

import com.auction.dao.LogDao;
import com.auction.model.Log;
import com.auction.util.FileLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogDao logDao;

    private LogService logService;

    @BeforeEach
    void setUp() {
        // Створюємо екземпляр класу, який тестуємо, і встановлюємо моки через рефлексію
        logService = new LogService();
        
        try {
            // Використовуємо рефлексію для встановлення моків
            java.lang.reflect.Field logDaoField = LogService.class.getDeclaredField("logDao");
            logDaoField.setAccessible(true);
            logDaoField.set(logService, logDao);
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
    }

    @Test
    void createLog_Success() {
        // Arrange
        Long userId = 1L;
        String action = "Тестова дія";
        Log expectedLog = new Log(userId, action);
        expectedLog.setId(1L);
        
        when(logDao.save(any(Log.class))).thenReturn(expectedLog);
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            // Для void методів використовуємо then(invocation -> null)
            mockedFileLogger.when(() -> FileLogger.logAction(anyLong(), anyString()))
                .then(invocation -> null);
            
            // Act
            Log result = logService.createLog(userId, action);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedLog.getId(), result.getId());
            assertEquals(userId, result.getUserId());
            assertEquals(action, result.getAction());
            
            // Перевіряємо, що методи моків були викликані
            verify(logDao).save(any(Log.class));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(userId), eq(action)));
        }
    }

    @Test
    void save_Success() {
        // Arrange
        Long logId = 1L;
        Long userId = 1L;
        String action = "Тестова дія";
        
        Log inputLog = new Log(userId, action);
        Log savedLog = new Log(logId, userId, action, LocalDateTime.now());
        
        when(logDao.save(any(Log.class))).thenReturn(savedLog);
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            mockedFileLogger.when(() -> FileLogger.logAction(anyLong(), anyString()))
                .then(invocation -> null);
            
            // Act
            Log result = logService.save(inputLog);
            
            // Assert
            assertNotNull(result);
            assertEquals(logId, result.getId());
            assertEquals(userId, result.getUserId());
            assertEquals(action, result.getAction());
            
            // Перевіряємо, що методи моків були викликані
            verify(logDao).save(inputLog);
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(userId), eq(action)));
        }
    }

    @Test
    void findAll_ReturnsAllLogs() {
        // Arrange
        List<Log> expectedLogs = Arrays.asList(
            createTestLog(1L, 1L, "Дія 1"),
            createTestLog(2L, 1L, "Дія 2"),
            createTestLog(3L, 2L, "Дія 3")
        );
        
        when(logDao.findAll()).thenReturn(expectedLogs);
        
        // Act
        List<Log> result = logService.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedLogs.size(), result.size());
        assertEquals(expectedLogs, result);
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }

    @Test
    void findByUser_ReturnsUserLogs() {
        // Arrange
        Long userId = 1L;
        List<Log> allLogs = Arrays.asList(
            createTestLog(1L, 1L, "Дія 1"),
            createTestLog(2L, 1L, "Дія 2"),
            createTestLog(3L, 2L, "Дія 3")
        );
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act
        List<Log> result = logService.findByUser(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> log.getUserId().equals(userId)));
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }

    @Test
    void findByDateRange_ReturnsLogsInRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(2);
        LocalDateTime to = now.plusDays(1);
        
        Log log1 = createTestLog(1L, 1L, "Дія 1");
        log1.setCreatedAt(now.minusDays(3)); // Поза діапазоном
        
        Log log2 = createTestLog(2L, 1L, "Дія 2");
        log2.setCreatedAt(now.minusDays(1)); // В діапазоні
        
        Log log3 = createTestLog(3L, 2L, "Дія 3");
        log3.setCreatedAt(now); // В діапазоні
        
        List<Log> allLogs = Arrays.asList(log1, log2, log3);
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act
        List<Log> result = logService.findByDateRange(from, to);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(log2));
        assertTrue(result.contains(log3));
        assertFalse(result.contains(log1));
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }

    @Test
    void findByActionContaining_ReturnsFilteredLogs() {
        // Arrange
        String searchText = "пошук";
        
        Log log1 = createTestLog(1L, 1L, "Дія без ключового слова");
        Log log2 = createTestLog(2L, 1L, "Дія з пошуком");
        Log log3 = createTestLog(3L, 2L, "Інша дія з ПОШУКОМ");
        
        List<Log> allLogs = Arrays.asList(log1, log2, log3);
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act
        List<Log> result = logService.findByActionContaining(searchText);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(log2));
        assertTrue(result.contains(log3));
        assertFalse(result.contains(log1));
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }
    
    @Test
    void findByActionContaining_WithEmptyText_ReturnsAllLogs() {
        // Arrange
        List<Log> allLogs = Arrays.asList(
            createTestLog(1L, 1L, "Дія 1"),
            createTestLog(2L, 1L, "Дія 2"),
            createTestLog(3L, 2L, "Дія 3")
        );
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act - передаємо порожній рядок
        List<Log> result = logService.findByActionContaining("");
        
        // Assert - повинні отримати всі логи
        assertNotNull(result);
        assertEquals(allLogs.size(), result.size());
        assertEquals(allLogs, result);
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }

    @Test
    void findFiltered_WithAllFilters() {
        // Arrange
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(2);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        String actionText = "пошук";
        
        Log log1 = createTestLog(1L, 1L, "Дія з пошуком");
        log1.setCreatedAt(LocalDateTime.now().minusDays(1)); // В діапазоні
        
        Log log2 = createTestLog(2L, 1L, "Інша дія");
        log2.setCreatedAt(LocalDateTime.now()); // В діапазоні, але без ключового слова
        
        Log log3 = createTestLog(3L, 2L, "Дія з пошуком");
        log3.setCreatedAt(LocalDateTime.now()); // В діапазоні, з ключовим словом, але інший користувач
        
        Log log4 = createTestLog(4L, 1L, "Дія з пошуком");
        log4.setCreatedAt(LocalDateTime.now().minusDays(3)); // Поза діапазоном
        
        List<Log> allLogs = Arrays.asList(log1, log2, log3, log4);
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act
        List<Log> result = logService.findFiltered(userId, from, to, actionText);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(log1));
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }
    
    @Test
    void findFiltered_WithNullFilters_ReturnsAllLogs() {
        // Arrange
        List<Log> allLogs = Arrays.asList(
            createTestLog(1L, 1L, "Дія 1"),
            createTestLog(2L, 1L, "Дія 2"),
            createTestLog(3L, 2L, "Дія 3")
        );
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act - передаємо null для всіх фільтрів
        List<Log> result = logService.findFiltered(null, null, null, null);
        
        // Assert - повинні отримати всі логи
        assertNotNull(result);
        assertEquals(allLogs.size(), result.size());
        assertEquals(allLogs, result);
        
        // Перевіряємо, що метод findAll був викликаний
        verify(logDao).findAll();
    }

    @Test
    void deleteOldLogs_DeletesLogsBeforeDate() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusDays(7);
        
        Log log1 = createTestLog(1L, 1L, "Старий лог");
        log1.setCreatedAt(now.minusDays(10)); // Старіший за before
        
        Log log2 = createTestLog(2L, 1L, "Новий лог");
        log2.setCreatedAt(now.minusDays(3)); // Новіший за before
        
        List<Log> allLogs = Arrays.asList(log1, log2);
        
        when(logDao.findAll()).thenReturn(allLogs);
        
        // Act
        logService.deleteOldLogs(before);
        
        // Assert - перевіряємо, що метод delete був викликаний тільки для старого логу
        verify(logDao).delete(log1.getId());
        verify(logDao, never()).delete(log2.getId());
    }
    
    // Допоміжний метод для створення тестових логів
    private Log createTestLog(Long id, Long userId, String action) {
        Log log = new Log(userId, action);
        log.setId(id);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}