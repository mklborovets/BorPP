package com.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppTest {
    
    private Stage stage;
    private Scene scene;
    
        @Start
    private void start(Stage stage) throws Exception {
        this.stage = stage;
        App app = new App();
        app.start(stage);
        scene = stage.getScene();
    }
    
        @Test
    void testAppInitialization(FxRobot robot) {
        // Перевірка, що вікно має правильний заголовок
        assertEquals("Аукціон транспортних засобів", stage.getTitle());
        
        // Перевірка, що сцена була створена
        assertNotNull(scene, "Сцена повинна бути створена");
        
        // Перевірка, що кореневий елемент не є null
        assertNotNull(scene.getRoot(), "Кореневий елемент не повинен бути null");
    }
    
        @Test
    void testLoginFormElements(FxRobot robot) {
        // Перевірка наявності полів вводу для логіну та паролю
        // Використовуємо правильні ідентифікатори з FXML файлу
        assertTrue(robot.lookup("#usernameField").tryQuery().isPresent(), "Поле логіну повинно бути присутнє");
        assertTrue(robot.lookup("#passwordField").tryQuery().isPresent(), "Поле паролю повинно бути присутнє");
        assertTrue(robot.lookup(".button").tryQuery().isPresent(), "Кнопка входу повинна бути присутня");
    }
    
        @Test
    void testLoginFormInteraction(FxRobot robot) {
        // Введення даних у поля
        robot.clickOn("#usernameField").write("testuser");
        robot.clickOn("#passwordField").write("password");
        
        // Перевірка, що дані були введені
        assertEquals("testuser", robot.lookup("#usernameField").queryTextInputControl().getText());
        assertEquals("password", robot.lookup("#passwordField").queryTextInputControl().getText());
    }
    
        private void runInFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
