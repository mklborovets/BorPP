package com.auction.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionTest {

    private Connection mockConnection;
    private ThreadLocal<Connection> originalConnectionHolder;
    private ThreadLocal<Boolean> originalIsInTransaction;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        // Зберігаємо оригінальні ThreadLocal об'єкти
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        originalConnectionHolder = (ThreadLocal<Connection>) connectionHolderField.get(null);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        originalIsInTransaction = (ThreadLocal<Boolean>) isInTransactionField.get(null);
        
        // Створюємо мок з'єднання
        mockConnection = mock(Connection.class);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Відновлюємо оригінальні ThreadLocal об'єкти
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, originalConnectionHolder);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, originalIsInTransaction);
    }

    @Test
    void testGetConnection_ReturnsExistingConnection() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        when(mockConnection.isClosed()).thenReturn(false);
        
        // Act
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            Connection result = DatabaseConnection.getConnection();
            
            // Assert
            assertEquals(mockConnection, result, "Повинно повернути існуюче з'єднання");
            mockedDriverManager.verifyNoInteractions();
        }
    }
    
    @Test
    void testGetConnection_CreatesNewConnection() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        // Act
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(mockConnection);
            
            Connection result = DatabaseConnection.getConnection();
            
            // Assert
            assertEquals(mockConnection, result, "Повинно створити нове з'єднання");
            mockedDriverManager.verify(() -> DriverManager.getConnection(anyString(), anyString(), anyString()));
            verify(mockConnection).setAutoCommit(true);
        }
    }
    
    @Test
    void testGetConnection_HandlesException() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        SQLException sqlException = new SQLException("Test exception");
        
        // Act & Assert
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class);
             MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class);
             MockedStatic<ErrorHandler> mockedErrorHandler = mockStatic(ErrorHandler.class)) {
            
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenThrow(sqlException);
            
            mockedFileLogger.when(() -> FileLogger.logSystemAction(anyString())).then(invocation -> null);
            
            assertThrows(SQLException.class, () -> {
                DatabaseConnection.getConnection();
            }, "Повинно пробросити виключення SQLException");
            
            mockedFileLogger.verify(() -> FileLogger.logSystemAction(contains("КРИТИЧНА ПОМИЛКА")));
        }
    }

    @Test
    void testBeginTransaction() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(false);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        when(mockConnection.getAutoCommit()).thenReturn(true);
        
        // Act
        DatabaseConnection.beginTransaction();
        
        // Assert
        verify(mockConnection).setAutoCommit(false);
        assertTrue(testIsInTransaction.get(), "Повинно встановити флаг транзакції в true");
    }
    
    @Test
    void testBeginTransaction_AlreadyInTransaction() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        when(mockConnection.getAutoCommit()).thenReturn(false);
        
        // Act
        DatabaseConnection.beginTransaction();
        
        // Assert
        verify(mockConnection, never()).setAutoCommit(anyBoolean());
    }

    @Test
    void testCommitTransaction() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(true);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        when(mockConnection.getAutoCommit()).thenReturn(false);
        
        // Act
        DatabaseConnection.commitTransaction();
        
        // Assert
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
        assertFalse(testIsInTransaction.get(), "Повинно встановити флаг транзакції в false");
    }
    
    @Test
    void testCommitTransaction_NotInTransaction() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        when(mockConnection.getAutoCommit()).thenReturn(true);
        
        // Act
        DatabaseConnection.commitTransaction();
        
        // Assert
        verify(mockConnection, never()).commit();
        verify(mockConnection, never()).setAutoCommit(anyBoolean());
    }

    @Test
    void testRollbackTransaction() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(true);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        when(mockConnection.getAutoCommit()).thenReturn(false);
        
        // Act
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            mockedFileLogger.when(() -> FileLogger.logSystemAction(anyString())).then(invocation -> null);
            
            DatabaseConnection.rollbackTransaction();
            
            // Assert
            verify(mockConnection).rollback();
            verify(mockConnection).setAutoCommit(true);
            assertFalse(testIsInTransaction.get(), "Повинно встановити флаг транзакції в false");
        }
    }
    
    @Test
    void testRollbackTransaction_NotInTransaction() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        when(mockConnection.getAutoCommit()).thenReturn(true);
        
        // Act
        DatabaseConnection.rollbackTransaction();
        
        // Assert
        verify(mockConnection, never()).rollback();
        verify(mockConnection, never()).setAutoCommit(anyBoolean());
    }
    
    @Test
    void testRollbackTransaction_HandlesException() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(true);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        when(mockConnection.getAutoCommit()).thenReturn(false);
        
        SQLException sqlException = new SQLException("Test exception");
        doThrow(sqlException).when(mockConnection).rollback();
        
        // Act & Assert
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class);
             MockedStatic<ErrorHandler> mockedErrorHandler = mockStatic(ErrorHandler.class)) {
            
            mockedFileLogger.when(() -> FileLogger.logSystemAction(anyString())).then(invocation -> null);
            
            assertThrows(SQLException.class, () -> {
                DatabaseConnection.rollbackTransaction();
            }, "Повинно пробросити виключення SQLException");
            
            mockedFileLogger.verify(() -> FileLogger.logSystemAction(contains("ПОМИЛКА")));
            mockedErrorHandler.verify(() -> ErrorHandler.handleCriticalError(eq("Database"), anyString(), eq(sqlException)));
        }
    }

    @Test
    void testIsInTransaction() throws Exception {
        // Arrange
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(true);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        // Act
        boolean result = DatabaseConnection.isInTransaction();
        
        // Assert
        assertTrue(result, "Повинно повернути true, коли в транзакції");
    }
    
    @Test
    void testIsInTransaction_NotInTransaction() throws Exception {
        // Arrange
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(false);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        // Act
        boolean result = DatabaseConnection.isInTransaction();
        
        // Assert
        assertFalse(result, "Повинно повернути false, коли не в транзакції");
    }

    @Test
    void testCloseConnection() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(true);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        when(mockConnection.isClosed()).thenReturn(false);
        
        // Act
        DatabaseConnection.closeConnection();
        
        // Assert
        verify(mockConnection).close();
        assertNull(testConnectionHolder.get(), "Повинно видалити з'єднання з ThreadLocal");
        assertNull(testIsInTransaction.get(), "Повинно видалити флаг транзакції з ThreadLocal");
    }
    
    @Test
    void testCloseConnection_HandlesException() throws Exception {
        // Arrange
        ThreadLocal<Connection> testConnectionHolder = new ThreadLocal<>();
        testConnectionHolder.set(mockConnection);
        
        Field connectionHolderField = DatabaseConnection.class.getDeclaredField("connectionHolder");
        connectionHolderField.setAccessible(true);
        connectionHolderField.set(null, testConnectionHolder);
        
        ThreadLocal<Boolean> testIsInTransaction = new ThreadLocal<>();
        testIsInTransaction.set(true);
        
        Field isInTransactionField = DatabaseConnection.class.getDeclaredField("isInTransaction");
        isInTransactionField.setAccessible(true);
        isInTransactionField.set(null, testIsInTransaction);
        
        when(mockConnection.isClosed()).thenReturn(false);
        
        SQLException sqlException = new SQLException("Test exception");
        doThrow(sqlException).when(mockConnection).close();
        
        // Act
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            mockedFileLogger.when(() -> FileLogger.logSystemAction(anyString())).then(invocation -> null);
            
            DatabaseConnection.closeConnection();
            
            // Assert
            mockedFileLogger.verify(() -> FileLogger.logSystemAction(contains("ПОМИЛКА")));
            assertNull(testConnectionHolder.get(), "Повинно видалити з'єднання з ThreadLocal навіть при помилці");
            assertNull(testIsInTransaction.get(), "Повинно видалити флаг транзакції з ThreadLocal навіть при помилці");
        }
    }
}