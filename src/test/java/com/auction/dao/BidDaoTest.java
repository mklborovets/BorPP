package com.auction.dao;

import com.auction.model.Bid;
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
class BidDaoTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @InjectMocks
    private BidDao bidDao;
    
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
        Bid bid = new Bid(1L, 2L, 100.0);
        bid.setCreatedAt(now);
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(5L);
        
        // Act
        Bid result = bidDao.save(bid);
        
        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getAuctionId());
        assertEquals(2L, result.getUserId());
        assertEquals(100.0, result.getAmount());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("INSERT INTO bids"));
        verify(mockPreparedStatement).setLong(1, 2L); // user_id
        verify(mockPreparedStatement).setLong(2, 1L); // auction_id
        verify(mockPreparedStatement).setDouble(3, 100.0); // amount
        verify(mockPreparedStatement).setTimestamp(4, Timestamp.valueOf(now)); // bid_time
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("id");
    }
    
    @Test
    void save_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Bid bid = new Bid(1L, 2L, 100.0);
        bid.setCreatedAt(now);
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.save(bid));
        assertTrue(exception.getMessage().contains("Помилка при збереженні ставки"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findById_ReturnsBid() throws SQLException {
        // Arrange
        Long bidId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(bidId);
        when(mockResultSet.getLong("user_id")).thenReturn(2L);
        when(mockResultSet.getLong("auction_id")).thenReturn(3L);
        when(mockResultSet.getDouble("amount")).thenReturn(150.0);
        when(mockResultSet.getTimestamp("bid_time")).thenReturn(timestamp);
        
        // Act
        Optional<Bid> result = bidDao.findById(bidId);
        
        // Assert
        assertTrue(result.isPresent());
        Bid bid = result.get();
        assertEquals(bidId, bid.getId());
        assertEquals(2L, bid.getUserId());
        assertEquals(3L, bid.getAuctionId());
        assertEquals(150.0, bid.getAmount());
        assertEquals(now, bid.getCreatedAt());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM bids WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, bidId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ReturnsEmpty() throws SQLException {
        // Arrange
        Long bidId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<Bid> result = bidDao.findById(bidId);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM bids WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, bidId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findById_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long bidId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.findById(bidId));
        assertTrue(exception.getMessage().contains("Помилка при пошуку ставки"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findAll_ReturnsBids() throws SQLException {
        // Arrange
        // Налаштовуємо моки
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("auction_id")).thenReturn(1L, 1L);
        when(mockResultSet.getDouble("amount")).thenReturn(100.0, 150.0);
        when(mockResultSet.getTimestamp("bid_time")).thenReturn(timestamp);
        
        // Act
        List<Bid> result = bidDao.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(100.0, result.get(0).getAmount());
        assertEquals(150.0, result.get(1).getAmount());
        
        // Verify
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(contains("SELECT * FROM bids"));
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findAll_ThrowsRuntimeException() throws SQLException {
        // Arrange
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.findAll());
        assertTrue(exception.getMessage().contains("Помилка при отриманні списку ставок"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void update_Success() throws SQLException {
        // Arrange
        Bid bid = new Bid(1L, 1L, 2L, 150.0, now);
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        bidDao.update(bid);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("UPDATE bids SET"));
        verify(mockPreparedStatement).setLong(1, 2L); // user_id
        verify(mockPreparedStatement).setLong(2, 1L); // auction_id
        verify(mockPreparedStatement).setDouble(3, 150.0); // amount
        verify(mockPreparedStatement).setTimestamp(4, Timestamp.valueOf(now)); // bid_time
        verify(mockPreparedStatement).setLong(5, 1L); // id
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void update_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Bid bid = new Bid(1L, 1L, 2L, 150.0, now);
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.update(bid));
        assertTrue(exception.getMessage().contains("Помилка при оновленні ставки"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void delete_Success() throws SQLException {
        // Arrange
        Long bidId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        bidDao.delete(bidId);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("DELETE FROM bids WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, bidId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void delete_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long bidId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.delete(bidId));
        assertTrue(exception.getMessage().contains("Помилка при видаленні ставки"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findByAuctionId_ReturnsBids() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("auction_id")).thenReturn(auctionId);
        when(mockResultSet.getDouble("amount")).thenReturn(100.0, 150.0);
        when(mockResultSet.getTimestamp("bid_time")).thenReturn(timestamp);
        when(mockResultSet.getString("username")).thenReturn("user1", "user2");
        
        // Act
        List<Bid> result = bidDao.findByAuctionId(auctionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(auctionId, result.get(0).getAuctionId());
        assertEquals(auctionId, result.get(1).getAuctionId());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("WHERE b.auction_id = ?"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findByAuctionId_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.findByAuctionId(auctionId));
        assertTrue(exception.getMessage().contains("Помилка при отриманні ставок для аукціону"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findHighestBidForAuction_ReturnsBid() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getLong("user_id")).thenReturn(2L);
        when(mockResultSet.getLong("auction_id")).thenReturn(auctionId);
        when(mockResultSet.getDouble("amount")).thenReturn(200.0);
        when(mockResultSet.getTimestamp("bid_time")).thenReturn(timestamp);
        when(mockResultSet.getString("username")).thenReturn("highestBidder");
        
        // Act
        Optional<Bid> result = bidDao.findHighestBidForAuction(auctionId);
        
        // Assert
        assertTrue(result.isPresent());
        Bid bid = result.get();
        assertEquals(1L, bid.getId());
        assertEquals(2L, bid.getUserId());
        assertEquals(auctionId, bid.getAuctionId());
        assertEquals(200.0, bid.getAmount());
        assertEquals("highestBidder", bid.getUsername());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("WHERE b.auction_id = ? ORDER BY b.amount DESC LIMIT 1"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findHighestBidForAuction_ReturnsEmpty() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<Bid> result = bidDao.findHighestBidForAuction(auctionId);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("WHERE b.auction_id = ? ORDER BY b.amount DESC LIMIT 1"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeQuery();
    }
    
    @Test
    void findHighestBidForAuction_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.findHighestBidForAuction(auctionId));
        assertTrue(exception.getMessage().contains("Помилка при отриманні найвищої ставки"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void deleteByAuctionId_Success() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        bidDao.deleteByAuctionId(auctionId);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("DELETE FROM bids WHERE auction_id = ?"));
        verify(mockPreparedStatement).setLong(1, auctionId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void deleteByAuctionId_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.deleteByAuctionId(auctionId));
        assertTrue(exception.getMessage().contains("Помилка при видаленні ставок для аукціону"));
        assertTrue(exception.getCause() instanceof SQLException);
    }

    @Test
    void findByUserId_ReturnsBids() throws SQLException {
        // Arrange
        Long userId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(userId);
        when(mockResultSet.getLong("auction_id")).thenReturn(1L, 2L);
        when(mockResultSet.getDouble("amount")).thenReturn(100.0, 150.0);
        when(mockResultSet.getTimestamp("bid_time")).thenReturn(timestamp);
        
        // Act
        List<Bid> result = bidDao.findByUserId(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userId, result.get(1).getUserId());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM bids WHERE user_id = ?"));
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
        Exception exception = assertThrows(RuntimeException.class, () -> bidDao.findByUserId(userId));
        assertTrue(exception.getMessage().contains("Помилка при пошуку ставок користувача"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
}