package com.auction.dao;

import com.auction.model.Auction;
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
class AuctionDaoTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @InjectMocks
    private AuctionDao auctionDao;
    
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;
    private Timestamp timestamp;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() throws SQLException {
        // Ініціалізуємо MockedStatic для DatabaseConnection
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        
        // Створюємо timestamp та LocalDateTime для тестів
        now = LocalDateTime.now();
        timestamp = Timestamp.valueOf(now);
    }
    
    @AfterEach
    void tearDown() {
        // Закриваємо MockedStatic
        mockedDatabaseConnection.close();
    }

    @Test
    void save_Success() throws SQLException {
        // Arrange
        LocalDateTime startTime = now.plusDays(1);
        LocalDateTime endTime = now.plusDays(2);
        Auction auction = new Auction(1L, 2L, 1000.0, 100.0, startTime, endTime);
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(5L);
        
        // Act
        Auction result = auctionDao.save(auction);
        
        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getVehicleId());
        assertEquals(1000.0, result.getStartPrice());
        assertEquals(100.0, result.getPriceStep());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertEquals("PENDING", result.getStatus());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("INSERT INTO auctions"));
        verify(mockPreparedStatement).setLong(1, 2L); // vehicle_id
        verify(mockPreparedStatement).setLong(2, 1L); // user_id
        verify(mockPreparedStatement).setDouble(3, 1000.0); // start_price
        verify(mockPreparedStatement).setDouble(4, 100.0); // price_step
        verify(mockPreparedStatement).setTimestamp(5, Timestamp.valueOf(startTime)); // start_time
        verify(mockPreparedStatement).setTimestamp(6, Timestamp.valueOf(endTime)); // end_time
        verify(mockPreparedStatement).setString(7, "PENDING"); // status
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("id");
    }
    
    @Test
    void save_ThrowsRuntimeException() throws SQLException {
        // Arrange
        LocalDateTime startTime = now.plusDays(1);
        LocalDateTime endTime = now.plusDays(2);
        Auction auction = new Auction(1L, 2L, 1000.0, 100.0, startTime, endTime);
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.save(auction));
        assertTrue(exception.getMessage().contains("Помилка при збереженні аукціону"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findById_ReturnsAuction() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(auctionId);
        when(mockResultSet.getLong("user_id")).thenReturn(2L);
        when(mockResultSet.getLong("vehicle_id")).thenReturn(3L);
        when(mockResultSet.getDouble("start_price")).thenReturn(1000.0);
        when(mockResultSet.getDouble("price_step")).thenReturn(100.0);
        when(mockResultSet.getTimestamp("start_time")).thenReturn(timestamp);
        when(mockResultSet.getTimestamp("end_time")).thenReturn(timestamp);
        when(mockResultSet.getString("status")).thenReturn("ACTIVE");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        Optional<Auction> result = auctionDao.findById(auctionId);
        
        // Assert
        assertTrue(result.isPresent());
        Auction auction = result.get();
        assertEquals(auctionId, auction.getId());
        assertEquals(2L, auction.getUserId());
        assertEquals(3L, auction.getVehicleId());
        assertEquals(1000.0, auction.getStartPrice());
        assertEquals(100.0, auction.getPriceStep());
        assertEquals(now, auction.getStartTime());
        assertEquals(now, auction.getEndTime());
        assertEquals("ACTIVE", auction.getStatus());
        assertEquals(now, auction.getCreatedAt());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM auctions WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ReturnsEmpty() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<Auction> result = auctionDao.findById(auctionId);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM auctions WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.findById(auctionId));
        assertTrue(exception.getMessage().contains("Помилка при пошуку аукціону"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findAll_ReturnsAuctions() throws SQLException {
        // Arrange
        // Налаштовуємо моки
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("vehicle_id")).thenReturn(1L, 2L);
        when(mockResultSet.getDouble("start_price")).thenReturn(1000.0, 2000.0);
        when(mockResultSet.getDouble("price_step")).thenReturn(100.0, 200.0);
        when(mockResultSet.getTimestamp("start_time")).thenReturn(timestamp);
        when(mockResultSet.getTimestamp("end_time")).thenReturn(timestamp);
        when(mockResultSet.getString("status")).thenReturn("ACTIVE", "PENDING");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        when(mockResultSet.getString("vehicle_info")).thenReturn("BMW X5 (2020)", "Audi A6 (2019)");
        when(mockResultSet.getDouble("current_price")).thenReturn(1200.0, 2000.0);
        when(mockResultSet.getInt("bid_count")).thenReturn(2, 0);
        
        // Act
        List<Auction> result = auctionDao.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals("BMW X5 (2020)", result.get(0).getVehicleInfo());
        assertEquals("Audi A6 (2019)", result.get(1).getVehicleInfo());
        assertEquals(1200.0, result.get(0).getCurrentPrice());
        assertEquals(2000.0, result.get(1).getCurrentPrice());
        assertEquals(2, result.get(0).getBidCount());
        assertEquals(0, result.get(1).getBidCount());
        
        // Verify
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(contains("SELECT a.*"));
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findAll_ThrowsRuntimeException() throws SQLException {
        // Arrange
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.findAll());
        assertTrue(exception.getMessage().contains("Помилка при отриманні списку аукціонів"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void update_Success() throws SQLException {
        // Arrange
        LocalDateTime startTime = now.plusDays(1);
        LocalDateTime endTime = now.plusDays(2);
        Auction auction = new Auction(1L, 2L, 1000.0, 100.0, startTime, endTime);
        auction.setId(1L);
        auction.setStatus("ACTIVE");
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        auctionDao.update(auction);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("UPDATE auctions SET"));
        verify(mockPreparedStatement).setLong(1, 2L); // vehicle_id
        verify(mockPreparedStatement).setDouble(2, 1000.0); // start_price
        verify(mockPreparedStatement).setDouble(3, 100.0); // price_step
        verify(mockPreparedStatement).setTimestamp(4, Timestamp.valueOf(startTime)); // start_time
        verify(mockPreparedStatement).setTimestamp(5, Timestamp.valueOf(endTime)); // end_time
        verify(mockPreparedStatement).setString(6, "ACTIVE"); // status
        verify(mockPreparedStatement).setLong(7, 1L); // id
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void update_ThrowsRuntimeException() throws SQLException {
        // Arrange
        LocalDateTime startTime = now.plusDays(1);
        LocalDateTime endTime = now.plusDays(2);
        Auction auction = new Auction(1L, 2L, 1000.0, 100.0, startTime, endTime);
        auction.setId(1L);
        auction.setStatus("ACTIVE");
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.update(auction));
        assertTrue(exception.getMessage().contains("Помилка при оновленні аукціону"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void delete_Success() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        auctionDao.delete(auctionId);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("DELETE FROM auctions WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void delete_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.delete(auctionId));
        assertTrue(exception.getMessage().contains("Помилка при видаленні аукціону"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findByStatus_ReturnsAuctions() throws SQLException {
        // Arrange
        String status = "ACTIVE";
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("vehicle_id")).thenReturn(1L, 2L);
        when(mockResultSet.getDouble("start_price")).thenReturn(1000.0, 2000.0);
        when(mockResultSet.getDouble("price_step")).thenReturn(100.0, 200.0);
        when(mockResultSet.getTimestamp("start_time")).thenReturn(timestamp);
        when(mockResultSet.getTimestamp("end_time")).thenReturn(timestamp);
        when(mockResultSet.getString("status")).thenReturn(status);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        List<Auction> result = auctionDao.findByStatus(status);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(status, result.get(0).getStatus());
        assertEquals(status, result.get(1).getStatus());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM auctions WHERE status = ?"));
        verify(mockPreparedStatement).setString(1, status);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findByStatus_ThrowsRuntimeException() throws SQLException {
        // Arrange
        String status = "ACTIVE";
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.findByStatus(status));
        assertTrue(exception.getMessage().contains("Помилка при пошуку аукціонів за статусом"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findActiveAuctions_ReturnsAuctions() throws SQLException {
        // Arrange
        // Налаштовуємо моки
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("vehicle_id")).thenReturn(1L, 2L);
        when(mockResultSet.getDouble("start_price")).thenReturn(1000.0, 2000.0);
        when(mockResultSet.getDouble("price_step")).thenReturn(100.0, 200.0);
        when(mockResultSet.getTimestamp("start_time")).thenReturn(timestamp);
        when(mockResultSet.getTimestamp("end_time")).thenReturn(timestamp);
        when(mockResultSet.getString("status")).thenReturn("ACTIVE");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        List<Auction> result = auctionDao.findActiveAuctions();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("ACTIVE", result.get(1).getStatus());
        
        // Verify
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(contains("SELECT * FROM auctions WHERE is_active = true"));
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findActiveAuctions_ThrowsRuntimeException() throws SQLException {
        // Arrange
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> auctionDao.findActiveAuctions());
        assertTrue(exception.getMessage().contains("Помилка при отриманні активних аукціонів"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
}