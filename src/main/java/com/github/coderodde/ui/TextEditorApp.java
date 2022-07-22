package com.github.coderodde.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 ()
 * @since 1.6 ()
 */
public class TextEditorApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TextUIWindow window = new TextUIWindow(80, 48, 18, 0);
        StackPane root = new StackPane();
        root.getChildren().add(window);
        Scene scene = new Scene(root, 
                                window.getPreferredWidth(), 
                                window.getPreferredHeight(),
                                false,
                                SceneAntialiasing.BALANCED);
        
        window.setTitleBorderThickness((int) scene.getY());
        
        primaryStage.setScene(scene);
        
        window.setForegroundColor(0, 0, Color.RED);
        window.setForegroundColor(1, 0, Color.YELLOW);
        window.setForegroundColor(2, 0, Color.GREEN);
        
        window.setChar(0, 0, 'H');
        window.setChar(1, 0, 'W');
        window.setChar(2, 0, '!');
        window.setChar(1, 1, 'X');
        
        primaryStage.show();
        
        window.repaint();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
