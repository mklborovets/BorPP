package com.auction.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.auction.dao.LogDao;
import com.auction.dao.VehicleDao;
import com.auction.exception.ServiceException;
import com.auction.model.Log;
import com.auction.model.Vehicle;
import com.auction.util.DatabaseConnection;
import com.auction.util.FileLogger;
import com.auction.util.ValidationUtils;

public class VehicleService {
    private final VehicleDao vehicleDao;
    private final LogDao logDao;
    private final UserService userService;
    
    public VehicleService() {
        this.vehicleDao = new VehicleDao();
        this.logDao = new LogDao();
        this.userService = new UserService();
    }
    
    public Vehicle addVehicle(Long userId, String brand, String model, int year, String type,
                            String condition, String vin, Integer mileage, String engine,
                            Double engineVolume, Integer power, String transmission,
                            String engineType, String documents, String description,
                            String photoUrl, String videoUrl) {
        // Перевірка прав користувача
        if (!userService.findById(userId).getRole().equals("admin") && 
            !userService.findById(userId).getRole().equals("user")) {
            throw new ServiceException("Недостатньо прав для додавання транспортного засобу");
        }
        
        // Валідація даних
        String validationError = ValidationUtils.validateVehicle(brand, model, year, vin, 
                                                               mileage, photoUrl, videoUrl);
        if (validationError != null) {
            throw new ServiceException(validationError);
        }
        
        // Перевірка унікальності VIN
        if (vin != null) {
            List<Vehicle> vehicles = vehicleDao.findAll();
            boolean vinExists = vehicles.stream()
                .anyMatch(v -> vin.equals(v.getVin()));
            if (vinExists) {
                throw new ServiceException("Транспортний засіб з таким VIN-кодом вже існує");
            }
        }
        
        // Створення нового транспортного засобу відповідного типу
        Vehicle vehicle = com.auction.model.VehicleFactory.createVehicle(type);
        vehicle.setUserId(userId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setType(type);
        vehicle.setCondition(condition);
        vehicle.setVin(vin);
        vehicle.setMileage(mileage);
        vehicle.setEngine(engine);
        vehicle.setEngineVolume(engineVolume);
        vehicle.setPower(power);
        vehicle.setTransmission(transmission);
        vehicle.setEngineType(engineType);
        vehicle.setDocuments(documents);
        vehicle.setDescription(description);
        vehicle.setPhotoUrl(photoUrl);
        vehicle.setVideoUrl(videoUrl);
        
        vehicle = vehicleDao.save(vehicle);
        
        // Логування
        String logMessage = "Додано новий транспортний засіб: " + brand + " " + model;
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
        
        return vehicle;
    }
    
    public void updateVehicle(Long userId, Long vehicleId, String brand, String model, int year,
                            String vin, Integer mileage, String engineType, String condition,
                            String photoUrl, String videoUrl, String description) {
        // Отримання транспортного засобу
        Vehicle vehicle = vehicleDao.findById(vehicleId)
            .orElseThrow(() -> new ServiceException("Транспортний засіб не знайдено"));
        
        // Перевірка прав
        if (!vehicle.getUserId().equals(userId) && !userService.isAdmin(userId)) {
            throw new ServiceException("Недостатньо прав для редагування цього транспортного засобу");
        }
        
        // Валідація даних
        String validationError = ValidationUtils.validateVehicle(brand, model, year, vin,
                                                               mileage, photoUrl, videoUrl);
        if (validationError != null) {
            throw new ServiceException(validationError);
        }
        
        // Оновлення даних
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setVin(vin);
        vehicle.setMileage(mileage);
        vehicle.setEngineType(engineType);
        vehicle.setCondition(condition);
        vehicle.setPhotoUrl(photoUrl);
        vehicle.setVideoUrl(videoUrl);
        vehicle.setDescription(description);
        
        vehicleDao.update(vehicle);
        
        // Логування
        String logMessage = "Оновлено транспортний засіб: " + brand + " " + model;
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
    }
    
    public void deleteVehicle(Long userId, Long vehicleId) {
        // Отримання транспортного засобу
        Vehicle vehicle = vehicleDao.findById(vehicleId)
            .orElseThrow(() -> new ServiceException("Транспортний засіб не знайдено"));
        
        // Перевірка прав
        if (!vehicle.getUserId().equals(userId) && !userService.isAdmin(userId)) {
            throw new ServiceException("Недостатньо прав для видалення цього транспортного засобу");
        }
        
        vehicleDao.delete(vehicleId);
        
        // Логування
        String logMessage = "Видалено транспортний засіб: " + vehicle.getBrand() + " " + vehicle.getModel();
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
    }
    
    public Vehicle findById(Long id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToVehicle(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new ServiceException("Помилка при пошуку транспортного засобу: " + e.getMessage());
        }
    }
    
    public List<Vehicle> findAll() {
        return vehicleDao.findAll();
    }
    
    public List<Vehicle> findByUser(Long userId) {
        return vehicleDao.findAll().stream()
            .filter(v -> v.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    public List<Vehicle> search(String brand, String model, Integer yearFrom, Integer yearTo,
                              Integer mileageFrom, Integer mileageTo, String engineType) {
        return vehicleDao.findAll().stream()
            .filter(v -> brand == null || v.getBrand().toLowerCase().contains(brand.toLowerCase()))
            .filter(v -> model == null || v.getModel().toLowerCase().contains(model.toLowerCase()))
            .filter(v -> yearFrom == null || v.getYear() >= yearFrom)
            .filter(v -> yearTo == null || v.getYear() <= yearTo)
            .filter(v -> mileageFrom == null || v.getMileage() == null || v.getMileage() >= mileageFrom)
            .filter(v -> mileageTo == null || v.getMileage() == null || v.getMileage() <= mileageTo)
            .filter(v -> engineType == null || engineType.equals(v.getEngineType()))
            .collect(Collectors.toList());
    }
    
    public List<Vehicle> findByUserId(Long userId) {
        String sql = "SELECT * FROM vehicles WHERE user_id = ?";
        List<Vehicle> vehicles = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
            return vehicles;
        } catch (SQLException e) {
            throw new ServiceException("Помилка при пошуку транспортних засобів: " + e.getMessage());
        }
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new ServiceException("Транспортний засіб не знайдено");
            }
        } catch (SQLException e) {
            throw new ServiceException("Помилка при видаленні транспортного засобу: " + e.getMessage());
        }
    }
    
