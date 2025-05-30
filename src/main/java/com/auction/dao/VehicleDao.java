package com.auction.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.auction.model.Vehicle;
import com.auction.util.DatabaseConnection;

public class VehicleDao implements GenericDao<Vehicle> {
    
    @Override
    public Vehicle save(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (user_id, brand, model, year, type, condition, description, " +
                    "vin, mileage, engine_type, photo_url, video_url, engine, engine_volume, power, " +
                    "transmission, documents, registration_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                    "RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, vehicle.getUserId());
            stmt.setString(2, vehicle.getBrand());
            stmt.setString(3, vehicle.getModel());
            stmt.setInt(4, vehicle.getYear());
            stmt.setString(5, vehicle.getType());
            stmt.setString(6, vehicle.getCondition());
            stmt.setString(7, vehicle.getDescription());
            stmt.setString(8, vehicle.getVin());
            stmt.setObject(9, vehicle.getMileage());
            stmt.setString(10, vehicle.getEngineType());
            stmt.setString(11, vehicle.getPhotoUrl());
            stmt.setString(12, vehicle.getVideoUrl());
            stmt.setString(13, vehicle.getEngine());
            stmt.setDouble(14, vehicle.getEngineVolume());
            stmt.setObject(15, vehicle.getPower());
            stmt.setString(16, vehicle.getTransmission());
            stmt.setString(17, vehicle.getDocuments());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                vehicle.setId(rs.getLong("id"));
            }
            return vehicle;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при збереженні транспортного засобу", e);
        }
    }
    
    @Override
    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToVehicle(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при пошуку транспортного засобу", e);
        }
    }
    
    @Override
    public List<Vehicle> findAll() {
        String sql = "SELECT * FROM vehicles";
        List<Vehicle> vehicles = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
            return vehicles;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні всіх транспортних засобів", e);
        }
    }
    
    @Override
    public void update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET user_id = ?, brand = ?, model = ?, year = ?, type = ?, " +
                    "condition = ?, description = ?, vin = ?, mileage = ?, engine_type = ?, " +
                    "photo_url = ?, video_url = ?, engine = ?, engine_volume = ?, power = ?, " +
                    "transmission = ?, documents = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, vehicle.getUserId());
            stmt.setString(2, vehicle.getBrand());
            stmt.setString(3, vehicle.getModel());
            stmt.setInt(4, vehicle.getYear());
            stmt.setString(5, vehicle.getType());
            stmt.setString(6, vehicle.getCondition());
            stmt.setString(7, vehicle.getDescription());
            stmt.setString(8, vehicle.getVin());
            stmt.setObject(9, vehicle.getMileage());
            stmt.setString(10, vehicle.getEngineType());
            stmt.setString(11, vehicle.getPhotoUrl());
            stmt.setString(12, vehicle.getVideoUrl());
            stmt.setString(13, vehicle.getEngine());
            stmt.setDouble(14, vehicle.getEngineVolume());
            stmt.setObject(15, vehicle.getPower());
            stmt.setString(16, vehicle.getTransmission());
            stmt.setString(17, vehicle.getDocuments());
            stmt.setLong(18, vehicle.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Транспортний засіб не знайдено");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні транспортного засобу", e);
        }
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні транспортного засобу", e);
        }
    }
    
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getLong("id"));
        vehicle.setUserId(rs.getLong("user_id"));
        vehicle.setBrand(rs.getString("brand"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setYear(rs.getInt("year"));
        vehicle.setType(rs.getString("type"));
        vehicle.setCondition(rs.getString("condition"));
        vehicle.setDescription(rs.getString("description"));
        vehicle.setVin(rs.getString("vin"));
        vehicle.setMileage((Integer) rs.getObject("mileage"));
        vehicle.setEngineType(rs.getString("engine_type"));
        vehicle.setPhotoUrl(rs.getString("photo_url"));
        vehicle.setVideoUrl(rs.getString("video_url"));
        vehicle.setEngine(rs.getString("engine"));
        vehicle.setEngineVolume(rs.getDouble("engine_volume"));
        vehicle.setPower((Integer) rs.getObject("power"));
        vehicle.setTransmission(rs.getString("transmission"));
        vehicle.setDocuments(rs.getString("documents"));
        vehicle.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
        vehicle.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return vehicle;
    }
}