package com.auction.util;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Клас для управління іконками додатку
 */
public class IconManager {
    
    private static final Logger logger = Logger.getLogger(IconManager.class.getName());
    private static final String APP_ICON_PATH = "/images/pngwing.com.png";
    
    private static Image appIcon;
    
    /**
     * Завантажує іконку додатку
     * @return Image об'єкт з іконкою додатку або null, якщо іконка не знайдена
     */
    public static Image getAppIcon() {
        if (appIcon == null) {
            try {
                InputStream iconStream = IconManager.class.getResourceAsStream(APP_ICON_PATH);
                if (iconStream != null) {
                    appIcon = new Image(iconStream);
                    
                } else {
                    logger.warning("Файл іконки додатку не знайдено: " + APP_ICON_PATH);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Помилка завантаження іконки додатку", e);
            }
        }
        return appIcon;
    }
    
    /**
     * Встановлює іконку додатку для вказаного вікна
     * @param stage вікно, для якого потрібно встановити іконку
     */
    public static void setAppIcon(Stage stage) {
        if (stage != null) {
            Image icon = getAppIcon();
            if (icon != null) {
                stage.getIcons().clear(); // Очищаємо попередні іконки
                stage.getIcons().add(icon);
                
            }
        }
    }
} 