    public void update(Vehicle vehicle) {
        try {
            Vehicle currentVehicle = findById(vehicle.getId());
            if (currentVehicle != null) {
                if (!currentVehicle.getUserId().equals(vehicle.getUserId())) {
                    System.out.println("OWNER CHANGE: from ID " + currentVehicle.getUserId() + " to ID " + vehicle.getUserId());
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting current data: " + e.getMessage());
        }
        
        String sql = "UPDATE vehicles SET user_id = ?, brand = ?, model = ?, year = ?, type = ?, " +
                    "condition = ?, description = ?, vin = ?, mileage = ?, engine = ?, " +
                    "engine_volume = ?, power = ?, transmission = ?, documents = ? " +
                    "WHERE id = ?";
                    
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
            stmt.setInt(9, vehicle.getMileage());
            stmt.setString(10, vehicle.getEngine());
            stmt.setDouble(11, vehicle.getEngineVolume());
            stmt.setInt(12, vehicle.getPower());
            stmt.setString(13, vehicle.getTransmission());
            stmt.setString(14, vehicle.getDocuments());
            stmt.setLong(15, vehicle.getId());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new ServiceException("Транспортний засіб не знайдено");
            }
        } catch (SQLException e) {
            throw new ServiceException("Помилка при оновленні транспортного засобу: " + e.getMessage());
        }
    }
    
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        // Отримуємо тип транспорту з бази даних
        String type = rs.getString("type");
        
        // Створюємо об'єкт відповідного типу за допомогою фабрики
        Vehicle vehicle = com.auction.model.VehicleFactory.createVehicle(type);
        
        // Заповнюємо всі поля
        vehicle.setId(rs.getLong("id"));
        vehicle.setUserId(rs.getLong("user_id"));
        vehicle.setBrand(rs.getString("brand"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setYear(rs.getInt("year"));
        vehicle.setType(type); // Встановлюємо тип, який отримали з бази даних
        vehicle.setCondition(rs.getString("condition"));
        vehicle.setDescription(rs.getString("description"));
        vehicle.setVin(rs.getString("vin"));
        vehicle.setMileage(rs.getInt("mileage"));
        vehicle.setEngineType(rs.getString("engine_type"));
        vehicle.setPhotoUrl(rs.getString("photo_url"));
        vehicle.setVideoUrl(rs.getString("video_url"));
        vehicle.setEngine(rs.getString("engine"));
        vehicle.setEngineVolume(rs.getDouble("engine_volume"));
        vehicle.setPower(rs.getInt("power"));
        vehicle.setTransmission(rs.getString("transmission"));
        vehicle.setDocuments(rs.getString("documents"));
        vehicle.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
        vehicle.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return vehicle;
    }
    
        public List<String> getAllBrands() {
        List<String> brands = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT brand FROM vehicles ORDER BY brand")) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String brand = rs.getString("brand");
                if (brand != null && !brand.isEmpty()) {
                    brands.add(brand);
                }
            }
        } catch (SQLException e) {
            throw new ServiceException("Помилка при отриманні списку брендів: " + e.getMessage());
        }
        
        return brands;
    }
}