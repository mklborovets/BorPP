package com.auction.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void isValidEmail() {
        // Валідні email
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.com"));
        
        // Невалідні email
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail("@example.com"));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    void isValidUsername() {
        // Валідні імена користувачів
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("User_123"));
        
        // Невалідні імена користувачів
        assertFalse(ValidationUtils.isValidUsername("ab")); // Занадто коротке
        assertFalse(ValidationUtils.isValidUsername("user name")); // Містить пробіл
        assertFalse(ValidationUtils.isValidUsername("user@name")); // Містить спецсимвол
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    @Test
    void isValidPassword() {
        // Валідні паролі
        assertTrue(ValidationUtils.isValidPassword("Password1@"));
        assertTrue(ValidationUtils.isValidPassword("StrongPass123#"));
        
        // Невалідні паролі
        assertFalse(ValidationUtils.isValidPassword("weakpass")); // Немає цифр і спецсимволів
        assertFalse(ValidationUtils.isValidPassword("ONLYUPPERCASE123@")); // Немає малих літер
        assertFalse(ValidationUtils.isValidPassword("onlylowercase123@")); // Немає великих літер
        assertFalse(ValidationUtils.isValidPassword("NoDigits@")); // Немає цифр
        assertFalse(ValidationUtils.isValidPassword("NoSpecial123")); // Немає спецсимволів
        assertFalse(ValidationUtils.isValidPassword("Short1@")); // Занадто короткий
        assertFalse(ValidationUtils.isValidPassword(null));
    }

    @Test
    void isValidAmount() {
        // Валідні суми
        assertTrue(ValidationUtils.isValidAmount(100.0));
        assertTrue(ValidationUtils.isValidAmount(0.01));
        assertTrue(ValidationUtils.isValidAmount(1000000000.0));
        
        // Невалідні суми
        assertFalse(ValidationUtils.isValidAmount(0.0));
        assertFalse(ValidationUtils.isValidAmount(-100.0));
        assertFalse(ValidationUtils.isValidAmount(1000000001.0));
    }

    @Test
    void isValidVin() {
        // Валідні VIN
        assertTrue(ValidationUtils.isValidVin("1HGCM82633A123456"));
        assertTrue(ValidationUtils.isValidVin("WVWZZZ1JZ3W123456"));
        
        // Невалідні VIN
        assertFalse(ValidationUtils.isValidVin("ABC123")); // Занадто короткий
        assertFalse(ValidationUtils.isValidVin("1HG=M82633A12345X")); // Містить недопустимий символ
        
        // Перевірка null
        boolean result = ValidationUtils.isValidVin(null);
        assertFalse(result, "isValidVin(null) повинен повертати false");
    }

    @Test
    void isValidYear() {
        // Валідні роки
        assertTrue(ValidationUtils.isValidYear(2023));
        assertTrue(ValidationUtils.isValidYear(2000));
        assertTrue(ValidationUtils.isValidYear(1900));
        
        // Невалідні роки
        assertFalse(ValidationUtils.isValidYear(1899)); // Занадто рано
        assertFalse(ValidationUtils.isValidYear(LocalDateTime.now().getYear() + 1)); // Майбутнє
    }

    @Test
    void isValidMileage() {
        // Валідний пробіг
        assertTrue(ValidationUtils.isValidMileage(0));
        assertTrue(ValidationUtils.isValidMileage(100000));
        assertTrue(ValidationUtils.isValidMileage(1000000));
        assertTrue(ValidationUtils.isValidMileage(null)); // null також валідний
        
        // Невалідний пробіг
        assertFalse(ValidationUtils.isValidMileage(-1));
        assertFalse(ValidationUtils.isValidMileage(1000001));
    }

    @Test
    void isValidUrl() {
        // Валідні URL
        assertTrue(ValidationUtils.isValidUrl("http://example.com"));
        assertTrue(ValidationUtils.isValidUrl("https://example.com/path"));
        assertTrue(ValidationUtils.isValidUrl("example.com"));
        assertTrue(ValidationUtils.isValidUrl(null)); // null також валідний
        
        // Невалідні URL
        assertFalse(ValidationUtils.isValidUrl("invalid url with spaces"));
    }

    @Test
    void validateUser() {
        // Валідний користувач
        assertNull(ValidationUtils.validateUser("validUser", "ValidPass1@", "user@example.com"));
        
        // Невалідне ім'я користувача
        String resultInvalidUsername = ValidationUtils.validateUser("u", "ValidPass1@", "user@example.com");
        assertNotNull(resultInvalidUsername);
        assertTrue(resultInvalidUsername.contains("Ім'я користувача"));
        
        // Невалідний пароль
        String resultInvalidPassword = ValidationUtils.validateUser("validUser", "weakpass", "user@example.com");
        assertNotNull(resultInvalidPassword);
        assertTrue(resultInvalidPassword.contains("Пароль"));
        
        // Невалідний email
        String resultInvalidEmail = ValidationUtils.validateUser("validUser", "ValidPass1@", "invalid-email");
        assertNotNull(resultInvalidEmail);
        assertTrue(resultInvalidEmail.contains("email"));
    }

    @Test
    void validateVehicle() {
        // Валідний транспортний засіб
        assertNull(ValidationUtils.validateVehicle("Toyota", "Camry", 2020, "1HGCM82633A123456", 50000, null, null));
        
        // Відсутня марка
        String resultMissingBrand = ValidationUtils.validateVehicle("", "Camry", 2020, "1HGCM82633A123456", 50000, null, null);
        assertNotNull(resultMissingBrand);
        assertTrue(resultMissingBrand.contains("Марка"));
        
        // Відсутня модель
        String resultMissingModel = ValidationUtils.validateVehicle("Toyota", "", 2020, "1HGCM82633A123456", 50000, null, null);
        assertNotNull(resultMissingModel);
        assertTrue(resultMissingModel.contains("Модель"));
        
        // Невалідний рік
        String resultInvalidYear = ValidationUtils.validateVehicle("Toyota", "Camry", 1800, "1HGCM82633A123456", 50000, null, null);
        assertNotNull(resultInvalidYear);
        assertTrue(resultInvalidYear.contains("рік"));
    }

    @Test
    void validateAuction() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.plusDays(7);
        
        // Валідний аукціон
        assertNull(ValidationUtils.validateAuction(1000.0, 100.0, start, end));
        
        // Невалідна початкова ціна
        String resultInvalidStartPrice = ValidationUtils.validateAuction(0.0, 100.0, start, end);
        assertNotNull(resultInvalidStartPrice);
        assertTrue(resultInvalidStartPrice.contains("Початкова ціна"));
        
        // Невалідний крок ціни
        String resultInvalidPriceStep = ValidationUtils.validateAuction(1000.0, 0.0, start, end);
        assertNotNull(resultInvalidPriceStep);
        assertTrue(resultInvalidPriceStep.contains("Крок ціни"));
        
        // Час початку в минулому
        String resultStartInPast = ValidationUtils.validateAuction(1000.0, 100.0, now.minusDays(1), end);
        assertNotNull(resultStartInPast);
        assertTrue(resultStartInPast.contains("минулому"));
        
        // Час початку після часу завершення
        String resultStartAfterEnd = ValidationUtils.validateAuction(1000.0, 100.0, end, start);
        assertNotNull(resultStartAfterEnd);
        assertTrue(resultStartAfterEnd.contains("не може бути пізніше"));
    }

    @Test
    void validateBid() {
        // Валідна ставка
        assertNull(ValidationUtils.validateBid(1200.0, 1000.0, 100.0));
        
        // Ставка менша за поточну ціну
        String resultBidTooLow = ValidationUtils.validateBid(900.0, 1000.0, 100.0);
        assertNotNull(resultBidTooLow);
        assertTrue(resultBidTooLow.contains("більше поточної ціни"));
        
        // Ставка менша за крок ціни
        String resultBidBelowStep = ValidationUtils.validateBid(1050.0, 1000.0, 100.0);
        assertNotNull(resultBidBelowStep);
        assertTrue(resultBidBelowStep.contains("плюс крок ставки"));
    }

    @Test
    void validatePassword() {
        // Валідний пароль
        assertNull(ValidationUtils.validatePassword("StrongPass1@"));
        
        // Занадто короткий пароль
        String resultTooShort = ValidationUtils.validatePassword("Short1@");
        assertNotNull(resultTooShort);
        assertTrue(resultTooShort.contains("мінімум 8 символів"));
        
        // Відсутня велика літера
        String resultNoUppercase = ValidationUtils.validatePassword("lowercase1@");
        assertNotNull(resultNoUppercase);
        assertTrue(resultNoUppercase.contains("велику літеру"));
        
        // Відсутня мала літера
        String resultNoLowercase = ValidationUtils.validatePassword("UPPERCASE1@");
        assertNotNull(resultNoLowercase);
        assertTrue(resultNoLowercase.contains("малу літеру"));
        
        // Відсутня цифра
        String resultNoDigit = ValidationUtils.validatePassword("NoDigits@");
        assertNotNull(resultNoDigit);
        assertTrue(resultNoDigit.contains("цифру"));
        
        // Відсутній спеціальний символ
        String resultNoSpecialChar = ValidationUtils.validatePassword("NoSpecial123");
        assertNotNull(resultNoSpecialChar);
        assertTrue(resultNoSpecialChar.contains("спеціальний символ"));
        
        // Містить пробіл
        String resultContainsSpace = ValidationUtils.validatePassword("Has Space1@");
        assertNotNull(resultContainsSpace);
        assertTrue(resultContainsSpace.contains("пробілів"));
    }
}