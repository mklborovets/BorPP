package com.auction.dao;

import com.auction.model.Vehicle;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleDaoTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private Statement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @InjectMocks
    private VehicleDao vehicleDao;
    
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
        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(1L);
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        vehicle.setYear(2020);
        vehicle.setType("SUV");
        vehicle.setCondition("New");
        vehicle.setDescription("Luxury SUV");
        vehicle.setVin("12345678901234567");
        vehicle.setMileage(1000);
        vehicle.setEngineType("Petrol");
        vehicle.setPhotoUrl("http://example.com/photo.jpg");
        vehicle.setVideoUrl("http://example.com/video.mp4");
        vehicle.setEngine("V8");
        vehicle.setEngineVolume(4.4);
        vehicle.setPower(400);
        vehicle.setTransmission("Automatic");
        vehicle.setDocuments("Full package");
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(5L);
        
        // Act
        Vehicle result = vehicleDao.save(vehicle);
        
        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("BMW", result.getBrand());
        assertEquals("X5", result.getModel());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("INSERT INTO vehicles"));
        verify(mockPreparedStatement).setLong(1, 1L); // user_id
        verify(mockPreparedStatement).setString(2, "BMW"); // brand
        verify(mockPreparedStatement).setString(3, "X5"); // model
        verify(mockPreparedStatement).setInt(4, 2020); // year
        verify(mockPreparedStatement).setString(5, "SUV"); // type
        verify(mockPreparedStatement).setString(6, "New"); // condition
        verify(mockPreparedStatement).setString(7, "Luxury SUV"); // description
        verify(mockPreparedStatement).setString(8, "12345678901234567"); // vin
        verify(mockPreparedStatement).setObject(9, 1000); // mileage
        verify(mockPreparedStatement).setString(10, "Petrol"); // engine_type
        verify(mockPreparedStatement).setString(11, "http://example.com/photo.jpg"); // photo_url
        verify(mockPreparedStatement).setString(12, "http://example.com/video.mp4"); // video_url
        verify(mockPreparedStatement).setString(13, "V8"); // engine
        verify(mockPreparedStatement).setDouble(14, 4.4); // engine_volume
        verify(mockPreparedStatement).setObject(15, 400); // power
        verify(mockPreparedStatement).setString(16, "Automatic"); // transmission
        verify(mockPreparedStatement).setString(17, "Full package"); // documents
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getLong("id");
    }
    
    @Test
    void save_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(1L);
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        vehicle.setYear(2020);
        vehicle.setType("SUV");
        vehicle.setCondition("New");
        vehicle.setDescription("Luxury SUV");
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleDao.save(vehicle));
        assertTrue(exception.getMessage().contains("Помилка при збереженні транспортного засобу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
    
    @Test
    void findById_ReturnsVehicle() throws SQLException {
        // Arrange
        Long vehicleId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("id")).thenReturn(vehicleId);
        when(mockResultSet.getLong("user_id")).thenReturn(1L);
        when(mockResultSet.getString("brand")).thenReturn("BMW");
        when(mockResultSet.getString("model")).thenReturn("X5");
        when(mockResultSet.getInt("year")).thenReturn(2020);
        when(mockResultSet.getString("type")).thenReturn("SUV");
        when(mockResultSet.getString("condition")).thenReturn("New");
        when(mockResultSet.getString("description")).thenReturn("Luxury SUV");
        when(mockResultSet.getString("vin")).thenReturn("12345678901234567");
        when(mockResultSet.getObject("mileage")).thenReturn(1000);
        when(mockResultSet.getString("engine_type")).thenReturn("Petrol");
        when(mockResultSet.getString("photo_url")).thenReturn("http://example.com/photo.jpg");
        when(mockResultSet.getString("video_url")).thenReturn("http://example.com/video.mp4");
        when(mockResultSet.getString("engine")).thenReturn("V8");
        when(mockResultSet.getDouble("engine_volume")).thenReturn(4.4);
        when(mockResultSet.getObject("power")).thenReturn(400);
        when(mockResultSet.getString("transmission")).thenReturn("Automatic");
        when(mockResultSet.getString("documents")).thenReturn("Full package");
        when(mockResultSet.getTimestamp("registration_date")).thenReturn(timestamp);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        Optional<Vehicle> result = vehicleDao.findById(vehicleId);
        
        // Assert
        assertTrue(result.isPresent());
        Vehicle vehicle = result.get();
        assertEquals(vehicleId, vehicle.getId());
        assertEquals("BMW", vehicle.getBrand());
        assertEquals("X5", vehicle.getModel());
        assertEquals(2020, vehicle.getYear());
        assertEquals("SUV", vehicle.getType());
        assertEquals("New", vehicle.getCondition());
        assertEquals("Luxury SUV", vehicle.getDescription());
        assertEquals("12345678901234567", vehicle.getVin());
        assertEquals(1000, vehicle.getMileage());
        assertEquals("Petrol", vehicle.getEngineType());
        assertEquals("http://example.com/photo.jpg", vehicle.getPhotoUrl());
        assertEquals("http://example.com/video.mp4", vehicle.getVideoUrl());
        assertEquals("V8", vehicle.getEngine());
        assertEquals(4.4, vehicle.getEngineVolume());
        assertEquals(400, vehicle.getPower());
        assertEquals("Automatic", vehicle.getTransmission());
        assertEquals("Full package", vehicle.getDocuments());
        assertEquals(now, vehicle.getRegistrationDate());
        assertEquals(now, vehicle.getCreatedAt());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM vehicles WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, vehicleId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
    }
    
    @Test
    void findById_ReturnsEmpty() throws SQLException {
        // Arrange
        Long vehicleId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<Vehicle> result = vehicleDao.findById(vehicleId);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify
        verify(mockConnection).prepareStatement(contains("SELECT * FROM vehicles WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, vehicleId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
    }
    
    @Test
    void findById_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long vehicleId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleDao.findById(vehicleId));
        assertTrue(exception.getMessage().contains("Помилка при пошуку транспортного засобу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
    
    @Test
    void findAll_ReturnsVehicles() throws SQLException {
        // Arrange
        // Налаштовуємо моки
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Повертаємо true двічі, потім false
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L, 2L);
        when(mockResultSet.getString("brand")).thenReturn("BMW", "Audi");
        when(mockResultSet.getString("model")).thenReturn("X5", "Q7");
        when(mockResultSet.getInt("year")).thenReturn(2020, 2021);
        when(mockResultSet.getString("type")).thenReturn("SUV", "SUV");
        when(mockResultSet.getString("condition")).thenReturn("New", "Used");
        when(mockResultSet.getString("description")).thenReturn("Luxury SUV", "Premium SUV");
        when(mockResultSet.getString("vin")).thenReturn("12345678901234567", "76543210987654321");
        when(mockResultSet.getObject("mileage")).thenReturn(1000, 5000);
        when(mockResultSet.getString("engine_type")).thenReturn("Petrol", "Diesel");
        when(mockResultSet.getString("photo_url")).thenReturn("http://example.com/photo1.jpg", "http://example.com/photo2.jpg");
        when(mockResultSet.getString("video_url")).thenReturn("http://example.com/video1.mp4", "http://example.com/video2.mp4");
        when(mockResultSet.getString("engine")).thenReturn("V8", "V6");
        when(mockResultSet.getDouble("engine_volume")).thenReturn(4.4, 3.0);
        when(mockResultSet.getObject("power")).thenReturn(400, 300);
        when(mockResultSet.getString("transmission")).thenReturn("Automatic", "Automatic");
        when(mockResultSet.getString("documents")).thenReturn("Full package", "Full package");
        when(mockResultSet.getTimestamp("registration_date")).thenReturn(timestamp);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
        
        // Act
        List<Vehicle> result = vehicleDao.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("BMW", result.get(0).getBrand());
        assertEquals("X5", result.get(0).getModel());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Audi", result.get(1).getBrand());
        assertEquals("Q7", result.get(1).getModel());
        
        // Verify
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery(contains("SELECT * FROM vehicles"));
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    void findAll_ThrowsRuntimeException() throws SQLException {
        // Arrange
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleDao.findAll());
        assertTrue(exception.getMessage().contains("Помилка при отриманні всіх транспортних засобів"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
    
    @Test
    void update_Success() throws SQLException {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setUserId(1L);
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        vehicle.setYear(2020);
        vehicle.setType("SUV");
        vehicle.setCondition("New");
        vehicle.setDescription("Luxury SUV");
        vehicle.setVin("12345678901234567");
        vehicle.setMileage(1000);
        vehicle.setEngineType("Petrol");
        vehicle.setPhotoUrl("http://example.com/photo.jpg");
        vehicle.setVideoUrl("http://example.com/video.mp4");
        vehicle.setEngine("V8");
        vehicle.setEngineVolume(4.4);
        vehicle.setPower(400);
        vehicle.setTransmission("Automatic");
        vehicle.setDocuments("Full package");
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 row affected
        
        // Act
        vehicleDao.update(vehicle);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("UPDATE vehicles SET"));
        verify(mockPreparedStatement).setLong(1, 1L); // user_id
        verify(mockPreparedStatement).setString(2, "BMW"); // brand
        verify(mockPreparedStatement).setString(3, "X5"); // model
        verify(mockPreparedStatement).setInt(4, 2020); // year
        verify(mockPreparedStatement).setString(5, "SUV"); // type
        verify(mockPreparedStatement).setString(6, "New"); // condition
        verify(mockPreparedStatement).setString(7, "Luxury SUV"); // description
        verify(mockPreparedStatement).setString(8, "12345678901234567"); // vin
        verify(mockPreparedStatement).setObject(9, 1000); // mileage
        verify(mockPreparedStatement).setString(10, "Petrol"); // engine_type
        verify(mockPreparedStatement).setString(11, "http://example.com/photo.jpg"); // photo_url
        verify(mockPreparedStatement).setString(12, "http://example.com/video.mp4"); // video_url
        verify(mockPreparedStatement).setString(13, "V8"); // engine
        verify(mockPreparedStatement).setDouble(14, 4.4); // engine_volume
        verify(mockPreparedStatement).setObject(15, 400); // power
        verify(mockPreparedStatement).setString(16, "Automatic"); // transmission
        verify(mockPreparedStatement).setString(17, "Full package"); // documents
        verify(mockPreparedStatement).setLong(18, 1L); // id in WHERE clause
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void update_VehicleNotFound() throws SQLException {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setUserId(1L);
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        vehicle.setYear(2020);
        vehicle.setType("SUV");
        vehicle.setCondition("New");
        vehicle.setDescription("Luxury SUV");
        vehicle.setVin("12345678901234567");
        vehicle.setMileage(1000);
        vehicle.setEngineType("Petrol");
        vehicle.setPhotoUrl("http://example.com/photo.jpg");
        vehicle.setVideoUrl("http://example.com/video.mp4");
        vehicle.setEngine("V8");
        vehicle.setEngineVolume(4.4);
        vehicle.setPower(400);
        vehicle.setTransmission("Automatic");
        vehicle.setDocuments("Full package");
        
        // Налаштовуємо моки - 0 rows affected
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // 0 rows affected
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleDao.update(vehicle));
        assertEquals("Транспортний засіб не знайдено", exception.getMessage());
    }
    
    @Test
    void update_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setUserId(1L);
        vehicle.setBrand("BMW");
        vehicle.setModel("X5");
        // ... інші поля
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleDao.update(vehicle));
        assertTrue(exception.getMessage().contains("Помилка при оновленні транспортного засобу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
    
    @Test
    void delete_Success() throws SQLException {
        // Arrange
        Long vehicleId = 1L;
        
        // Налаштовуємо моки
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Act
        vehicleDao.delete(vehicleId);
        
        // Verify
        verify(mockConnection).prepareStatement(contains("DELETE FROM vehicles WHERE id = ?"));
        verify(mockPreparedStatement).setLong(1, vehicleId);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void delete_ThrowsRuntimeException() throws SQLException {
        // Arrange
        Long vehicleId = 1L;
        
        // Налаштовуємо моки для викидання SQLException
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> vehicleDao.delete(vehicleId));
        assertTrue(exception.getMessage().contains("Помилка при видаленні транспортного засобу"));
        assertTrue(exception.getCause() instanceof SQLException);
    }
}