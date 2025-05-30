package com.auction.dao;

import com.auction.model.Log;
import com.auction.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogDaoTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @InjectMocks
    private LogDao logDao;
    
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;
    private Timestamp timestamp;
    
    @BeforeEach
    void setUp() throws SQLException {
        // Ініціалізуємо MockedStatic для DatabaseConnection
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        
        // Створюємо timestamp для тестів
        timestamp = new Timestamp(System.currentTimeMillis());
    }
    
    @AfterEach
    void tearDown() {
        // Закриваємо MockedStatic
        mockedDatabaseConnection.close();
    }

    @Test
    void save_Success() throws SQLException {
        // Arrange
        Log log = new Log(1L, "Тестова дія");
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(5L);
        
        // Act
        Log result = logDao.save(log);
        
        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("Тестова дія", result.getAction());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("INSERT INTO logs"));
        verify(mockPreparedStatement).setLong(1, 1L);
        verify(mockPreparedStatement).setString(2, "Тестова дія");
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("id");
    }
    
    @Test
    void save_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Log log = new Log(1L, "Тестова дія");
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> logDao.save(log));
        assertTrue(exception.getMessage().contains("Помилка при збереженні логу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findById_ReturnsLog() throws SQLException {
        // Arrange
        Long logId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(logId);
        when(mockResultSet.getLong("user_id")).thenReturn(2L);
        when(mockResultSet.getString("action")).thenReturn("Тестова дія");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        Optional<Log> result = logDao.findById(logId);
        
        // Assert
        assertTrue(result.isPresent());
        Log log = result.get();
        assertEquals(logId, log.getId());
        assertEquals(2L, log.getUserId());
        assertEquals("Тестова дія", log.getAction());
        assertEquals(timestamp.toLocalDateTime(), log.getCreatedAt());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM logs WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, logId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ReturnsEmpty() throws SQLException {
        // Arrange
        Long logId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<Log> result = logDao.findById(logId);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM logs WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, logId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long logId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> logDao.findById(logId));
        assertTrue(exception.getMessage().contains("Помилка при пошуку логу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findAll_ReturnsLogs() throws SQLException {
        // Arrange
        // Налаштовуємо моки
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getString("action")).thenReturn("Дія 1", "Дія 2");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        List<Log> result = logDao.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        
        // Verify
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(contains("SELECT * FROM logs"));
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findAll_ThrowsRuntimeException() throws SQLException {
        // Arrange
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> logDao.findAll());
        assertTrue(exception.getMessage().contains("Помилка при отриманні всіх логів"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void update_Success() throws SQLException {
        // Arrange
        Log log = new Log(1L, 2L, "Оновлена дія", LocalDateTime.now());
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        logDao.update(log);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("UPDATE logs SET"));
        verify(mockPreparedStatement).setLong(1, 2L);
        verify(mockPreparedStatement).setString(2, "Оновлена дія");
        verify(mockPreparedStatement).setLong(3, 1L);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void update_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Log log = new Log(1L, 2L, "Оновлена дія", LocalDateTime.now());
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> logDao.update(log));
        assertTrue(exception.getMessage().contains("Помилка при оновленні логу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void delete_Success() throws SQLException {
        // Arrange
        Long logId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        logDao.delete(logId);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("DELETE FROM logs WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, logId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void delete_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long logId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> logDao.delete(logId));
        assertTrue(exception.getMessage().contains("Помилка при видаленні логу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findByUserId_ReturnsLogs() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(userId);
        when(mockResultSet.getString("action")).thenReturn("Дія 1", "Дія 2");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        List<Log> result = logDao.findByUserId(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userId, result.get(1).getUserId());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM logs WHERE user_id = ?"));
        verify(mockPreparedStatement).setLong(1, userId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findByUserId_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> logDao.findByUserId(userId));
        assertTrue(exception.getMessage().contains("Помилка при отриманні логів користувача"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
}