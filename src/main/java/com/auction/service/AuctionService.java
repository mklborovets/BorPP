package com.auction.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.auction.dao.AuctionDao;
import com.auction.dao.BidDao;
import com.auction.dao.LogDao;
import com.auction.exception.ServiceException;
import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.model.Log;
import com.auction.model.Vehicle;
import com.auction.util.DatabaseConnection;
import com.auction.util.FileLogger;
import com.auction.util.Navigator;
import com.auction.util.ValidationUtils;

public class AuctionService {
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final LogDao logDao;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final BidService bidService;
    private List<Auction> auctions;
    private Long nextId;
    private static int statusCheckCounter = 0;

    public AuctionService() {
        this.auctionDao = new AuctionDao();
        this.bidDao = new BidDao();
        this.logDao = new LogDao();
        this.userService = new UserService();
        this.vehicleService = new VehicleService();
        this.bidService = new BidService();
        this.auctions = new ArrayList<>();
        this.nextId = 1L;
    }

    public Auction createAuction(Long userId, Long vehicleId, double startPrice, 
                               double priceStep, LocalDateTime startTime, LocalDateTime endTime) throws ServiceException {
        
        if (!userService.findById(userId).getRole().equals("admin") && 
            !userService.findById(userId).getRole().equals("user")) {
            throw new ServiceException("Недостатньо прав для створення аукціону");
        }

        Vehicle vehicle = vehicleService.findById(vehicleId);
        if (vehicle == null) {
            throw new ServiceException("Транспортний засіб не знайдено");
        }
     
        if (!vehicle.getUserId().equals(userId) && !userService.isAdmin(userId)) {
            throw new ServiceException("Ви не можете створити аукціон для цього транспортного засобу");
        }
       
        if (!findActiveAuctionsByVehicle(vehicleId).isEmpty()) {
            throw new ServiceException("Для цього транспортного засобу вже існує активний аукціон");
        }

        String validationError = ValidationUtils.validateAuction(startPrice, priceStep, startTime, endTime);
        if (validationError != null) {
            throw new ServiceException(validationError);
        }

        Auction auction = new Auction(userId, vehicleId, startPrice, priceStep, startTime, endTime);
        auction.setStatus("PENDING");
      
        auction = auctionDao.save(auction);
     
        String logMessage = "Створено новий аукціон для транспортного засобу: " + vehicle.getBrand() + " " + vehicle.getModel();
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
        
        return auction;
    }

    public void placeBid(Long userId, Long auctionId, double amount) {
        
        if (!userService.findById(userId).getRole().equals("user")) {
            throw new ServiceException("Тільки зареєстровані користувачі можуть робити ставки");
        }

        Auction auction = findById(auctionId);
        if (auction == null) {
            throw new ServiceException("Аукціон не знайдено");
        }

        Vehicle vehicle = vehicleService.findById(auction.getVehicleId());
        if (vehicle.getUserId().equals(userId)) {
            throw new ServiceException("Ви не можете робити ставки у власному аукціоні");
        }

        if (!isAuctionActive(auction)) {
            throw new ServiceException("Аукціон не активний. Статус: " + auction.getStatus());
        }

        double currentPrice = getCurrentPrice(auctionId);

        String validationError = ValidationUtils.validateBid(amount, currentPrice, auction.getPriceStep());
        if (validationError != null) {
            throw new ServiceException(validationError);
        }

        if (userService.getBalance(userId) < amount) {
            throw new ServiceException("Недостатньо коштів на балансі");
        }

        Bid bid = new Bid(userId, auctionId, amount);
        bidDao.save(bid);

        String logMessage = "Зроблено ставку " + amount + " на аукціоні: " + vehicle.getBrand() + " " + vehicle.getModel();
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
    }

    public List<Auction> findActiveAuctions() {
        LocalDateTime now = LocalDateTime.now();
        return auctionDao.findAll().stream()
            .filter(a -> a.getStartTime().isBefore(now) && a.getEndTime().isAfter(now))
            .collect(Collectors.toList());
    }

    public List<Auction> findActiveAuctionsByVehicle(Long vehicleId) {
        LocalDateTime now = LocalDateTime.now();
        return auctionDao.findAll().stream()
            .filter(a -> a.getVehicleId().equals(vehicleId))
            .filter(a -> a.getEndTime().isAfter(now))
            .collect(Collectors.toList());
    }

