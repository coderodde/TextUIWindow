package com.github.coderodde.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
    
    private static final int CHAR_GRID_WIDTH = 40;
    private static final int CHAR_GRID_HEIGHT = 24;
    private static final int FONT_SIZE = 17;
    private static final int CHAR_HORIZONTAL_DELIMITER_LENGTH = 1;
    private static final int SLEEP_MILLISECONDS = 400;
    private static final String HELLO_WORLD_STRING = "Hello, world! ";
    
    private final TextUIWindow window;
    private final HelloWorldThread helloWorldThread;
    private final CursorBlinkThread cursorBlinkThread;
    
    private volatile int cursorX = 0;
    private volatile int cursorY = 2;
    private volatile int previousCursorX;
    private volatile int previousCursorY;
    private volatile Color previousForegroundColor;
    private volatile Color previousBackgroundColor;

    public TextEditorApp() {
        this.window = new TextUIWindow(CHAR_GRID_WIDTH,
                                       CHAR_GRID_HEIGHT,
                                       FONT_SIZE,
                                       CHAR_HORIZONTAL_DELIMITER_LENGTH);
                                       
        this.helloWorldThread = new HelloWorldThread();
        this.cursorBlinkThread = new CursorBlinkThread();
    }
    
    @Override
    public void stop() {
        helloWorldThread.requestExit();
        cursorBlinkThread.requestExit();
        
        try {
            helloWorldThread.join();
        } catch (InterruptedException ex) {
            
        }
        
        try {
            cursorBlinkThread.join();
        } catch (InterruptedException ex) {
            
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        Platform.runLater(() -> {
            
            try {
                StackPane root = new StackPane();
                root.getChildren().add(window);
                Scene scene = new Scene(root, 
                                        window.getPreferredWidth(), 
                                        window.getPreferredHeight(),
                                        false,
                                        SceneAntialiasing.BALANCED);

                window.setTitleBorderThickness((int) scene.getY());

                primaryStage.setScene(scene);
                window.addTextUIWindowMouseListener(new TextEditorMouseListener());
                window.addTextUIWindowKeyboardListener(
                        new TextEditorKeyboardListener());
                
                helloWorldThread.start();
                cursorBlinkThread.start();
                
                window.repaint();
                primaryStage.setResizable(false);
                primaryStage.show();
            } catch (Exception ex) {
                System.err.println("Something failed.");
                helloWorldThread.requestExit();
                cursorBlinkThread.requestExit();
                
//                try {
//                    helloWorldThread.join();
//                } catch (InterruptedException ex2) {
//                    
//                }
//                
//                try {
//                    cursorBlinkThread.join();
//                } catch (InterruptedException ex2) {
//                    
//                }
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private final class CursorBlinkThread extends Thread {
        
        private static final long CURSOR_BLINK_SLEEP = 600L;
        
        private volatile boolean doRun = true;
        
        @Override
        public void run() {
            while (doRun) {
                try {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(CURSOR_BLINK_SLEEP / 10);
                        
                        if (!doRun) {
                            return;
                        }
                    }
                } catch (InterruptedException ex) {
                    
                }
                
                window.toggleBlinkCursor(cursorX, cursorY);
                window.repaint();
            }
        }
        
        void requestExit() {
            doRun = false;
        }
    }
    
    private final class HelloWorldThread extends Thread {
        private final char[] textChars = new char[HELLO_WORLD_STRING.length()];
        private volatile boolean doRun = true;
        
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
    
    private final class TextEditorKeyboardListener 
            implements TextUIWindowKeyboardListener {
        
        @Override
        public void onKeyTyped(KeyEvent event) {
            System.out.println("yeah = " + event.getCharacter());
            switch (event.getCode()) {
                case UP:
                    moveCursorUp();
                    break;
                    
                case LEFT:
                    moveCursorLeft();
                    break;
                    
                case RIGHT:
                    moveCursorRight();
                    break;
                    
                case DOWN:
                    moveCursorDown();
                    break;
            }
            
            window.printString(cursorX, cursorY, event.getCharacter());
            
            Platform.runLater(() -> { window.repaint(); });
            event.consume();
        }
        
        private void moveCursorUp() {
            if (cursorY == 2) {
                return;
            }
            
            cursorY--;
            Platform.runLater(() -> { window.repaint(); });
        }
        
        private void moveCursorLeft() {
            if (cursorX == 0) {
                if (cursorY > 2) {
                    cursorY--;
                    cursorX = window.getGridWidth() - 1;
                }
            } else {
                cursorX--;
            }
            
            Platform.runLater(() -> { window.repaint(); });
        }
        
        private void moveCursorRight() {
            if (cursorX == window.getGridWidth() - 1) {
                if (cursorY < window.getGridHeight() - 1) {
                    cursorY++;
                    cursorX = 0;
                }
            } else {
                cursorX++;
            }
            
            Platform.runLater(() -> { window.repaint(); });
        }
        
        private void moveCursorDown() {
            if (cursorY == window.getGridHeight() - 1) {
                return;
            }
            
            cursorY++;
            Platform.runLater(() -> { window.repaint(); });
        }
    }
    
    private final class TextEditorMouseListener 
            implements TextUIWindowMouseListener {
        
        public void onMouseClick(MouseEvent event, int charX, int charY) {
            handle(event, charX, charY);
            cursorX = charX;
            cursorY = charY;
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
