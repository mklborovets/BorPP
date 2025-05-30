package com.auction.service;

import com.auction.dao.AuctionDao;
import com.auction.dao.BidDao;
import com.auction.dao.LogDao;
import com.auction.exception.ServiceException;
import com.auction.model.Auction;
import com.auction.model.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidDao bidDao;

    @Mock
    private LogDao logDao;

    @Mock
    private UserService userService;

    @Mock
    private AuctionDao auctionDao;

    private BidService bidService;

    @BeforeEach
    void setUp() {
        // Створюємо екземпляр класу, який тестуємо, і встановлюємо моки через рефлексію
        bidService = new BidService();
        
        try {
            // Використовуємо рефлексію для встановлення моків
            java.lang.reflect.Field bidDaoField = BidService.class.getDeclaredField("bidDao");
            bidDaoField.setAccessible(true);
            bidDaoField.set(bidService, bidDao);
            
            java.lang.reflect.Field logDaoField = BidService.class.getDeclaredField("logDao");
            logDaoField.setAccessible(true);
            logDaoField.set(bidService, logDao);
            
            java.lang.reflect.Field userServiceField = BidService.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(bidService, userService);
            
            java.lang.reflect.Field auctionDaoField = BidService.class.getDeclaredField("auctionDao");
            auctionDaoField.setAccessible(true);
            auctionDaoField.set(bidService, auctionDao);
            
            // Очищаємо список ставок
            java.lang.reflect.Field bidsField = BidService.class.getDeclaredField("bids");
            bidsField.setAccessible(true);
            bidsField.set(bidService, Arrays.asList());
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
    }

    @Test
    void placeBid_Success() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1500.0;
        double userBalance = 2000.0;
        
        // Створюємо тестовий аукціон
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setUserId(2L); // Інший користувач є власником аукціону
        auction.setStartPrice(1000.0);
        auction.setPriceStep(100.0);
        auction.setStatus("ACTIVE");
        auction.setStartTime(LocalDateTime.now().minusHours(1)); // Аукціон вже розпочався
        auction.setEndTime(LocalDateTime.now().plusHours(1)); // Аукціон ще не закінчився
        
        // Створюємо тестову ставку
        Bid highestBid = new Bid();
        highestBid.setId(1L);
        highestBid.setUserId(3L); // Інший користувач зробив попередню ставку
        highestBid.setAuctionId(auctionId);
        highestBid.setAmount(1200.0); // Попередня ставка
        
        Bid newBid = new Bid(auctionId, userId, bidAmount);
        newBid.setId(2L);
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        when(userService.getBalance(userId)).thenReturn(userBalance);
        when(bidDao.findHighestBidForAuction(auctionId)).thenReturn(Optional.of(highestBid));
        when(bidDao.save(any(Bid.class))).thenReturn(newBid);
        
        // Act
        Bid result = bidService.placeBid(userId, auctionId, bidAmount);
        
        // Assert
        assertNotNull(result);
        assertEquals(newBid.getId(), result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(auctionId, result.getAuctionId());
        assertEquals(bidAmount, result.getAmount());
        
        // Перевіряємо, що методи моків були викликані
        verify(auctionDao).findById(auctionId);
        verify(userService).getBalance(userId);
        verify(bidDao).findHighestBidForAuction(auctionId);
        verify(bidDao).save(any(Bid.class));
    }
    
    @Test
    void placeBid_AuctionNotFound() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1500.0;
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.empty());
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.placeBid(userId, auctionId, bidAmount);
        });
        
        assertEquals("Аукціон не знайдено", exception.getMessage());
        verify(auctionDao).findById(auctionId);
        verify(bidDao, never()).save(any(Bid.class));
    }
    
    @Test
    void placeBid_AuctionNotActive() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1500.0;
        
        // Створюємо тестовий аукціон з неактивним статусом
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("CLOSED"); // Неактивний статус
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.placeBid(userId, auctionId, bidAmount);
        });
        
        assertEquals("Аукціон не активний", exception.getMessage());
        verify(auctionDao).findById(auctionId);
        verify(bidDao, never()).save(any(Bid.class));
    }
    
    @Test
    void placeBid_InsufficientFunds() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1500.0;
        double userBalance = 1000.0; // Недостатньо коштів
        
        // Створюємо тестовий аукціон
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("ACTIVE");
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        when(userService.getBalance(userId)).thenReturn(userBalance);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.placeBid(userId, auctionId, bidAmount);
        });
        
        assertEquals("Недостатньо коштів на балансі", exception.getMessage());
        verify(auctionDao).findById(auctionId);
        verify(userService).getBalance(userId);
        verify(bidDao, never()).save(any(Bid.class));
    }
    
    @Test
    void placeBid_AuctionNotStarted() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1500.0;
        double userBalance = 2000.0;
        
        // Створюємо тестовий аукціон, який ще не розпочався
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("ACTIVE");
        auction.setStartTime(LocalDateTime.now().plusHours(1)); // Аукціон ще не розпочався
        auction.setEndTime(LocalDateTime.now().plusHours(2));
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        when(userService.getBalance(userId)).thenReturn(userBalance);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.placeBid(userId, auctionId, bidAmount);
        });
        
        assertEquals("Аукціон ще не розпочався", exception.getMessage());
        verify(auctionDao).findById(auctionId);
        verify(userService).getBalance(userId);
        verify(bidDao, never()).save(any(Bid.class));
    }
    
    @Test
    void placeBid_AuctionEnded() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1500.0;
        double userBalance = 2000.0;
        
        // Створюємо тестовий аукціон, який вже закінчився
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("ACTIVE");
        auction.setStartTime(LocalDateTime.now().minusHours(2));
        auction.setEndTime(LocalDateTime.now().minusHours(1)); // Аукціон вже закінчився
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        when(userService.getBalance(userId)).thenReturn(userBalance);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.placeBid(userId, auctionId, bidAmount);
        });
        
        assertEquals("Аукціон вже завершено", exception.getMessage());
        verify(auctionDao).findById(auctionId);
        verify(userService).getBalance(userId);
        verify(bidDao, never()).save(any(Bid.class));
    }
    
    @Test
    void placeBid_BidTooLow() {
        // Arrange
        Long userId = 1L;
        Long auctionId = 1L;
        double bidAmount = 1250.0; // Ставка занадто низька
        double userBalance = 2000.0;
        
        // Створюємо тестовий аукціон
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus("ACTIVE");
        auction.setStartPrice(1000.0);
        auction.setPriceStep(100.0);
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        
        // Створюємо тестову найвищу ставку
        Bid highestBid = new Bid();
        highestBid.setId(1L);
        highestBid.setAuctionId(auctionId);
        highestBid.setAmount(1200.0); // Поточна найвища ставка
        
        // Налаштовуємо поведінку моків
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        when(userService.getBalance(userId)).thenReturn(userBalance);
        when(bidDao.findHighestBidForAuction(auctionId)).thenReturn(Optional.of(highestBid));
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.placeBid(userId, auctionId, bidAmount);
        });
        
        assertTrue(exception.getMessage().startsWith("Мінімальна сума ставки:"));
        verify(auctionDao).findById(auctionId);
        verify(userService).getBalance(userId);
        verify(bidDao).findHighestBidForAuction(auctionId);
        verify(bidDao, never()).save(any(Bid.class));
    }

    @Test
    void findByAuction_ReturnsAllBidsForAuction() {
        // Arrange
        Long auctionId = 1L;
        List<Bid> expectedBids = Arrays.asList(
            createTestBid(1L, 1L, auctionId, 1500.0),
            createTestBid(2L, 2L, auctionId, 1600.0),
            createTestBid(3L, 3L, auctionId, 1700.0)
        );
        
        when(bidDao.findByAuctionId(auctionId)).thenReturn(expectedBids);
        
        // Act
        List<Bid> result = bidService.findByAuction(auctionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedBids.size(), result.size());
        assertEquals(expectedBids, result);
        
        // Перевіряємо, що метод findByAuctionId був викликаний
        verify(bidDao).findByAuctionId(auctionId);
    }

    @Test
    void findByUser_ReturnsUserBids() {
        // Arrange
        Long userId = 1L;
        Long auctionId1 = 1L;
        Long auctionId2 = 2L;
        
        // Створюємо тестові ставки
        Bid bid1 = createTestBid(1L, userId, auctionId1, 1500.0);
        Bid bid2 = createTestBid(2L, userId, auctionId2, 1600.0);
        Bid bid3 = createTestBid(3L, 2L, auctionId1, 1700.0); // Ставка іншого користувача
        
        // Встановлюємо список ставок через рефлексію
        try {
            java.lang.reflect.Field bidsField = BidService.class.getDeclaredField("bids");
            bidsField.setAccessible(true);
            bidsField.set(bidService, Arrays.asList(bid1, bid2, bid3));
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
        
        // Act
        List<Bid> result = bidService.findByUser(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(bid1));
        assertTrue(result.contains(bid2));
        assertFalse(result.contains(bid3));
    }

    @Test
    void findHighestBid_ReturnsHighestBid() {
        // Arrange
        Long auctionId = 1L;
        
        // Створюємо тестові ставки
        Bid bid1 = createTestBid(1L, 1L, auctionId, 1500.0);
        Bid bid2 = createTestBid(2L, 2L, auctionId, 1800.0); // Найвища ставка
        Bid bid3 = createTestBid(3L, 3L, auctionId, 1700.0);
        
        // Встановлюємо список ставок через рефлексію
        try {
            java.lang.reflect.Field bidsField = BidService.class.getDeclaredField("bids");
            bidsField.setAccessible(true);
            bidsField.set(bidService, Arrays.asList(bid1, bid2, bid3));
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
        
        // Act
        Bid result = bidService.findHighestBid(auctionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(bid2.getId(), result.getId());
        assertEquals(bid2.getAmount(), result.getAmount());
    }
    
    @Test
    void findHighestBid_NoHighestBid_ReturnsNull() {
        // Arrange
        Long auctionId = 1L;
        
        // Створюємо тестові ставки для іншого аукціону
        Bid bid1 = createTestBid(1L, 1L, 2L, 1500.0);
        
        // Встановлюємо список ставок через рефлексію
        try {
            java.lang.reflect.Field bidsField = BidService.class.getDeclaredField("bids");
            bidsField.setAccessible(true);
            bidsField.set(bidService, Arrays.asList(bid1));
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
        
        // Act
        Bid result = bidService.findHighestBid(auctionId);
        
        // Assert
        assertNull(result);
    }

    @Test
    void getCurrentPrice_WithHighestBid() {
        // Arrange
        Long auctionId = 1L;
        double highestBidAmount = 1500.0;
        
        // Створюємо тестову найвищу ставку
        Bid highestBid = new Bid();
        highestBid.setId(1L);
        highestBid.setAuctionId(auctionId);
        highestBid.setAmount(highestBidAmount);
        
        // Налаштовуємо поведінку моків
        when(bidDao.findHighestBidForAuction(auctionId)).thenReturn(Optional.of(highestBid));
        
        // Act
        double result = bidService.getCurrentPrice(auctionId);
        
        // Assert
        assertEquals(highestBidAmount, result);
        verify(bidDao).findHighestBidForAuction(auctionId);
        verify(auctionDao, never()).findById(auctionId); // Не повинен викликатися, якщо є ставка
    }
    
    @Test
    void getCurrentPrice_NoHighestBid_ReturnsStartPrice() {
        // Arrange
        Long auctionId = 1L;
        double startPrice = 1000.0;
        
        // Створюємо тестовий аукціон
        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStartPrice(startPrice);
        
        // Налаштовуємо поведінку моків
        when(bidDao.findHighestBidForAuction(auctionId)).thenReturn(Optional.empty());
        when(auctionDao.findById(auctionId)).thenReturn(Optional.of(auction));
        
        // Act
        double result = bidService.getCurrentPrice(auctionId);
        
        // Assert
        assertEquals(startPrice, result);
        verify(bidDao).findHighestBidForAuction(auctionId);
        verify(auctionDao).findById(auctionId);
    }
    
    @Test
    void getCurrentPrice_AuctionNotFound() {
        // Arrange
        Long auctionId = 1L;
        
        // Налаштовуємо поведінку моків
        when(bidDao.findHighestBidForAuction(auctionId)).thenReturn(Optional.empty());
        when(auctionDao.findById(auctionId)).thenReturn(Optional.empty());
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            bidService.getCurrentPrice(auctionId);
        });
        
        assertEquals("Аукціон не знайдено", exception.getMessage());
        verify(bidDao).findHighestBidForAuction(auctionId);
        verify(auctionDao).findById(auctionId);
    }
    
    // Допоміжний метод для створення тестових ставок
    private Bid createTestBid(Long id, Long userId, Long auctionId, double amount) {
        Bid bid = new Bid(auctionId, userId, amount);
        bid.setId(id);
        bid.setCreatedAt(LocalDateTime.now());
        return bid;
    }
}