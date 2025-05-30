package com.auction.dao;

import com.auction.model.User;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @InjectMocks
    private UserDao userDao;
    
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
        User user = new User("testuser", "test@example.com", "password123");
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(1L);
        
        // Act
        User result = userDao.save(user);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("INSERT INTO users"));
        verify(mockPreparedStatement).setString(1, "testuser");
        verify(mockPreparedStatement).setString(2, "password123");
        verify(mockPreparedStatement).setString(3, "test@example.com");
        verify(mockPreparedStatement).setString(4, "USER");
        verify(mockPreparedStatement).setDouble(5, 0.0);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("id");
    }
    
    @Test
    void save_ThrowsRuntimeException() throws SQLException {
        // Arrange
        User user = new User("testuser", "test@example.com", "password123");
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userDao.save(user));
        assertTrue(exception.getMessage().contains("Помилка при збереженні користувача"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findById_ReturnsUser() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(userId);
        when(mockResultSet.getString("username")).thenReturn("testuser");
        when(mockResultSet.getString("password")).thenReturn("hashedpassword");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("role")).thenReturn("USER");
        when(mockResultSet.getDouble("balance")).thenReturn(100.0);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        Optional<User> result = userDao.findById(userId);
        
        // Assert
        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals(userId, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpassword", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("USER", user.getRole());
        assertEquals(100.0, user.getBalance());
        assertEquals(timestamp.toLocalDateTime(), user.getCreatedAt());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM users WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ReturnsEmpty() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<User> result = userDao.findById(userId);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM users WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userDao.findById(userId));
        assertTrue(exception.getMessage().contains("Помилка при пошуку користувача за ID"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findAll_ReturnsUsers() throws SQLException {
        // Arrange
        // Налаштовуємо моки
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getString("username")).thenReturn("user1", "user2");
        when(mockResultSet.getString("password")).thenReturn("pass1", "pass2");
        when(mockResultSet.getString("email")).thenReturn("user1@example.com", "user2@example.com");
        when(mockResultSet.getString("role")).thenReturn("USER", "ADMIN");
        when(mockResultSet.getDouble("balance")).thenReturn(100.0, 200.0);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        List<User> result = userDao.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals(2L, result.get(1).getId());
        assertEquals("user2", result.get(1).getUsername());
        
        // Verify
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(contains("SELECT * FROM users"));
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findAll_ThrowsRuntimeException() throws SQLException {
        // Arrange
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userDao.findAll());
        assertTrue(exception.getMessage().contains("Помилка при отриманні всіх користувачів"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void update_Success() throws SQLException {
        // Arrange
        User user = new User(1L, "updateduser", "updated@example.com", "newpassword", "ADMIN", 500.0, LocalDateTime.now());
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        userDao.update(user);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("UPDATE users SET"));
        verify(mockPreparedStatement).setString(1, "updateduser");
        verify(mockPreparedStatement).setString(2, "newpassword");
        verify(mockPreparedStatement).setString(3, "updated@example.com");
        verify(mockPreparedStatement).setString(4, "ADMIN");
        verify(mockPreparedStatement).setDouble(5, 500.0);
        verify(mockPreparedStatement).setLong(6, 1L);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void update_ThrowsRuntimeException() throws SQLException {
        // Arrange
        User user = new User(1L, "updateduser", "updated@example.com", "newpassword", "ADMIN", 500.0, LocalDateTime.now());
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userDao.update(user));
        assertTrue(exception.getMessage().contains("Помилка при оновленні користувача"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void delete_Success() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        userDao.delete(userId);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("DELETE FROM users WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, userId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void delete_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userDao.delete(userId));
        assertTrue(exception.getMessage().contains("Помилка при видаленні користувача"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findByUsername_ReturnsUser() throws SQLException {
        // Arrange
        String username = "testuser";
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("hashedpassword");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("role")).thenReturn("USER");
        when(mockResultSet.getDouble("balance")).thenReturn(100.0);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        Optional<User> result = userDao.findByUsername(username);
        
        // Assert
        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals(1L, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals("hashedpassword", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("USER", user.getRole());
        assertEquals(100.0, user.getBalance());
        assertEquals(timestamp.toLocalDateTime(), user.getCreatedAt());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM users WHERE username = ?"));
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findByUsername_ReturnsEmpty() throws SQLException {
        // Arrange
        String username = "nonexistentuser";
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<User> result = userDao.findByUsername(username);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM users WHERE username = ?"));
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findByUsername_ThrowsRuntimeException() throws SQLException {
        // Arrange
        String username = "testuser";
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> userDao.findByUsername(username));
        assertTrue(exception.getMessage().contains("Помилка при пошуку користувача за username"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
}