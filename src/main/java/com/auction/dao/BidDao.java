package com.auction.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.auction.model.Bid;
import com.auction.util.DatabaseConnection;

public class BidDao implements GenericDao<Bid> {
    
    @Override
    public Bid save(Bid bid) {
        String sql = "INSERT INTO bids (user_id, auction_id, amount, bid_time) " +
                    "VALUES (?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bid.getUserId());
            stmt.setLong(2, bid.getAuctionId());
            stmt.setDouble(3, bid.getAmount());
            stmt.setTimestamp(4, Timestamp.valueOf(bid.getCreatedAt()));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                bid.setId(rs.getLong("id"));
            }
            
            return bid;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні ставки", e);
        }
    }
    
    @Override
    public Optional<Bid> findById(Long id) {
        String sql = "SELECT * FROM bids WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBid(rs));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку ставки", e);
        }
    }
    
    @Override
    public List<Bid> findAll() {
        String sql = "SELECT * FROM bids";
        List<Bid> bids = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }
            
            return bids;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні списку ставок", e);
        }
    }
    
    @Override
    public void update(Bid bid) {
        String sql = "UPDATE bids SET user_id = ?, auction_id = ?, amount = ?, bid_time = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, bid.getUserId());
            stmt.setLong(2, bid.getAuctionId());
            stmt.setDouble(3, bid.getAmount());
            stmt.setTimestamp(4, Timestamp.valueOf(bid.getCreatedAt()));
            stmt.setLong(5, bid.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні ставки", e);
        }
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM bids WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні ставки", e);
        }
    }
    
    public List<Bid> findByAuctionId(Long auctionId) {
        String sql = "SELECT b.*, u.username " +
                 "FROM bids b " +
                 "JOIN users u ON b.user_id = u.id " +
                 "WHERE b.auction_id = ? " +
                 "ORDER BY b.amount DESC";
        List<Bid> bids = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, auctionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {          
                bids.add(mapResultSetToBid(rs));
            }
            return bids;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні ставок для аукціону", e);
        }
    }
    
    public Optional<Bid> findHighestBidForAuction(Long auctionId) {
        String sql = "SELECT b.*, u.username " +
                    "FROM bids b " +
                    "JOIN users u ON b.user_id = u.id " +
                    "WHERE b.auction_id = ? " +
                    "ORDER BY b.amount DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, auctionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBid(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні найвищої ставки", e);
        }
    }
    
    private Bid mapResultSetToBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setId(rs.getLong("id"));
        bid.setUserId(rs.getLong("user_id"));
        bid.setAuctionId(rs.getLong("auction_id"));
        bid.setAmount(rs.getDouble("amount"));
        bid.setCreatedAt(rs.getTimestamp("bid_time").toLocalDateTime());
        
        // Перевіряємо, чи є колонка username у результаті запиту
        try {
            String username = rs.getString("username");
            if (username != null) {
                bid.setUsername(username);
            }
        } catch (SQLException e) {
            // Ігноруємо помилку, якщо колонки username немає
        }
        
        return bid;
    }
    
        public void deleteByAuctionId(Long auctionId) {
        String sql = "DELETE FROM bids WHERE auction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, auctionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні ставок для аукціону", e);
        }
    }
    
        public List<Bid> findByUserId(Long userId) {
        String sql = "SELECT * FROM bids WHERE user_id = ? ORDER BY bid_time DESC";
        List<Bid> bids = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку ставок користувача", e);
        }
        
        return bids;
    }
}