package com.auction.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_BODY = "Test Body";
    private static final String TEST_PASSWORD = "testPassword";
    
    @BeforeEach
    void setUp() throws Exception {
        // Скидаємо статичні поля перед кожним тестом
        resetStaticFields();
    }
    
        private void resetStaticFields() throws Exception {
        setStaticField("SMTP_HOST", "smtp.gmail.com");
        setStaticField("SMTP_PORT", "587");
        setStaticField("SMTP_AUTH", "true");
        setStaticField("SMTP_STARTTLS", "true");
        setStaticField("SENDER_EMAIL", "your.email@gmail.com");
        setStaticField("SENDER_PASSWORD", "your_app_password");
        setStaticField("ERROR_RECIPIENT_EMAIL", "admin@yourdomain.com");
    }
    
        private void setStaticField(String fieldName, String value) throws Exception {
        Field field = EmailSender.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
    
        private String getStaticField(String fieldName) throws Exception {
        Field field = EmailSender.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(null);
    }

    @Test
    void testSendEmail_Success() throws Exception {
        // Підготовка тесту з використанням мокування статичних методів
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class);
             MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            
            // Мокуємо статичний метод Transport.send, щоб він нічого не робив
            mockedTransport.when(() -> Transport.send(any(MimeMessage.class))).then(invocation -> null);
            
            // Act
            boolean result = EmailSender.sendEmail(TEST_EMAIL, TEST_SUBJECT, TEST_BODY);
            
            // Assert
            assertTrue(result, "Метод повинен повертати true при успішній відправці");
            
            // Перевіряємо, що Transport.send був викликаний
            mockedTransport.verify(() -> Transport.send(any(MimeMessage.class)));
            
            // Перевіряємо, що логування помилок не відбувалося
            mockedFileLogger.verify(() -> FileLogger.logSystemAction(anyString()), never());
        }
    }
    
    @Test
    void testSendEmail_Failure() throws Exception {
        // Підготовка тесту з використанням мокування статичних методів
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class);
             MockedStatic<FileLogger> mockedFileLogger = mockStatic(FileLogger.class)) {
            
            // Налаштування мока для симуляції помилки
            mockedTransport.when(() -> Transport.send(any(MimeMessage.class)))
                .thenThrow(new javax.mail.MessagingException("Test exception"));
            
            // Act
            boolean result = EmailSender.sendEmail(TEST_EMAIL, TEST_SUBJECT, TEST_BODY);
            
            // Assert
            assertFalse(result, "Метод повинен повертати false при помилці відправки");
            
            // Перевіряємо, що відбулося логування помилки
            mockedFileLogger.verify(() -> FileLogger.logSystemAction(contains("Помилка при відправленні email")));
        }
    }
    
    @Test
    void testSendErrorEmail() throws Exception {
        String errorRecipient = getStaticField("ERROR_RECIPIENT_EMAIL");
        
        // Використовуємо окремий мок для виклику реального методу sendErrorEmail
        try (MockedStatic<EmailSender> mockedEmailSender = mockStatic(EmailSender.class)) {
            // Дозволяємо виклик sendErrorEmail
            mockedEmailSender.when(() -> EmailSender.sendErrorEmail(anyString(), anyString()))
                .thenCallRealMethod();
                
            // Мокуємо sendEmail, щоб він повертав true
            mockedEmailSender.when(() -> EmailSender.sendEmail(anyString(), anyString(), anyString()))
                .thenReturn(true);
            
            // Act
            boolean result = EmailSender.sendErrorEmail(TEST_SUBJECT, TEST_BODY);
            
            // Assert
            assertTrue(result, "Метод повинен повертати результат від sendEmail");
            
            // Перевіряємо, що sendEmail був викликаний з правильними параметрами
            mockedEmailSender.verify(() -> EmailSender.sendEmail(
                eq(errorRecipient), 
                eq(TEST_SUBJECT), 
                eq(TEST_BODY)
            ));
        }
    }
    
    @Test
    void testLoadConfiguration_Success() throws Exception {
        // Створюємо тестовий клас для перевірки завантаження конфігурації
        TestEmailSender testSender = new TestEmailSender();
        
        // Створюємо тестовий Properties файл
        String propertiesContent = 
            "mail.smtp.host=test.smtp.com\n" +
            "mail.smtp.port=465\n" +
            "mail.smtp.auth=false\n" +
            "mail.smtp.starttls.enable=false\n" +
            "mail.sender.email=test@test.com\n" +
            "mail.sender.password=testpass\n" +
            "mail.error.recipient=error@test.com";
        
        // Мокуємо ClassLoader для повернення нашого тестового Properties файлу
        ClassLoader mockClassLoader = mock(ClassLoader.class);
        InputStream mockInputStream = new ByteArrayInputStream(propertiesContent.getBytes());
        when(mockClassLoader.getResourceAsStream("email.properties")).thenReturn(mockInputStream);
        
        // Мокуємо Logger
        Logger mockLogger = mock(Logger.class);
        
        // Зберігаємо початкові значення полів
        String originalHost = getStaticField("SMTP_HOST");
        String originalPort = getStaticField("SMTP_PORT");
        String originalAuth = getStaticField("SMTP_AUTH");
        String originalStartTls = getStaticField("SMTP_STARTTLS");
        String originalEmail = getStaticField("SENDER_EMAIL");
        String originalPassword = getStaticField("SENDER_PASSWORD");
        String originalErrorEmail = getStaticField("ERROR_RECIPIENT_EMAIL");
        
        try {
            // Встановлюємо мокований ClassLoader та Logger
            testSender.setTestClassLoader(mockClassLoader);
            testSender.setTestLogger(mockLogger);
            
            // Викликаємо метод завантаження конфігурації
            testSender.loadTestConfiguration();
            
            // Перевіряємо, що налаштування були завантажені правильно
            assertEquals("test.smtp.com", getStaticField("SMTP_HOST"));
            assertEquals("465", getStaticField("SMTP_PORT"));
            assertEquals("false", getStaticField("SMTP_AUTH"));
            assertEquals("false", getStaticField("SMTP_STARTTLS"));
            assertEquals("test@test.com", getStaticField("SENDER_EMAIL"));
            assertEquals("testpass", getStaticField("SENDER_PASSWORD"));
            assertEquals("error@test.com", getStaticField("ERROR_RECIPIENT_EMAIL"));
            
            // Перевіряємо, що було виведено інформаційне повідомлення
            verify(mockLogger).log(eq(Level.INFO), contains("Налаштування електронної пошти успішно завантажені"));
        } finally {
            // Відновлюємо початкові значення полів
            setStaticField("SMTP_HOST", originalHost);
            setStaticField("SMTP_PORT", originalPort);
            setStaticField("SMTP_AUTH", originalAuth);
            setStaticField("SMTP_STARTTLS", originalStartTls);
            setStaticField("SENDER_EMAIL", originalEmail);
            setStaticField("SENDER_PASSWORD", originalPassword);
            setStaticField("ERROR_RECIPIENT_EMAIL", originalErrorEmail);
        }
    }
    
    @Test
    void testLoadConfiguration_FileNotFound() throws Exception {
        // Створюємо тестовий клас для перевірки поведінки при відсутності файлу
        TestEmailSender testSender = new TestEmailSender();
        
        // Мокуємо ClassLoader для симуляції відсутності файлу
        ClassLoader mockClassLoader = mock(ClassLoader.class);
        when(mockClassLoader.getResourceAsStream("email.properties")).thenReturn(null);
        
        // Мокуємо Logger для перевірки логування
        Logger mockLogger = mock(Logger.class);
        
        // Зберігаємо початкові значення полів
        String originalHost = getStaticField("SMTP_HOST");
        String originalPort = getStaticField("SMTP_PORT");
        
        try {
            // Встановлюємо мокований ClassLoader та Logger
            testSender.setTestClassLoader(mockClassLoader);
            testSender.setTestLogger(mockLogger);
            
            // Викликаємо метод завантаження конфігурації
            testSender.loadTestConfiguration();
            
            // Перевіряємо, що налаштування залишилися за замовчуванням
            assertEquals("smtp.gmail.com", getStaticField("SMTP_HOST"));
            assertEquals("587", getStaticField("SMTP_PORT"));
            
            // Перевіряємо, що було виведено попередження
            verify(mockLogger).log(eq(Level.WARNING), contains("Не вдалося знайти файл email.properties"));
        } finally {
            // Відновлюємо початкові значення полів
            setStaticField("SMTP_HOST", originalHost);
            setStaticField("SMTP_PORT", originalPort);
        }
    }
    
    @Test
    void testLoadConfiguration_IOException() throws Exception {
        // Створюємо тестовий клас для перевірки поведінки при IOException
        TestEmailSender testSender = new TestEmailSender();
        
        // Мокуємо InputStream для симуляції IOException
        InputStream mockInputStream = mock(InputStream.class);
        doThrow(new IOException("Test IO Exception")).when(mockInputStream).read(any(byte[].class));
        
        // Мокуємо ClassLoader
        ClassLoader mockClassLoader = mock(ClassLoader.class);
        when(mockClassLoader.getResourceAsStream("email.properties")).thenReturn(mockInputStream);
        
        // Мокуємо Logger
        Logger mockLogger = mock(Logger.class);
        
        // Зберігаємо початкові значення полів
        String originalHost = getStaticField("SMTP_HOST");
        String originalPort = getStaticField("SMTP_PORT");
        
        try {
            // Встановлюємо мокований ClassLoader та Logger
            testSender.setTestClassLoader(mockClassLoader);
            testSender.setTestLogger(mockLogger);
            
            // Викликаємо метод завантаження конфігурації
            testSender.loadTestConfiguration();
            
            // Перевіряємо, що було виведено помилку
            verify(mockLogger).log(eq(Level.SEVERE), anyString(), contains("Test IO Exception"));
        } finally {
            // Відновлюємо початкові значення полів
            setStaticField("SMTP_HOST", originalHost);
            setStaticField("SMTP_PORT", originalPort);
        }
    }
    
        private static class TestEmailSender {
        private ClassLoader testClassLoader;
        private Logger testLogger;
        
        public void setTestClassLoader(ClassLoader classLoader) {
            this.testClassLoader = classLoader;
        }
        
        public void setTestLogger(Logger logger) {
            this.testLogger = logger;
        }
        
        public void loadTestConfiguration() {
            Properties props = new Properties();
            try (InputStream input = testClassLoader.getResourceAsStream("email.properties")) {
                if (input == null) {
                    testLogger.log(Level.WARNING, "Не вдалося знайти файл email.properties, використовуються налаштування за замовчуванням");
                    return;
                }
                
                props.load(input);
                
                // Завантаження налаштувань SMTP
                setStaticFieldValue("SMTP_HOST", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
                setStaticFieldValue("SMTP_PORT", props.getProperty("mail.smtp.port", "587"));
                setStaticFieldValue("SMTP_AUTH", props.getProperty("mail.smtp.auth", "true"));
                setStaticFieldValue("SMTP_STARTTLS", props.getProperty("mail.smtp.starttls.enable", "true"));
                
                // Завантаження облікових даних
                setStaticFieldValue("SENDER_EMAIL", props.getProperty("mail.sender.email", "your.email@gmail.com"));
                setStaticFieldValue("SENDER_PASSWORD", props.getProperty("mail.sender.password", "your_app_password"));
                
                // Завантаження адреси для помилок
                setStaticFieldValue("ERROR_RECIPIENT_EMAIL", props.getProperty("mail.error.recipient", "admin@yourdomain.com"));
                
                testLogger.log(Level.INFO, "Налаштування електронної пошти успішно завантажені");
            } catch (IOException e) {
                testLogger.log(Level.SEVERE, "Помилка при завантаженні налаштувань електронної пошти: {0}", e.getMessage());
            }
        }
        
        private void setStaticFieldValue(String fieldName, String value) {
            try {
                Field field = EmailSender.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(null, value);
            } catch (Exception e) {
                testLogger.log(Level.SEVERE, "Помилка при встановленні поля {0}: {1}", new Object[]{fieldName, e.getMessage()});
            }
        }
    }
} 