package com.auction.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String errorMessage = "Тестова помилка сервісу";
        
        // Act
        ServiceException exception = new ServiceException(errorMessage);
        
        // Assert
        assertEquals(errorMessage, exception.getMessage(), "Повідомлення про помилку має відповідати переданому");
        assertNull(exception.getCause(), "Причина помилки має бути null");
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        // Arrange
        String errorMessage = "Тестова помилка сервісу";
        Throwable cause = new RuntimeException("Причина помилки");
        
        // Act
        ServiceException exception = new ServiceException(errorMessage, cause);
        
        // Assert
        assertEquals(errorMessage, exception.getMessage(), "Повідомлення про помилку має відповідати переданому");
        assertEquals(cause, exception.getCause(), "Причина помилки має відповідати переданій");
    }
    
    @Test
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        ServiceException exception = new ServiceException("Тест");
        
        // Assert
        assertTrue(exception instanceof RuntimeException, "ServiceException має бути підкласом RuntimeException");
    }
    
    @Test
    void testExceptionCanBeThrownWithoutTryCatch() {
        // Цей тест перевіряє, що ServiceException є unchecked exception
        // і може бути кинутий без явного try-catch блоку
        
        // Arrange
        String errorMessage = "Тестова помилка";
        
        // Act & Assert
        assertThrows(ServiceException.class, () -> {
            throwServiceException(errorMessage);
        }, "ServiceException має бути unchecked exception");
    }
    
    private void throwServiceException(String message) {
        throw new ServiceException(message);
    }
}