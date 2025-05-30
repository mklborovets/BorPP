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

import com.auction.model.Auction;
import com.auction.util.DatabaseConnection;

public class AuctionDao implements GenericDao<Auction> {
    
    
    @Override
    public Auction save(Auction auction) {
        String sql = "INSERT INTO auctions (vehicle_id, user_id, start_price, price_step, start_time, end_time, status) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, auction.getVehicleId());
        stmt.setLong(2, auction.getUserId()); 
        stmt.setDouble(3, auction.getStartPrice());
        stmt.setDouble(4, auction.getPriceStep());
        stmt.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
        stmt.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
        stmt.setString(7, auction.getStatus());

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            auction.setId(rs.getLong("id"));
        }

        return auction;
    } catch (SQLException e) {
        throw new RuntimeException("Помилка при збереженні аукціону", e);
    }
    }
    
    @Override
    public Optional<Auction> findById(Long id) {
        String sql = "SELECT * FROM auctions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToAuction(rs));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку аукціону", e);
        }
    }
    
    @Override
    public List<Auction> findAll() {
        String sql = "WITH bid_info AS (" +
                    "    SELECT auction_id, " +
                    "           MAX(amount) as current_price, " +
                    "           COUNT(*) as bid_count " +
                    "    FROM bids " +
                    "    GROUP BY auction_id" +
                    "), " +
                    "vehicle_info AS (" +
                    "    SELECT id, " +
                    "           CONCAT(brand, ' ', model, ' (', year, ')') as vehicle_info " +
                    "    FROM vehicles" +
                    ") " +
                    "SELECT a.*, " +
                    "       v.vehicle_info, " +
                    "       COALESCE(b.current_price, a.start_price) as current_price, " +
                    "       COALESCE(b.bid_count, 0) as bid_count " +
                    "FROM auctions a " +
                    "LEFT JOIN vehicle_info v ON a.vehicle_id = v.id " +
                    "LEFT JOIN bid_info b ON a.id = b.auction_id " +
                    "ORDER BY " +
                    "    CASE a.status " +
                    "        WHEN 'ACTIVE' THEN 1 " +
                    "        WHEN 'PENDING' THEN 2 " +
                    "        ELSE 3 " +
                    "    END, " +
                    "    a.created_at DESC";
                    
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Auction auction = mapResultSetToAuction(rs);
                auction.setVehicleInfo(rs.getString("vehicle_info"));
                auction.setCurrentPrice(rs.getDouble("current_price"));
                auction.setBidCount(rs.getInt("bid_count"));
                auctions.add(auction);
            }
            
            return auctions;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні списку аукціонів", e);
        }
    }
    
    @Override
    public void update(Auction auction) {
        String sql = "UPDATE auctions SET vehicle_id = ?, start_price = ?, price_step = ?, " +
                    "start_time = ?, end_time = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, auction.getVehicleId());
            
            stmt.setDouble(2, auction.getStartPrice());
            stmt.setDouble(3, auction.getPriceStep());
            stmt.setTimestamp(4, Timestamp.valueOf(auction.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(auction.getEndTime()));
            stmt.setString(6, auction.getStatus());
            stmt.setLong(7, auction.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні аукціону", e);
        }
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM auctions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні аукціону", e);
        }
    }
    
        public List<Auction> findByStatus(String status) {
        String sql = "SELECT * FROM auctions WHERE status = ?";
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                auctions.add(mapResultSetToAuction(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку аукціонів за статусом", e);
        }
        
        return auctions;
    }
    
    public List<Auction> findActiveAuctions() {
        String sql = "SELECT * FROM auctions WHERE is_active = true AND end_time > CURRENT_TIMESTAMP";
        List<Auction> auctions = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                auctions.add(mapResultSetToAuction(rs));
            }
            return auctions;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні активних аукціонів", e);
        }
    }
    
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
        Auction auction = new Auction();
        auction.setId(rs.getLong("id"));
        auction.setVehicleId(rs.getLong("vehicle_id"));
        auction.setUserId(rs.getLong("user_id")); 
        auction.setStartPrice(rs.getDouble("start_price"));
        auction.setPriceStep(rs.getDouble("price_step"));
        auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        auction.setStatus(rs.getString("status"));
        auction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return auction;
    }
}