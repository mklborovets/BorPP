package com.auction.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.auction.model.Log;
import com.auction.util.DatabaseConnection;

public class LogDao implements GenericDao<Log> {
    
    @Override
    public Log save(Log log) {
        String sql = "INSERT INTO logs (user_id, action) VALUES (?, ?) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, log.getUserId());
            stmt.setString(2, log.getAction());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                log.setId(rs.getLong("id"));
            }
            return log;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні логу", e);
        }
    }
    
    @Override
    public Optional<Log> findById(Long id) {
        String sql = "SELECT * FROM logs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToLog(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку логу", e);
        }
    }
    
    @Override
    public List<Log> findAll() {
        String sql = "SELECT * FROM logs ORDER BY created_at DESC";
        List<Log> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
            return logs;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні всіх логів", e);
        }
    }
    
    @Override
    public void update(Log log) {
        String sql = "UPDATE logs SET user_id = ?, action = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, log.getUserId());
            stmt.setString(2, log.getAction());
            stmt.setLong(3, log.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні логу", e);
        }
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM logs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні логу", e);
        }
    }
    
    public List<Log> findByUserId(Long userId) {
        String sql = "SELECT * FROM logs WHERE user_id = ? ORDER BY created_at DESC";
        List<Log> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
            return logs;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні логів користувача", e);
        }
    }
    
    private Log mapResultSetToLog(ResultSet rs) throws SQLException {
        Log log = new Log();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getLong("user_id"));
        log.setAction(rs.getString("action"));
        log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return log;
    }
} 