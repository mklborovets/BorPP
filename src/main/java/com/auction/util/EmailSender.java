package com.auction.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Утилітний клас для відправлення електронних листів
 */
public class EmailSender {
    private static final Logger logger = Logger.getLogger(EmailSender.class.getName());
    
    // Налаштування за замовчуванням
    private static String SMTP_HOST = "smtp.gmail.com";
    private static String SMTP_PORT = "587";
    private static String SMTP_AUTH = "true";
    private static String SMTP_STARTTLS = "true";
    
    // Облікові дані відправника
    private static String SENDER_EMAIL = "your.email@gmail.com";
    private static String SENDER_PASSWORD = "your_app_password";
    
    // Адреса для отримання критичних помилок
    private static String ERROR_RECIPIENT_EMAIL = "admin@yourdomain.com";
    
    // Завантаження налаштувань з файлу конфігурації
    static {
        loadConfiguration();
    }
    
    /**
     * Відправляє повідомлення про критичну помилку на адресу адміністратора
     * @param subject тема повідомлення
     * @param errorMessage текст повідомлення про помилку
     * @return true, якщо повідомлення успішно відправлено
     */
    public static boolean sendErrorEmail(String subject, String errorMessage) {
        return sendEmail(ERROR_RECIPIENT_EMAIL, subject, errorMessage);
    }
    
    /**
     * Завантажує налаштування з файлу конфігурації
     */
    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = EmailSender.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                logger.log(Level.WARNING, "Не вдалося знайти файл email.properties, використовуються налаштування за замовчуванням");
                return;
            }
            
            props.load(input);
            
            // Завантаження налаштувань SMTP
            SMTP_HOST = props.getProperty("mail.smtp.host", SMTP_HOST);
            SMTP_PORT = props.getProperty("mail.smtp.port", SMTP_PORT);
            SMTP_AUTH = props.getProperty("mail.smtp.auth", SMTP_AUTH);
            SMTP_STARTTLS = props.getProperty("mail.smtp.starttls.enable", SMTP_STARTTLS);
            
            // Завантаження облікових даних
            SENDER_EMAIL = props.getProperty("mail.sender.email", SENDER_EMAIL);
            SENDER_PASSWORD = props.getProperty("mail.sender.password", SENDER_PASSWORD);
            
            // Завантаження адреси для помилок
            ERROR_RECIPIENT_EMAIL = props.getProperty("mail.error.recipient", ERROR_RECIPIENT_EMAIL);
            
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Помилка при завантаженні налаштувань електронної пошти: {0}", e.getMessage());
        }
    }
    
    /**
     * Відправляє електронний лист на вказану адресу
     * @param recipient адреса отримувача
     * @param subject тема повідомлення
     * @param body текст повідомлення
     * @return true, якщо повідомлення успішно відправлено
     */
    public static boolean sendEmail(String recipient, String subject, String body) {
        // Налаштування властивостей для підключення до SMTP-сервера
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", SMTP_AUTH);
        props.put("mail.smtp.starttls.enable", SMTP_STARTTLS);
        
        // Створення сесії з автентифікацією
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
        
        try {
            // Створення повідомлення
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);
            
            // Відправлення повідомлення
            Transport.send(message);
            
            
            return true;
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Помилка при відправленні електронного листа: {0}", e.getMessage());
            // Логуємо помилку у файл, оскільки відправка email не вдалася
            FileLogger.logSystemAction("Помилка при відправленні email: " + e.getMessage());
            return false;
        }
    }
}
