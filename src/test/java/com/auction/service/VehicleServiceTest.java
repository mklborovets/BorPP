package com.auction.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auction.dao.LogDao;
import com.auction.dao.VehicleDao;
import com.auction.exception.ServiceException;
import com.auction.model.Log;
import com.auction.model.User;
import com.auction.model.Vehicle;
import com.auction.util.ValidationUtils;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleDao vehicleDao;
    
    @Mock
    private LogDao logDao;
    
    @Mock
    private UserService userService;
    
    // Клас, який ми тестуємо
    private VehicleService vehicleService;
    
    @BeforeEach
    void setUp() {
        // Створюємо екземпляр класу, який тестуємо, і встановлюємо моки через рефлексію
        vehicleService = new VehicleService();
        
        try {
            // Використовуємо рефлексію для встановлення моків
            java.lang.reflect.Field vehicleDaoField = VehicleService.class.getDeclaredField("vehicleDao");
            vehicleDaoField.setAccessible(true);
            vehicleDaoField.set(vehicleService, vehicleDao);
            
            java.lang.reflect.Field logDaoField = VehicleService.class.getDeclaredField("logDao");
            logDaoField.setAccessible(true);
            logDaoField.set(vehicleService, logDao);
            
            java.lang.reflect.Field userServiceField = VehicleService.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(vehicleService, userService);
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
    }

    @Test
    void testAddVehicle_Success() {
        // Arrange
        Long userId = 1L;
        String brand = "Toyota";
        String model = "Corolla";
        int year = 2020;
        String type = "car";
        String condition = "new";
        String vin = "1HGCM82633A123456";
        Integer mileage = 10000;
        String engine = "2.0";
        Double engineVolume = 2.0;
        Integer power = 150;
        String transmission = "automatic";
        String engineType = "gasoline";
        String documents = "all";
        String description = "Good car";
        String photoUrl = "http://example.com/photo.jpg";
        String videoUrl = "http://example.com/video.mp4";
        
        // Створюємо тестовий об'єкт User з роллю "user"
        User testUser = new User();
        testUser.setId(userId);
        testUser.setRole("user");
        
        // Створюємо тестовий об'єкт Vehicle
        Vehicle testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setUserId(userId);
        testVehicle.setBrand(brand);
        testVehicle.setModel(model);
        
        // Налаштовуємо поведінку моків
        when(userService.findById(userId)).thenReturn(testUser);
        when(vehicleDao.findAll()).thenReturn(new ArrayList<>()); // Порожній список для перевірки унікальності VIN
        when(vehicleDao.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class);
             MockedStatic<com.auction.util.FileLogger> mockedFileLogger = mockStatic(com.auction.util.FileLogger.class)) {
            
            // Мокуємо статичні методи
            mockedValidationUtils.when(() -> ValidationUtils.validateVehicle(anyString(), anyString(), anyInt(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(null); // null означає, що валідація пройшла успішно
            
            // Для void методів використовуємо doNothing() або then(invocation -> null)
            mockedFileLogger.when(() -> com.auction.util.FileLogger.logAction(anyLong(), anyString()))
                .then(invocation -> null); // Правильний спосіб мокування void методу
            
            // Act
            Vehicle result = vehicleService.addVehicle(userId, brand, model, year, type, condition, vin, mileage,
                    engine, engineVolume, power, transmission, engineType, documents, description, photoUrl, videoUrl);
            
            // Assert
            assertNotNull(result, "Результат не повинен бути null");
            assertEquals(userId, result.getUserId(), "ID користувача повинен співпадати");
            assertEquals(brand, result.getBrand(), "Бренд повинен співпадати");
            assertEquals(model, result.getModel(), "Модель повинна співпадати");
            
            // Перевіряємо, що методи моків були викликані
            verify(vehicleDao).save(any(Vehicle.class));
            verify(logDao).save(any(Log.class));
            mockedFileLogger.verify(() -> com.auction.util.FileLogger.logAction(eq(userId), anyString()));
        }
    }
    
    @Test
    void testAddVehicle_InsufficientPermissions() {
        // Arrange
        Long userId = 1L;
        String brand = "Toyota";
        String model = "Corolla";
        int year = 2020;
        String type = "car";
        String condition = "new";
        String vin = "1HGCM82633A123456";
        Integer mileage = 10000;
        
        // Створюємо тестовий об'єкт User з роллю "guest"
        User testUser = new User();
        testUser.setId(userId);
        testUser.setRole("guest"); // Роль, яка не має прав для додавання
        
        // Налаштовуємо поведінку моків
        when(userService.findById(userId)).thenReturn(testUser);
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            vehicleService.addVehicle(userId, brand, model, year, type, condition, vin, mileage,
                    null, null, null, null, null, null, null, null, null);
        });
        
        assertEquals("Недостатньо прав для додавання транспортного засобу", exception.getMessage());
        
        // Перевіряємо, що методи моків не були викликані
        verify(vehicleDao, never()).save(any(Vehicle.class));
        verify(logDao, never()).save(any(Log.class));
    }
    
    @Test
    void testAddVehicle_ValidationError() {
        // Arrange
        Long userId = 1L;
        String brand = "Toyota";
        String model = "Corolla";
        int year = 2020;
        String type = "car";
        String condition = "new";
        String vin = "1HGCM82633A123456";
        Integer mileage = 10000;
        
        // Створюємо тестовий об'єкт User з роллю "user"
        User testUser = new User();
        testUser.setId(userId);
        testUser.setRole("user");
        
        // Налаштовуємо поведінку моків
        when(userService.findById(userId)).thenReturn(testUser);
        
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class)) {
            // Мокуємо статичний метод для повернення помилки валідації
            String validationError = "Рік випуску повинен бути не менше 1900";
            mockedValidationUtils.when(() -> ValidationUtils.validateVehicle(anyString(), anyString(), anyInt(), anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(validationError);
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                vehicleService.addVehicle(userId, brand, model, year, type, condition, vin, mileage,
                        "2.0", 2.0, 150, "automatic", "gasoline", "all", "description", "photo.jpg", "video.mp4");
            });
            
            assertEquals(validationError, exception.getMessage());
            
            // Перевіряємо, що методи моків не були викликані
            verify(vehicleDao, never()).save(any(Vehicle.class));
            verify(logDao, never()).save(any(Log.class));
        }
    }
    
    @Test
    void testFindById_Success() throws SQLException {
        // Arrange
        Long vehicleId = 1L;
        String brand = "Toyota";
        String model = "Corolla";
        
        try (MockedStatic<com.auction.util.DatabaseConnection> mockedDbConnection = mockStatic(com.auction.util.DatabaseConnection.class)) {
            // Мокуємо з'єднання з базою даних
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            
            mockedDbConnection.when(() -> com.auction.util.DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            
            // Налаштовуємо поведінку ResultSet для повернення даних транспортного засобу
            when(mockResultSet.next()).thenReturn(true, false); // Один результат, потім кінець
            when(mockResultSet.getLong("id")).thenReturn(vehicleId);
            when(mockResultSet.getLong("user_id")).thenReturn(1L);
            when(mockResultSet.getString("brand")).thenReturn(brand);
            when(mockResultSet.getString("model")).thenReturn(model);
            when(mockResultSet.getInt("year")).thenReturn(2020);
            when(mockResultSet.getString("type")).thenReturn("car");
            when(mockResultSet.getString("condition")).thenReturn("new");
            when(mockResultSet.getString("description")).thenReturn("Good car");
            when(mockResultSet.getString("vin")).thenReturn("1HGCM82633A123456");
            when(mockResultSet.getInt("mileage")).thenReturn(10000);
            when(mockResultSet.getString("engine_type")).thenReturn("gasoline");
            when(mockResultSet.getString("photo_url")).thenReturn("http://example.com/photo.jpg");
            when(mockResultSet.getString("video_url")).thenReturn("http://example.com/video.mp4");
            when(mockResultSet.getString("engine")).thenReturn("2.0");
            when(mockResultSet.getDouble("engine_volume")).thenReturn(2.0);
            when(mockResultSet.getInt("power")).thenReturn(150);
            when(mockResultSet.getString("transmission")).thenReturn("automatic");
            when(mockResultSet.getString("documents")).thenReturn("all");
            
            // Мокуємо дати для уникнення NullPointerException
            java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
            when(mockResultSet.getTimestamp("registration_date")).thenReturn(timestamp);
            when(mockResultSet.getTimestamp("created_at")).thenReturn(timestamp);
            
            // Act
            Vehicle result = vehicleService.findById(vehicleId);
            
            // Assert
            assertNotNull(result, "Результат не повинен бути null");
            assertEquals(vehicleId, result.getId(), "ID транспортного засобу повинен співпадати");
            assertEquals(brand, result.getBrand(), "Бренд повинен співпадати");
            assertEquals(model, result.getModel(), "Модель повинна співпадати");
            
            // Перевіряємо, що методи моків були викликані
            verify(mockConnection).prepareStatement(anyString());
            verify(mockStatement).setLong(1, vehicleId);
            verify(mockStatement).executeQuery();
            verify(mockResultSet, times(1)).next(); // Метод next() викликається лише один раз у методі findById
        }
    }
    
    @Test
    void testFindById_NotFound() throws SQLException {
        // Arrange
        Long vehicleId = 999L; // Неіснуючий ID
        
        try (MockedStatic<com.auction.util.DatabaseConnection> mockedDbConnection = mockStatic(com.auction.util.DatabaseConnection.class)) {
            // Мокуємо з'єднання з базою даних
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            
            mockedDbConnection.when(() -> com.auction.util.DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            
            // Налаштовуємо поведінку ResultSet для порожнього результату
            when(mockResultSet.next()).thenReturn(false); // Немає результатів
            
            // Act
            Vehicle result = vehicleService.findById(vehicleId);
            
            // Assert
            assertNull(result, "Результат повинен бути null для неіснуючого ID");
            
            // Перевіряємо, що методи моків були викликані
            verify(mockConnection).prepareStatement(anyString());
            verify(mockStatement).setLong(1, vehicleId);
            verify(mockStatement).executeQuery();
            verify(mockResultSet).next();
        }
    }
    
    @Test
    void testFindAll_Success() {
        // Arrange
        List<Vehicle> expectedVehicles = Arrays.asList(
            createTestVehicle(1L, "Toyota", "Corolla"),
            createTestVehicle(2L, "Honda", "Civic")
        );
        
        when(vehicleDao.findAll()).thenReturn(expectedVehicles);
        
        // Act
        List<Vehicle> result = vehicleService.findAll();
        
        // Assert
        assertNotNull(result, "Результат не повинен бути null");
        assertEquals(2, result.size(), "Кількість транспортних засобів повинна співпадати");
        assertEquals("Toyota", result.get(0).getBrand(), "Бренд першого транспортного засобу повинен співпадати");
        assertEquals("Honda", result.get(1).getBrand(), "Бренд другого транспортного засобу повинен співпадати");
        
        // Перевіряємо, що метод моку був викликаний
        verify(vehicleDao).findAll();
    }
    
    @Test
    void testDeleteVehicle_Success() {
        // Arrange
        Long userId = 1L;
        Long vehicleId = 1L;
        
        // Створюємо тестовий об'єкт User з роллю "admin"
        User testUser = new User();
        testUser.setId(userId);
        testUser.setRole("admin");
        
        // Створюємо тестовий об'єкт Vehicle
        Vehicle testVehicle = new Vehicle();
        testVehicle.setId(vehicleId);
        testVehicle.setUserId(userId);
        testVehicle.setBrand("Toyota");
        testVehicle.setModel("Corolla");
        
        // Налаштовуємо поведінку моків
        when(vehicleDao.findById(vehicleId)).thenReturn(Optional.of(testVehicle));
        doNothing().when(vehicleDao).delete(vehicleId);
        
        try (MockedStatic<com.auction.util.FileLogger> mockedFileLogger = mockStatic(com.auction.util.FileLogger.class)) {
            // Для void методів використовуємо doNothing() або then(invocation -> null)
            mockedFileLogger.when(() -> com.auction.util.FileLogger.logAction(anyLong(), anyString()))
                .then(invocation -> null); // Правильний спосіб мокування void методу
            
            // Act
            vehicleService.deleteVehicle(userId, vehicleId);
            
            // Assert - перевіряємо, що методи моків були викликані
            verify(vehicleDao).delete(vehicleId);
            verify(logDao).save(any(Log.class));
            mockedFileLogger.verify(() -> com.auction.util.FileLogger.logAction(eq(userId), anyString()));
        }
    }
    
    @Test
    void testGetAllBrands_Success() throws Exception {
        // Arrange - створюємо моки для бази даних
        List<String> expectedBrands = Arrays.asList("Toyota", "Honda", "BMW");
        
        try (MockedStatic<com.auction.util.DatabaseConnection> mockedDbConnection = mockStatic(com.auction.util.DatabaseConnection.class)) {
            // Мокуємо з'єднання з базою даних
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            
            mockedDbConnection.when(() -> com.auction.util.DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            
            // Налаштовуємо поведінку ResultSet для повернення брендів
            when(mockResultSet.next()).thenReturn(true, true, true, false); // 3 бренди, потім кінець
            when(mockResultSet.getString("brand")).thenReturn("Toyota", "Honda", "BMW");
            
            // Act
            List<String> result = vehicleService.getAllBrands();
            
            // Assert
            assertNotNull(result, "Результат не повинен бути null");
            assertEquals(3, result.size(), "Кількість брендів повинна співпадати");
            assertEquals(expectedBrands, result, "Список брендів повинен співпадати");
            
            // Перевіряємо, що методи моків були викликані
            verify(mockConnection).prepareStatement(anyString());
            verify(mockStatement).executeQuery();
            verify(mockResultSet, times(4)).next(); // 3 рази true + 1 раз false
            verify(mockResultSet, times(3)).getString("brand");
        }
    }
    
    // Допоміжний метод для створення тестових транспортних засобів
    private Vehicle createTestVehicle(Long id, String brand, String model) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        return vehicle;
    }
}
