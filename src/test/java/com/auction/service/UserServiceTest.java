package com.auction.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auction.dao.LogDao;
import com.auction.dao.UserDao;
import com.auction.exception.ServiceException;
import com.auction.model.Log;
import com.auction.model.User;
import com.auction.util.DatabaseConnection;
import com.auction.util.FileLogger;
import com.auction.util.PasswordHasher;
import com.auction.util.ValidationUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private LogDao logDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Створюємо екземпляр класу, який тестуємо
        userService = new UserService();
        
        try {
            // Використовуємо рефлексію для встановлення моків
            java.lang.reflect.Field userDaoField = UserService.class.getDeclaredField("userDao");
            userDaoField.setAccessible(true);
            userDaoField.set(userService, userDao);
            
            java.lang.reflect.Field logDaoField = UserService.class.getDeclaredField("logDao");
            logDaoField.setAccessible(true);
            logDaoField.set(userService, logDao);
        } catch (Exception e) {
            fail("Помилка при налаштуванні тесту: " + e.getMessage());
        }
    }

    @Test
    void register_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String email = "test@example.com";
        
        // Створюємо тестовий об'єкт User
        User newUser = new User(username, email, "hashedPassword");
        newUser.setId(1L);
        
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class);
             MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class);
             MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            
            // Мокуємо поведінку ValidationUtils
            mockedValidationUtils.when(() -> ValidationUtils.validateUser(eq(username), eq(password), eq(email)))
                .thenReturn(null); // null означає, що валідація пройшла успішно
            
            // Мокуємо поведінку PasswordHasher
            mockedPasswordHasher.when(() -> PasswordHasher.hashPassword(eq(password)))
                .thenReturn("hashedPassword");
            
            // Мокуємо поведінку userDao
            when(userDao.findByUsername(username)).thenReturn(Optional.empty()); // Користувач не існує
            when(userDao.save(any(User.class))).thenReturn(newUser);
            
            // Мокуємо логування
            mockedFileLogger.when(() -> FileLogger.logAction(eq(1L), anyString()))
                .then(invocation -> null);
            
            // Act
            User result = userService.register(username, password, email);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(username, result.getUsername());
            assertEquals(email, result.getEmail());
            
            // Перевіряємо виклики методів
            verify(userDao).findByUsername(username);
            verify(userDao).save(any(User.class));
            verify(logDao).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.validateUser(eq(username), eq(password), eq(email)));
            mockedPasswordHasher.verify(() -> PasswordHasher.hashPassword(eq(password)));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(1L), anyString()));
        }
    }
    
    @Test
    void register_ValidationError() {
        // Arrange
        String username = "test";
        String password = "123"; // Занадто короткий пароль
        String email = "invalid-email";
        String errorMessage = "Пароль повинен містити не менше 6 символів";
        
        // Мокуємо поведінку ValidationUtils
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class)) {
            mockedValidationUtils.when(() -> ValidationUtils.validateUser(eq(username), eq(password), eq(email)))
                .thenReturn(errorMessage); // Валідація не пройшла
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.register(username, password, email);
            });
            
            assertEquals(errorMessage, exception.getMessage());
            
            // Перевіряємо, що методи не викликались
            verify(userDao, never()).findByUsername(anyString());
            verify(userDao, never()).save(any(User.class));
            verify(logDao, never()).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.validateUser(eq(username), eq(password), eq(email)));
        }
    }
    
    @Test
    void register_UserAlreadyExists() {
        // Arrange
        String username = "existinguser";
        String password = "password123";
        String email = "existing@example.com";
        User existingUser = new User(username, "hashedPassword", email);
        existingUser.setId(1L);
        
        // Мокуємо поведінку ValidationUtils
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class)) {
            mockedValidationUtils.when(() -> ValidationUtils.validateUser(eq(username), eq(password), eq(email)))
                .thenReturn(null); // Валідація успішна
            
            // Користувач вже існує
            when(userDao.findByUsername(username)).thenReturn(Optional.of(existingUser));
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.register(username, password, email);
            });
            
            assertEquals("Користувач з таким іменем вже існує", exception.getMessage());
            
            // Перевіряємо виклики методів
            verify(userDao).findByUsername(username);
            verify(userDao, never()).save(any(User.class));
            verify(logDao, never()).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.validateUser(eq(username), eq(password), eq(email)));
        }
    }

    @Test
    void authenticate_Success() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = "hashedPassword";
        String email = "test@example.com";
        
        // Створюємо тестовий об'єкт User
        User user = new User(username, email, hashedPassword);
        user.setId(1L);
        
        // Мокуємо поведінку userDao
        when(userDao.findByUsername(username)).thenReturn(Optional.of(user));
        
        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class);
             MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            
            // Мокуємо перевірку пароля
            mockedPasswordHasher.when(() -> PasswordHasher.verifyPassword(eq(password), eq(hashedPassword)))
                .thenReturn(true);
            
            // Мокуємо логування
            mockedFileLogger.when(() -> FileLogger.logAction(eq(1L), anyString()))
                .then(invocation -> null);
            
            // Act
            User result = userService.authenticate(username, password);
            
            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(username, result.getUsername());
            assertEquals(hashedPassword, result.getPassword());
            
            // Перевіряємо виклики методів
            verify(userDao).findByUsername(username);
            verify(logDao).save(any(Log.class));
            
            mockedPasswordHasher.verify(() -> PasswordHasher.verifyPassword(eq(password), eq(hashedPassword)));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(1L), anyString()));
        }
    }
    
    @Test
    void authenticate_UserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        String password = "password123";
        
        // Мокуємо поведінку userDao
        when(userDao.findByUsername(username)).thenReturn(Optional.empty());
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.authenticate(username, password);
        });
        
        assertEquals("Невірне ім'я користувача або пароль", exception.getMessage());
        
        // Перевіряємо виклики методів
        verify(userDao).findByUsername(username);
        verify(logDao, never()).save(any(Log.class));
    }
    
    @Test
    void authenticate_InvalidPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        String hashedPassword = "hashedPassword";
        String email = "test@example.com";
        
        // Створюємо тестовий об'єкт User
        User user = new User(username, email, hashedPassword);
        user.setId(1L);
        
        // Мокуємо поведінку userDao
        when(userDao.findByUsername(username)).thenReturn(Optional.of(user));
        
        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            // Мокуємо перевірку пароля
            mockedPasswordHasher.when(() -> PasswordHasher.verifyPassword(eq(password), eq(hashedPassword)))
                .thenReturn(false); // Неправильний пароль
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.authenticate(username, password);
            });
            
            assertEquals("Невірне ім'я користувача або пароль", exception.getMessage());
            
            // Перевіряємо виклики методів
            verify(userDao).findByUsername(username);
            verify(logDao, never()).save(any(Log.class));
            
            mockedPasswordHasher.verify(() -> PasswordHasher.verifyPassword(eq(password), eq(hashedPassword)));
        }
    }

    @Test
    void updateBalance_Success() {
        // Arrange
        Long userId = 1L;
        double amount = 500.0;
        double initialBalance = 1000.0;
        double expectedBalance = initialBalance + amount;
        
        User user = new User("testuser", "hashedPassword", "test@example.com");
        user.setId(userId);
        user.setBalance(initialBalance);
        
        // Мокуємо поведінку ValidationUtils
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class);
             MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            
            mockedValidationUtils.when(() -> ValidationUtils.isValidAmount(amount))
                .thenReturn(true);
            
            // Мокуємо поведінку userDao
            when(userDao.findById(userId)).thenReturn(Optional.of(user));
            
            // Мокуємо логування
            mockedFileLogger.when(() -> FileLogger.logAction(eq(userId), anyString()))
                .then(invocation -> null);
            
            // Act
            userService.updateBalance(userId, amount);
            
            // Assert
            assertEquals(expectedBalance, user.getBalance());
            
            // Перевіряємо виклики методів
            verify(userDao).findById(userId);
            verify(userDao).update(user);
            verify(logDao).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.isValidAmount(amount));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(userId), anyString()));
        }
    }
    
    @Test
    void updateBalance_InvalidAmount() {
        // Arrange
        Long userId = 1L;
        double amount = -500.0; // Від'ємна сума, яка призведе до від'ємного балансу
        
        // Мокуємо поведінку ValidationUtils
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class)) {
            mockedValidationUtils.when(() -> ValidationUtils.isValidAmount(amount))
                .thenReturn(false);
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.updateBalance(userId, amount);
            });
            
            assertEquals("Невірна сума", exception.getMessage());
            
            // Перевіряємо виклики методів
            verify(userDao, never()).findById(anyLong());
            verify(userDao, never()).update(any(User.class));
            verify(logDao, never()).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.isValidAmount(amount));
        }
    }
    
    @Test
    void updateBalance_UserNotFound() {
        // Arrange
        Long userId = 1L;
        double amount = 500.0;
        
        // Мокуємо поведінку ValidationUtils
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class)) {
            mockedValidationUtils.when(() -> ValidationUtils.isValidAmount(amount))
                .thenReturn(true);
            
            // Мокуємо поведінку userDao
            when(userDao.findById(userId)).thenReturn(Optional.empty());
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.updateBalance(userId, amount);
            });
            
            assertEquals("Користувача не знайдено", exception.getMessage());
            
            // Перевіряємо виклики методів
            verify(userDao).findById(userId);
            verify(userDao, never()).update(any(User.class));
            verify(logDao, never()).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.isValidAmount(amount));
        }
    }
    
    @Test
    void updateBalance_InsufficientFunds() {
        // Arrange
        Long userId = 1L;
        double amount = -1500.0; // Спроба зняти більше, ніж є на балансі
        double initialBalance = 1000.0;
        
        User user = new User("testuser", "hashedPassword", "test@example.com");
        user.setId(userId);
        user.setBalance(initialBalance);
        
        // Мокуємо поведінку ValidationUtils
        try (MockedStatic<ValidationUtils> mockedValidationUtils = mockStatic(ValidationUtils.class)) {
            mockedValidationUtils.when(() -> ValidationUtils.isValidAmount(amount))
                .thenReturn(true);
            
            // Мокуємо поведінку userDao
            when(userDao.findById(userId)).thenReturn(Optional.of(user));
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.updateBalance(userId, amount);
            });
            
            assertEquals("Недостатньо коштів на балансі", exception.getMessage());
            
            // Перевіряємо виклики методів
            verify(userDao).findById(userId);
            verify(userDao, never()).update(any(User.class));
            verify(logDao, never()).save(any(Log.class));
            
            mockedValidationUtils.verify(() -> ValidationUtils.isValidAmount(amount));
        }
    }
    
    @Test
    void changeRole_Success() {
        // Arrange
        Long userId = 1L;
        String newRole = "admin";
        
        User user = new User("testuser", "test@example.com", "hashedPassword");
        user.setId(userId);
        user.setRole("user");
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            // Мокуємо логування
            mockedFileLogger.when(() -> FileLogger.logAction(eq(userId), anyString()))
                .then(invocation -> null);
            
            // Act
            userService.changeRole(userId, newRole);
            
            // Assert
            assertEquals(newRole, user.getRole());
            
            // Перевіряємо виклики методів
            verify(userDao).findById(userId);
            verify(userDao).update(user);
            verify(logDao).save(any(Log.class));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(userId), anyString()));
        }
    }
    
    @Test
    void changeRole_InvalidRole() {
        // Arrange
        Long userId = 1L;
        String newRole = "superuser"; // Недійсна роль
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.changeRole(userId, newRole);
        });
        
        assertEquals("Невірна роль", exception.getMessage());
        
        // Перевіряємо виклики методів
        verify(userDao, never()).findById(anyLong());
        verify(userDao, never()).update(any(User.class));
        verify(logDao, never()).save(any(Log.class));
    }
    
    @Test
    void changeRole_UserNotFound() {
        // Arrange
        Long userId = 1L;
        String newRole = "admin";
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.changeRole(userId, newRole);
        });
        
        assertEquals("Користувача не знайдено", exception.getMessage());
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
        verify(userDao, never()).update(any(User.class));
        verify(logDao, never()).save(any(Log.class));
    }
    
    @Test
    void findById_Success() {
        // Arrange
        Long userId = 1L;
        User expectedUser = new User("testuser", "test@example.com", "hashedPassword");
        expectedUser.setId(userId);
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(expectedUser));
        
        // Act
        User result = userService.findById(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void findById_UserNotFound() {
        // Arrange
        Long userId = 1L;
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.findById(userId);
        });
        
        assertEquals("Користувача не знайдено", exception.getMessage());
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void findAll_Success() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(
            new User("user1", "user1@example.com", "password1"),
            new User("user2", "user2@example.com", "password2")
        );
        expectedUsers.get(0).setId(1L);
        expectedUsers.get(1).setId(2L);
        
        // Мокуємо поведінку userDao
        when(userDao.findAll()).thenReturn(expectedUsers);
        
        // Act
        List<User> result = userService.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Перевіряємо виклики методів
        verify(userDao).findAll();
    }
    
    @Test
    void delete_Success() {
        // Arrange
        Long userId = 1L;
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            // Мокуємо логування
            mockedFileLogger.when(() -> FileLogger.logAction(eq(userId), anyString()))
                .then(invocation -> null);
            
            // Act
            userService.delete(userId);
            
            // Перевіряємо виклики методів
            verify(userDao).delete(userId);
            verify(logDao).save(any(Log.class));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(userId), anyString()));
        }
    }
    
    @Test
    void isAdmin_True() {
        // Arrange
        Long userId = 1L;
        User adminUser = new User("admin", "admin@example.com", "password");
        adminUser.setId(userId);
        adminUser.setRole("admin");
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(adminUser));
        
        // Act
        boolean result = userService.isAdmin(userId);
        
        // Assert
        assertTrue(result);
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void isAdmin_False() {
        // Arrange
        Long userId = 1L;
        User regularUser = new User("user", "user@example.com", "password");
        regularUser.setId(userId);
        regularUser.setRole("user");
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(regularUser));
        
        // Act
        boolean result = userService.isAdmin(userId);
        
        // Assert
        assertFalse(result);
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void isAdmin_UserNotFound() {
        // Arrange
        Long userId = 1L;
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.empty());
        
        // Act
        boolean result = userService.isAdmin(userId);
        
        // Assert
        assertFalse(result);
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void getBalance_Success() {
        // Arrange
        Long userId = 1L;
        double expectedBalance = 1000.0;
        
        User user = new User("testuser", "test@example.com", "hashedPassword");
        user.setId(userId);
        user.setBalance(expectedBalance);
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        
        // Act
        double result = userService.getBalance(userId);
        
        // Assert
        assertEquals(expectedBalance, result);
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void getBalance_UserNotFound() {
        // Arrange
        Long userId = 1L;
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.getBalance(userId);
        });
        
        assertEquals("Користувача не знайдено", exception.getMessage());
        
        // Перевіряємо виклики методів
        verify(userDao).findById(userId);
    }
    
    @Test
    void changePassword_Success() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String oldHashedPassword = "oldHashedPassword";
        String newHashedPassword = "newHashedPassword";
        
        User user = new User("testuser", "test@example.com", oldHashedPassword);
        user.setId(userId);
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        
        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            // Мокуємо перевірку пароля
            mockedPasswordHasher.when(() -> PasswordHasher.verifyPassword(eq(oldPassword), eq(oldHashedPassword)))
                .thenReturn(true);
            
            // Мокуємо хешування нового пароля
            mockedPasswordHasher.when(() -> PasswordHasher.hashPassword(eq(newPassword)))
                .thenReturn(newHashedPassword);
            
            // Act
            userService.changePassword(userId, oldPassword, newPassword);
            
            // Assert
            assertEquals(newHashedPassword, user.getPassword());
            
            // Перевіряємо виклики методів
            verify(userDao).findById(userId);
            verify(userDao).update(user);
            mockedPasswordHasher.verify(() -> PasswordHasher.verifyPassword(eq(oldPassword), eq(oldHashedPassword)));
            mockedPasswordHasher.verify(() -> PasswordHasher.hashPassword(eq(newPassword)));
        }
    }
    
    @Test
    void changePassword_InvalidOldPassword() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";
        String oldHashedPassword = "oldHashedPassword";
        
        User user = new User("testuser", "test@example.com", oldHashedPassword);
        user.setId(userId);
        
        // Мокуємо поведінку userDao
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        
        try (MockedStatic<PasswordHasher> mockedPasswordHasher = mockStatic(PasswordHasher.class)) {
            // Мокуємо перевірку пароля
            mockedPasswordHasher.when(() -> PasswordHasher.verifyPassword(eq(oldPassword), eq(oldHashedPassword)))
                .thenReturn(false); // Неправильний старий пароль
            
            // Act & Assert
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.changePassword(userId, oldPassword, newPassword);
            });
            
            assertEquals("Невірний старий пароль", exception.getMessage());
            
            // Перевіряємо виклики методів
            verify(userDao).findById(userId);
            verify(userDao, never()).update(any(User.class));
            mockedPasswordHasher.verify(() -> PasswordHasher.verifyPassword(eq(oldPassword), eq(oldHashedPassword)));
        }
    }
    
    @Test
    void transferMoney_Success() {
        // Arrange
        Long fromUserId = 1L;
        Long toUserId = 2L;
        double amount = 500.0;
        double fromInitialBalance = 1000.0;
        double toInitialBalance = 200.0;
        
        User fromUser = new User("user1", "user1@example.com", "password1");
        fromUser.setId(fromUserId);
        fromUser.setBalance(fromInitialBalance);
        
        User toUser = new User("user2", "user2@example.com", "password2");
        toUser.setId(toUserId);
        toUser.setBalance(toInitialBalance);
        
        // Мокуємо поведінку userDao
        when(userDao.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        when(userDao.findById(toUserId)).thenReturn(Optional.of(toUser));
        
        try (MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class);
             MockedStatic<DatabaseConnection> mockedDbConnection = mockStatic(DatabaseConnection.class)) {
            
            // Мокуємо логування
            mockedFileLogger.when(() -> FileLogger.logAction(anyLong(), anyString()))
                .then(invocation -> null);
            
            // Мокуємо транзакцію
            mockedDbConnection.when(() -> DatabaseConnection.isInTransaction()).thenReturn(false);
            mockedDbConnection.when(() -> DatabaseConnection.beginTransaction()).then(invocation -> null);
            mockedDbConnection.when(() -> DatabaseConnection.commitTransaction()).then(invocation -> null);
            
            // Act
            userService.transferMoney(fromUserId, toUserId, amount);
            
            // Assert
            assertEquals(fromInitialBalance - amount, fromUser.getBalance());
            assertEquals(toInitialBalance + amount, toUser.getBalance());
            
            // Перевіряємо виклики методів
            verify(userDao).update(fromUser);
            verify(userDao).update(toUser);
            verify(logDao, times(2)).save(any(Log.class));
            
            mockedDbConnection.verify(() -> DatabaseConnection.isInTransaction());
            mockedDbConnection.verify(() -> DatabaseConnection.beginTransaction());
            mockedDbConnection.verify(() -> DatabaseConnection.commitTransaction());
            
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(fromUserId), anyString()));
            mockedFileLogger.verify(() -> FileLogger.logAction(eq(toUserId), anyString()));
        }
    }
    
    @Test
    void transferMoney_InsufficientFunds() {
        // Arrange
        Long fromUserId = 1L;
        Long toUserId = 2L;
        double amount = 1500.0;
        double fromInitialBalance = 1000.0;
        
        User fromUser = new User("user1", "user1@example.com", "password1");
        fromUser.setId(fromUserId);
        fromUser.setBalance(fromInitialBalance);
        
        // Мокуємо поведінку userDao
        when(userDao.findById(fromUserId)).thenReturn(Optional.of(fromUser));
        
        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.transferMoney(fromUserId, toUserId, amount);
        });
        
        assertEquals("Недостатньо коштів на балансі", exception.getMessage());
        
        // Перевіряємо виклики методів
        verify(userDao).findById(fromUserId);
        verify(userDao, never()).update(any(User.class));
        verify(logDao, never()).save(any(Log.class));
    }
}