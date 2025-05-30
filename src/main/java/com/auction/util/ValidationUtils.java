package com.auction.util;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_]{3,50}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    
    private static final Pattern VIN_PATTERN = Pattern.compile(
        "^[A-HJ-NPR-Z0-9]{17}$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$"
    );
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static boolean isValidAmount(double amount) {
        return amount > 0 && amount <= 1000000000;
    }

    public static boolean isValidVin(String vin) {
        return vin != null && VIN_PATTERN.matcher(vin).matches();
    }

    public static boolean isValidYear(int year) {
        int currentYear = Year.now().getValue();
        return year >= 1900 && year <= currentYear;
    }

    public static boolean isValidMileage(Integer mileage) {
        return mileage == null || (mileage >= 0 && mileage <= 1000000);
    }

    public static boolean isValidUrl(String url) {
        return url == null || URL_PATTERN.matcher(url).matches();
    }
    
    public static String validateUser(String username, String password, String email) {
        if (!isValidUsername(username)) {
            return "Ім'я користувача повинно містити від 3 до 50 символів (літери, цифри та підкреслення)";
        }
        
        if (!isValidPassword(password)) {
            return "Пароль повинен містити мінімум 8 символів, включаючи великі та малі літери, " +
                   "цифри та спеціальні символи (@#$%^&+=)";
        }
        
        if (!isValidEmail(email)) {
            return "Невірний формат email";
        }
        
        return null;
    }

    public static String validateVehicle(String brand, String model, int year, String vin,
                                       Integer mileage, String photoUrl, String videoUrl) {
        if (brand == null || brand.trim().isEmpty()) {
            return "Марка транспортного засобу обов'язкова";
        }
        if (model == null || model.trim().isEmpty()) {
            return "Модель транспортного засобу обов'язкова";
        }
        if (year < 1900 || year > LocalDateTime.now().getYear()) {
            return "Недійсний рік випуску";
        }
        if (vin != null && !isValidVin(vin)) {
            return "Недійсний VIN-код";
        }
        if (mileage != null && mileage < 0) {
            return "Пробіг не може бути від'ємним";
        }
        return null;
    }

    public static String validateAuction(double startPrice, double priceStep,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        if (startPrice <= 0) {
            return "Початкова ціна повинна бути більше нуля";
        }
        if (priceStep <= 0) {
            return "Крок ціни повинен бути більше нуля";
        }
        if (startTime == null) {
            return "Час початку аукціону обов'язковий";
        }
        if (endTime == null) {
            return "Час завершення аукціону обов'язковий";
        }
        if (startTime.isAfter(endTime)) {
            return "Час початку не може бути пізніше часу завершення";
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            return "Час початку не може бути в минулому";
        }
        return null;
    }

    public static String validateBid(double amount, double currentPrice, Double priceStep) {
        if (amount <= currentPrice) {
            return "Ставка повинна бути більше поточної ціни";
        }
        if (priceStep != null && amount < currentPrice + priceStep) {
            return "Ставка має бути не меншою за поточну ціну плюс крок ставки";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Пароль повинен містити мінімум 8 символів";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "Пароль повинен містити хоча б одну велику літеру";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "Пароль повинен містити хоча б одну малу літеру";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "Пароль повинен містити хоча б одну цифру";
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            return "Пароль повинен містити хоча б один спеціальний символ";
        }
        
        if (password.contains(" ")) {
            return "Пароль не повинен містити пробілів";
        }
        
        return null; // пароль валідний
    }
} 