    public List<Bid> findBidsByAuction(Long auctionId) {
        return bidDao.findAll().stream()
            .filter(b -> b.getAuctionId().equals(auctionId))
            .sorted((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()))
            .collect(Collectors.toList());
    }

    public double getCurrentPrice(Long auctionId) {
        Auction auction = findById(auctionId);
        if (auction == null) {
            throw new ServiceException("Аукціон не знайдено");
        }
        
        return bidDao.findHighestBidForAuction(auctionId)
            .map(Bid::getAmount)
            .orElse(auction.getStartPrice());
    }

    public Auction findById(Long id) {
        return auctionDao.findById(id)
            .map(auction -> {
                updateAuctionStatus(auction);
                return auction;
            })
            .orElse(null);
    }

    private boolean isAuctionActive(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        boolean isTimeValid = auction.getStartTime().isBefore(now) && auction.getEndTime().isAfter(now);
        boolean isStatusActive = "ACTIVE".equals(auction.getStatus());
        return isTimeValid && isStatusActive;
    }

    public List<Auction> findAll() {
        List<Auction> auctions = auctionDao.findAll();
        auctions.forEach(this::updateAuctionStatus);
        return auctions;
    }

    public Auction create(Auction auction) {
        auction.setId((long) (auctions.size() + 1));
        auction.setStatus("ACTIVE");
        auction.setCreatedAt(LocalDateTime.now());
        auctions.add(auction);
        return auction;
    }

    public void update(Auction auction) {
        int index = -1;
        for (int i = 0; i < auctions.size(); i++) {
            if (auctions.get(i).getId().equals(auction.getId())) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            throw new ServiceException("Auction not found");
        }
        
        auctions.set(index, auction);
    }

    public void delete(Long id) {
        auctions.removeIf(a -> a.getId().equals(id));
    }

    public void save(Auction auction) {
        auctionDao.save(auction);
    }

