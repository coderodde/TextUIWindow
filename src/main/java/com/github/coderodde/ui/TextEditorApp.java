package com.github.coderodde.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 ()
 * @since 1.6 ()
 */
public class TextEditorApp extends Application {
    
    private static final int CHAR_GRID_WIDTH = 80;
    private static final int CHAR_GRID_HEIGHT = 48;
    private static final int FONT_SIZE = 17;
    private static final int CHAR_HORIZONTAL_DELIMITER_LENGTH = 3;
    private static final int SLEEP_MILLISECONDS = 400;
    private static final String HELLO_WORLD_STRING = "Hello, world! ";
    
    private final TextUIWindow window;
    private final HelloWorldThread helloWorldThread;

    public TextEditorApp() {
        this.window = new TextUIWindow(CHAR_GRID_WIDTH,
                                       CHAR_GRID_HEIGHT,
                                       FONT_SIZE,
                                       CHAR_HORIZONTAL_DELIMITER_LENGTH);
                                       
        this.helloWorldThread = new HelloWorldThread(window);
    }
    
    @Override
    public void stop() {
        helloWorldThread.requestExit();
        
        try {
            helloWorldThread.join();
        } catch (InterruptedException ex) {
            
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.getChildren().add(window);
        Scene scene = new Scene(root, 
                                window.getPreferredWidth(), 
                                window.getPreferredHeight(),
                                false,
                                SceneAntialiasing.BALANCED);
        
        window.setTitleBorderThickness((int) scene.getY());
        
        primaryStage.setScene(scene);
        helloWorldThread.start();
        
        window.setChar(window.getGridWidth() - 1, window.getGridHeight() - 1, '?');
        window.addTextUIWindowMouseListener(new TextEditorMouseListener());
        
        window.repaint();
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private static final class HelloWorldThread extends Thread {
        private final TextUIWindow window;
        private final char[] textChars = new char[HELLO_WORLD_STRING.length()];
        private boolean doRun = true;
        
        HelloWorldThread(TextUIWindow window) {
            this.window = window;
        }
        
        void requestExit() {
            doRun = false;
        }
        
        @Override
        public void run() {
            int xOffset = 
                    (window.getGridWidth() - HELLO_WORLD_STRING.length()) / 2;
            
            List<Character> characterList = 
                    new ArrayList<>(HELLO_WORLD_STRING.length());
            
            for (char ch : HELLO_WORLD_STRING.toCharArray()) {
                characterList.add(ch);
            }
            
            while (doRun) {
                try {
                    Thread.sleep(SLEEP_MILLISECONDS);
                } catch (InterruptedException ex) {
                    return;
                }
                
                String text = toString(characterList, textChars);
                window.printString(xOffset, 0, text);
                Character ch = characterList.remove(0);
                characterList.add(ch);
                window.repaint();
            }
        }
        
        private static String toString(List<Character> charList,
                                       char[] chars) {
            for (int i = 0; i < chars.length; i++) {
                chars[i] = charList.get(i);
            }
            
            return new String(chars);
        }
    }
    
    private static final class TextEditorMouseListener 
            implements TextUIWindowMouseListener {
        
        public void onMouseClick(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }
        
        public void onMouseEntered(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }

        public void onMouseExited(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }

        public void onMousePressed(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }

        public void onMouseReleased(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }

        public void onMouseMove(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }

        public void onMouseDragged(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
        }
        
        private void handle(MouseEvent event, int charX, int charY) {
            System.out.println("[" + charX + ", " + charY + "]");
        }
    }
}
