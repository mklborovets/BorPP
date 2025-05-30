package com.auction.util;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.auction.model.User;
import com.auction.model.Vehicle;
import com.auction.service.UserService;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class NavigatorTest {

    
    private Stage stage;
    
    
    private Parent testRoot;
    private UserService mockUserService;
    
    @Start
    public void start(Stage stage) {
        this.stage = stage;
    }
    
    @BeforeEach
    void setUp() {
        // Очищаємо поточного користувача перед кожним тестом
        Navigator.setCurrentUser(null);
        
        
        testRoot = new VBox();
        
        
        mockUserService = Mockito.mock(UserService.class);
    }
    
    @Test
    void testSetAndGetCurrentUser() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        // Act
        Navigator.setCurrentUser(testUser);
        User result = Navigator.getCurrentUser();
        
        // Assert
        assertEquals(testUser, result, "Поточний користувач повинен бути встановлений і отриманий коректно");
        assertEquals("testuser", result.getUsername(), "Ім'я користувача повинно бути збережено");
    }
    
    @Test
    void testRefreshCurrentUser() throws Exception {
        // Arrange
        User originalUser = new User();
        originalUser.setId(1L);
        originalUser.setUsername("oldUsername");
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("newUsername");
        
        // Встановлюємо поточного користувача
        Navigator.setCurrentUser(originalUser);
        
        // Мокуємо UserService для повернення оновленого користувача
        // Налаштовуємо поведінку mockUserService перед створенням MockedStatic
        when(mockUserService.findById(1L)).thenReturn(updatedUser);
        
        try (MockedStatic<UserService> mockedUserService = mockStatic(UserService.class)) {
            // Налаштовуємо статичний мок для повернення нашого підготовленого mockUserService
            // при створенні нового екземпляра UserService
            mockedUserService.when(UserService::new).thenReturn(mockUserService);
            
            // Act
            Navigator.refreshCurrentUser();
            
            // Assert
            User result = Navigator.getCurrentUser();
            assertEquals("newUsername", result.getUsername(), "Ім'я користувача повинно бути оновлено");
            
            // Перевіряємо, що метод findById був викликаний
            verify(mockUserService).findById(1L);
        }
    }
    
    @Test
    void testNavigateTo() throws IOException, InterruptedException {
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                
                stage.setScene(new Scene(new VBox()));
                
                
                try (MockedStatic<FXMLLoader> mockedLoader = mockStatic(FXMLLoader.class)) {
                    
                    mockedLoader.when(() -> FXMLLoader.load(any(URL.class))).thenReturn(testRoot);
                    
                    // Act
                    Navigator.navigateTo(stage, "test.fxml");
                    
                    
                    assertNotNull(stage.getScene());
                    assertEquals(testRoot, stage.getScene().getRoot());
                }
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testNavigateToWithNullScene() throws IOException, InterruptedException {
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                
                stage.setScene(null);
                
                
                try (MockedStatic<FXMLLoader> mockedLoader = mockStatic(FXMLLoader.class)) {
                    mockedLoader.when(() -> FXMLLoader.load(any(URL.class))).thenReturn(testRoot);
                    
                    // Act
                    Navigator.navigateTo(stage, "test.fxml");
                    
                    
                    assertNotNull(stage.getScene());
                    assertEquals(testRoot, stage.getScene().getRoot());
                }
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testNavigateToLogin() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Arrange
                try (MockedStatic<Navigator> mockedNavigator = mockStatic(Navigator.class, CALLS_REAL_METHODS)) {
                    // Мокуємо метод navigateTo, щоб перевірити, що він викликається з правильними параметрами
                    // Для void методів використовуємо then(invocation -> null) замість thenReturn(null)
                    mockedNavigator.when(() -> Navigator.navigateTo(any(Stage.class), anyString())).then(invocation -> null);
                    
                    // Act
                    Navigator.navigateToLogin(stage);
                    
                    // Assert
                    mockedNavigator.verify(() -> Navigator.navigateTo(stage, "login.fxml"));
                }
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testNavigateToRegister() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Arrange
                try (MockedStatic<Navigator> mockedNavigator = mockStatic(Navigator.class, CALLS_REAL_METHODS)) {
                    // Мокуємо метод navigateTo, щоб перевірити, що він викликається з правильними параметрами
                    // Для void методів використовуємо then(invocation -> null) замість thenReturn(null)
                    mockedNavigator.when(() -> Navigator.navigateTo(any(Stage.class), anyString())).then(invocation -> null);
                    
                    // Act
                    Navigator.navigateToRegister(stage);
                    
                    // Assert
                    mockedNavigator.verify(() -> Navigator.navigateTo(stage, "register.fxml"));
                }
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testNavigateToProfile() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Arrange
                try (MockedStatic<Navigator> mockedNavigator = mockStatic(Navigator.class, CALLS_REAL_METHODS)) {
                    // Мокуємо метод navigateTo, щоб перевірити, що він викликається з правильними параметрами
                    // Для void методів використовуємо then(invocation -> null) замість thenReturn(null)
                    mockedNavigator.when(() -> Navigator.navigateTo(any(Stage.class), anyString())).then(invocation -> null);
                    
                    // Act
                    Navigator.navigateToProfile(stage);
                    
                    // Assert
                    mockedNavigator.verify(() -> Navigator.navigateTo(stage, "profile.fxml"));
                }
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for the JavaFX thread to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testNavigateToVehicleDetails() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Arrange
                Vehicle testVehicle = new Vehicle();
                testVehicle.setId(1L);
                testVehicle.setBrand("Toyota");
                testVehicle.setModel("Corolla");
                
                
                FXMLLoader testLoader = new FXMLLoader();
                
                try (MockedStatic<FXMLLoader> mockedLoader = mockStatic(FXMLLoader.class)) {
                    // Підготуємо контролер заздалегідь
                    com.auction.controller.VehicleDetailsController mockController = 
                        Mockito.mock(com.auction.controller.VehicleDetailsController.class);
                    
                    // Мокуємо статичний метод для повернення нашого тестового лоадера
                    // Замість конкретного URL використовуємо any() для будь-якого URL
                    mockedLoader.when(() -> new FXMLLoader(any(URL.class))).thenReturn(testLoader);
                    
                    // Мокуємо метод load(), щоб повернути тестовий кореневий елемент
                    mockedLoader.when(() -> testLoader.load()).thenReturn(testRoot);
                    
                    // Налаштовуємо контролер через рефлексію після виклику load()
                    // але перед тим, як він буде використаний
                    doAnswer(invocation -> {
                        // Встановлюємо контролер після виклику load()
                        java.lang.reflect.Field controllerField = FXMLLoader.class.getDeclaredField("controller");
                        controllerField.setAccessible(true);
                        controllerField.set(testLoader, mockController);
                        return testRoot;
                    }).when(testLoader).load();
                    
                    // Act
                    Navigator.navigateToVehicleDetails(stage, testVehicle);
                    
                    // Assert - перевіряємо, що сцена була встановлена
                    assertNotNull(stage.getScene());
                    
                    // Перевіряємо, що метод setVehicle був викликаний з правильним параметром
                    verify(mockController).setVehicle(testVehicle);
                }
            } catch (Exception e) {
                fail("Exception occurred: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