    private void updateAuctionStatus(Auction auction) {
        
        if ("FINISHED".equals(auction.getStatus())) {
            return;
        }
        
        statusCheckCounter++;
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Cycle " + statusCheckCounter + ", Time: " + now);
        
        String oldStatus = auction.getStatus();
        
        if (now.isBefore(auction.getStartTime())) {
            auction.setStatus("PENDING");
        } else if (now.isAfter(auction.getEndTime())) {
            if (!"FINISHED".equals(oldStatus)) {
                try {
                    
                    updateVehicleOwner(auction);
                    auction.setStatus("FINISHED");
                } catch (Exception e) {
                    System.err.println("Помилка при оновленні власника: " + e.getMessage());
                    auction.setStatus("CANCELLED");
                }
            }
        } else {
            auction.setStatus("ACTIVE");
        }

        try {
            auctionDao.update(auction);
        } catch (Exception e) {
            System.err.println("Error updating auction status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateVehicleOwner(Auction auction) {
        System.out.println("\n========= VEHICLE TRANSFER PROCESS ==========");
        System.out.println("Auction ID: " + auction.getId());
        System.out.println("Vehicle ID: " + auction.getVehicleId());
        
        try {
            System.out.println("Finding highest bid for auction...");
            Optional<Bid> highestBid = bidDao.findHighestBidForAuction(auction.getId());
            if (highestBid.isEmpty()) {
                System.out.println("No bids found. Auction ended without a winner.");
                String logMessage = "Auction ended without bids.";
                FileLogger.logAction(auction.getUserId(), logMessage);
                logDao.save(new Log(auction.getUserId(), logMessage));
                return;
            }

            Bid winningBid = highestBid.get();
            System.out.println("Highest bid: " + winningBid.getAmount() + " from user ID: " + winningBid.getUserId());
            
            System.out.println("Getting vehicle data...");
            Vehicle vehicle = vehicleService.findById(auction.getVehicleId());
            if (vehicle == null) {
                System.out.println("ERROR: Vehicle not found!");
                throw new ServiceException("Vehicle not found");
            }
            System.out.println("Found vehicle: " + vehicle.getBrand() + " " + vehicle.getModel());

            Long oldOwnerId = vehicle.getUserId();
            Long newOwnerId = winningBid.getUserId();
            System.out.println("Current owner ID: " + oldOwnerId);
            System.out.println("New owner ID: " + newOwnerId);

            if (oldOwnerId.equals(newOwnerId)) {
                System.out.println("Owner remains unchanged. Transfer not needed.");
                String logMessage = "Auction completed. Owner remained unchanged.";
                FileLogger.logAction(newOwnerId, logMessage);
                logDao.save(new Log(newOwnerId, logMessage));
                return;
            }

            try {
                System.out.println("Starting vehicle transfer transaction...");
                DatabaseConnection.beginTransaction();

                System.out.println("Step 1: Changing vehicle owner from ID " + oldOwnerId + " to ID " + newOwnerId);
                vehicle.setUserId(newOwnerId);
                System.out.println("Calling vehicleService.update() to update owner in database...");
                vehicleService.update(vehicle);

                System.out.println("Step 2: Transferring amount " + winningBid.getAmount() + " from user ID " + newOwnerId + " to user ID " + oldOwnerId);
                userService.transferMoney(newOwnerId, oldOwnerId, winningBid.getAmount());

                System.out.println("Confirming transaction (commit)...");
                DatabaseConnection.commitTransaction();
                System.out.println("Transaction completed successfully!");

                System.out.println("Saving log about successful vehicle transfer...");
                String logMessage = "Аукціон завершено. Транспортний засіб передано новому власнику.";
                FileLogger.logAction(newOwnerId, logMessage);
                logDao.save(new Log(newOwnerId, logMessage));
    
                Navigator.refreshCurrentUser();
                System.out.println("Current user data refreshed.");
      
                System.out.println("Verifying owner update after transaction:");
                Vehicle updatedVehicle = vehicleService.findById(auction.getVehicleId());
                if (updatedVehicle != null) {
                    System.out.println("Current vehicle owner after update: ID " + updatedVehicle.getUserId());
                    if (updatedVehicle.getUserId().equals(newOwnerId)) {
                        System.out.println("SUCCESS: Vehicle owner successfully changed!");
                    } else {
                        System.out.println("ERROR: Vehicle owner did not change! Expected ID " + newOwnerId + ", but remains ID " + updatedVehicle.getUserId());
                    }
                }
            } catch (SQLException e) {
                System.out.println("TRANSACTION ERROR: " + e.getMessage());
                e.printStackTrace();
                try {
                    DatabaseConnection.rollbackTransaction();
                    System.out.println("Transaction rolled back due to error");
                } catch (Exception rollbackEx) {
                    System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                }
                throw new ServiceException("Transaction error", e);
            }
        } catch (Exception e) {
            throw new ServiceException("Error updating owner: " + e.getMessage());
        }
    }
    
        public void deleteAuctionWithBids(Long auctionId, Long adminId) throws ServiceException {
        try {
            
            Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new ServiceException("Аукціон не знайдено"));

            if (!userService.isAdmin(adminId)) {
                throw new ServiceException("Недостатньо прав для видалення аукціону");
            }

            try {
                DatabaseConnection.beginTransaction();

                bidDao.deleteByAuctionId(auctionId);

                auctionDao.delete(auctionId);
 
                String logMessage = "Видалено аукціон #" + auctionId;
                FileLogger.logAction(adminId, logMessage);
                logDao.save(new Log(adminId, logMessage));
 
                DatabaseConnection.commitTransaction();
            } catch (SQLException e) {
                try {
                    DatabaseConnection.rollbackTransaction();
                } catch (SQLException rollbackEx) {
                    throw new ServiceException("Помилка при відкаті транзакції: " + rollbackEx.getMessage());
                }
                throw new ServiceException("Помилка при видаленні аукціону: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new ServiceException("Помилка при видаленні аукціону: " + e.getMessage());
        }
    }
    
        public int getWonAuctionsCount(Long userId) {
        LogService logService = new LogService();

        List<Log> allLogs = logService.findAll();

        List<Log> wonAuctionLogs = allLogs.stream()
            .filter(log -> {
                
                if (!log.getAction().contains("Переказано")) {
                    return false;
                }
                
                
                return log.getUserId().equals(userId);
            })
            .collect(Collectors.toList());
        
        
        return wonAuctionLogs.size();
    }
    
        public int getParticipatedAuctionsCount(Long userId) {
        
        List<Bid> userBids = bidDao.findByUserId(userId);
        
        
        Set<Long> participatedAuctionIds = userBids.stream()
            .map(Bid::getAuctionId)
            .collect(Collectors.toSet());
        
        return participatedAuctionIds.size();
    }
    
        public double getWinPercentage(Long userId) {
        
        int won = getWonAuctionsCount(userId);
        
        
        int participated = getParticipatedAuctionsCount(userId);
        
        if (participated == 0) {
            return 0.0;
        }

        return (double) won / participated * 100.0;
    }
}