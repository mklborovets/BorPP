package com.auction.service;

import java.sql.SQLException;
import java.util.List;

import com.auction.dao.LogDao;
import com.auction.dao.UserDao;
import com.auction.exception.ServiceException;
import com.auction.model.Log;
import com.auction.model.User;
import com.auction.util.DatabaseConnection;
import com.auction.util.FileLogger;
import com.auction.util.PasswordHasher;
import com.auction.util.ValidationUtils;

public class UserService {
    private final UserDao userDao;
    private final LogDao logDao;
    
    public UserService() {
        this.userDao = new UserDao();
        this.logDao = new LogDao();
    }
    
    public User register(String username, String password, String email) {
        // Валідація даних
        String validationError = ValidationUtils.validateUser(username, password, email);
        if (validationError != null) {
            throw new ServiceException(validationError);
        }
        
        // Перевірка чи користувач вже існує
        if (userDao.findByUsername(username).isPresent()) {
            throw new ServiceException("Користувач з таким іменем вже існує");
        }
        
        // Створення нового користувача
        User user = new User(username, PasswordHasher.hashPassword(password), email);
        user = userDao.save(user);
        
        // Логування
        String logMessage = "Зареєстровано нового користувача";
        FileLogger.logAction(user.getId(), logMessage);
        logDao.save(new Log(user.getId(), logMessage));
        
        return user;
    }
    
    public User authenticate(String username, String password) {
        User user = userDao.findByUsername(username)
            .orElseThrow(() -> new ServiceException("Невірне ім'я користувача або пароль"));
            
        if (!PasswordHasher.verifyPassword(password, user.getPassword())) {
            throw new ServiceException("Невірне ім'я користувача або пароль");
        }
        
        String logMessage = "Вхід в систему";
        FileLogger.logAction(user.getId(), logMessage);
        logDao.save(new Log(user.getId(), logMessage));
        return user;
    }
    
    public void updateBalance(Long userId, double amount) {
        if (!ValidationUtils.isValidAmount(amount)) {
            throw new ServiceException("Невірна сума");
        }
        
        User user = userDao.findById(userId)
            .orElseThrow(() -> new ServiceException("Користувача не знайдено"));
        
        double newBalance = user.getBalance() + amount;
        if (newBalance < 0) {
            throw new ServiceException("Недостатньо коштів на балансі");
        }
        
        user.setBalance(newBalance);
        userDao.update(user);
        
        String logMessage = "Зміна балансу на " + amount;
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
    }
    
    public void changeRole(Long userId, String newRole) {
        if (!newRole.equals("user") && !newRole.equals("admin")) {
            throw new ServiceException("Невірна роль");
        }
        
        User user = userDao.findById(userId)
            .orElseThrow(() -> new ServiceException("Користувача не знайдено"));
        
        user.setRole(newRole);
        userDao.update(user);
        
        String logMessage = "Зміна ролі на " + newRole;
        FileLogger.logAction(userId, logMessage);
        logDao.save(new Log(userId, logMessage));
    }
    
    public User findById(Long id) {
        return userDao.findById(id)
            .orElseThrow(() -> new ServiceException("Користувача не знайдено"));
    }
    
    public List<User> findAll() {
        return userDao.findAll();
    }
    
    public void delete(Long id) {
        userDao.delete(id);
        String logMessage = "Видалено користувача";
        FileLogger.logAction(id, logMessage);
        logDao.save(new Log(id, logMessage));
    }
    
    public boolean isAdmin(Long userId) {
        return userDao.findById(userId)
            .map(user -> "admin".equals(user.getRole()))
            .orElse(false);
    }
    
    public double getBalance(Long userId) {
        return userDao.findById(userId)
            .map(User::getBalance)
            .orElseThrow(() -> new ServiceException("Користувача не знайдено"));
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        
        if (!PasswordHasher.verifyPassword(oldPassword, user.getPassword())) {
            throw new ServiceException("Невірний старий пароль");
        }
        
        user.setPassword(PasswordHasher.hashPassword(newPassword));
        userDao.update(user);
    }

    public void transferMoney(Long fromUserId, Long toUserId, double amount) {
        // Перевіряємо баланс відправника
        User fromUser = findById(fromUserId);
        if (fromUser.getBalance() < amount) {
            throw new ServiceException("Недостатньо коштів на балансі");
        }
        
        // Отримуємо отримувача
        User toUser = findById(toUserId);
        
        // Виконуємо переказ
        fromUser.setBalance(fromUser.getBalance() - amount);
        toUser.setBalance(toUser.getBalance() + amount);
        
        // Зберігаємо зміни
        try {
            // Перевіряємо, чи вже є активна транзакція
            boolean wasInTransaction = false;
            try {
                wasInTransaction = DatabaseConnection.isInTransaction();
                if (!wasInTransaction) {
                    DatabaseConnection.beginTransaction();
                }
            } catch (SQLException e) {
                throw new ServiceException("Помилка при перевірці статусу транзакції: " + e.getMessage(), e);
            }
            
            userDao.update(fromUser);
            userDao.update(toUser);
            
            // Логуємо транзакцію
            String fromLogMessage = "Переказ коштів користувачу " + toUser.getUsername() + " у розмірі " + amount;
            FileLogger.logAction(fromUserId, fromLogMessage);
            logDao.save(new Log(fromUserId, fromLogMessage));
            
            String toLogMessage = "Отримано кошти від користувача " + fromUser.getUsername() + " у розмірі " + amount;
            FileLogger.logAction(toUserId, toLogMessage);
            logDao.save(new Log(toUserId, toLogMessage));
            
            // Якщо ми почали нову транзакцію, то завершуємо її
            if (!wasInTransaction) {
                DatabaseConnection.commitTransaction();
            }
        } catch (Exception e) {
            try {
                // Відкат транзакції, якщо сталася помилка
                DatabaseConnection.rollbackTransaction();
            } catch (SQLException rollbackEx) {
                System.err.println("Помилка при відкаті транзакції: " + rollbackEx.getMessage());
            }
            throw new ServiceException("Помилка при переказі коштів: " + e.getMessage(), e);
        }
    }
} 