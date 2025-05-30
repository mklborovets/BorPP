package com.auction.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.dao.AuctionDao;
import com.auction.dao.BidDao;
import com.auction.dao.LogDao;
import com.auction.exception.ServiceException;
import com.auction.model.Auction;
import com.auction.model.Bid;

public class BidService {
    private final BidDao bidDao;
    private final LogDao logDao;
    private final UserService userService;
    private final AuctionDao auctionDao;
    private List<Bid> bids;
    private Long nextId;

    public BidService() {
        this.bidDao = new BidDao();
        this.logDao = new LogDao();
        this.userService = new UserService();
        this.auctionDao = new AuctionDao();
        this.bids = new ArrayList<>();
        this.nextId = 1L;
    }

    public Bid placeBid(Long userId, Long auctionId, double amount) throws ServiceException {
        // Перевіряємо чи існує аукціон
        Auction auction = auctionDao.findById(auctionId)
            .orElseThrow(() -> new ServiceException("Аукціон не знайдено"));
        
        // Перевіряємо статус аукціону
        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new ServiceException("Аукціон не активний");
        }
        
        if (userService.getBalance(userId) < amount) {
            throw new ServiceException("Недостатньо коштів на балансі");
        }
        // Перевіряємо час
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(auction.getStartTime())) {
            throw new ServiceException("Аукціон ще не розпочався");
        }
        if (now.isAfter(auction.getEndTime())) {
            throw new ServiceException("Аукціон вже завершено");
        }

        // Перевіряємо суму ставки
        double currentPrice = bidDao.findHighestBidForAuction(auctionId)
            .map(Bid::getAmount)
            .orElse(auction.getStartPrice());
            
        double minBid = currentPrice + auction.getPriceStep();
        if (amount < minBid) {
            throw new ServiceException(
                String.format("Мінімальна сума ставки: %.2f грн", minBid)
            );
        }
        
        // Створюємо нову ставку
        Bid bid = new Bid(userId, auctionId, amount);
        return bidDao.save(bid);
    }

    public List<Bid> findByAuction(Long auctionId) {
        return bidDao.findByAuctionId(auctionId);
    }

    public List<Bid> findByUser(Long userId) {
        List<Bid> userBids = new ArrayList<>();
        for (Bid bid : bids) {
            if (bid.getUserId().equals(userId)) {
                userBids.add(bid);
            }
        }
        return userBids;
    }

    public Bid findHighestBid(Long auctionId) {
        Bid highestBid = null;
        double maxAmount = 0;
        
        for (Bid bid : bids) {
            if (bid.getAuctionId().equals(auctionId) && bid.getAmount() > maxAmount) {
                maxAmount = bid.getAmount();
                highestBid = bid;
            }
        }
        
        return highestBid;
    }

    public double getCurrentPrice(Long auctionId) {
        return bidDao.findHighestBidForAuction(auctionId)
            .map(Bid::getAmount)
            .orElseGet(() -> auctionDao.findById(auctionId)
                .orElseThrow(() -> new ServiceException("Аукціон не знайдено"))
                .getStartPrice());
    }

    private boolean isAuctionActive(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        return auction.getStartTime().isBefore(now) && 
               auction.getEndTime().isAfter(now) &&
               auction.getStatus().equals("ACTIVE");
    }
} 