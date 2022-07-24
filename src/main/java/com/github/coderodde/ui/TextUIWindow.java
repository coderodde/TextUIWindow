package com.github.coderodde.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.FontWeight;

/**
 * This class implements a simple colorful terminal window.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jul 24, 2022)
 * @since 1.6 (Jul 24, 2022)
 */
public class TextUIWindow extends Canvas {
    
    private static final int MINIMUM_WIDTH = 1;
    private static final int MINIMUM_HEIGHT = 1;
    private static final int MINIMUM_FONT_SIZE = 1;
    private static final Color DEFAULT_TEXT_BACKGROUND_COLOR = Color.BLACK;
    private static final Color DEFAULT_TEXT_FOREGROUND_COLOR = Color.WHITE;
    private static final Color DEFAULT_BLINK_BACKGROUND_COLOR = Color.WHITE;
    private static final Color DEFAULT_BLINK_FOREGROUND_COLOR = Color.BLACK;
    private static final char DEFAULT_CHAR = ' ';
    private static final String FONT_NAME = "Monospaced";
    private static final int DEFAULT_CHAR_DELIMITER_LENGTH = 4;

    private final int width;
    private final int height;
    private final int fontSize;
    private final Font font;
    private final int fontCharWidth;
    private final int fontCharHeight;
    private final int charDelimiterLength;
    private int windowTitleBorderThickness;
    private final Set<TextUIWindowMouseListener> mouseMotionListeners = 
            new HashSet<>();
    
    private final Set<TextUIWindowKeyboardListener> keyboardListeners =
            new HashSet<>();
    
    private final Color[][] backgroundColorGrid;
    private final Color[][] foregroundColorGrid;
    private final boolean[][] cursorGrid;
    private final char[][] charGrid;
    private Color textBackgroundColor = DEFAULT_TEXT_BACKGROUND_COLOR;
    private Color textForegroundColor = DEFAULT_TEXT_FOREGROUND_COLOR;
    private Color blinkCursorBackgroundColor = DEFAULT_BLINK_BACKGROUND_COLOR;
    private Color blinkCursorForegroundColor = DEFAULT_BLINK_FOREGROUND_COLOR;
    
    public TextUIWindow(int width, int height, int fontSize) {
        this(width, height, fontSize, DEFAULT_CHAR_DELIMITER_LENGTH);
    }
    
    public TextUIWindow(int width, 
                        int height, 
                        int fontSize, 
                        int charDelimiterLength) {
        
        this.width = checkWidth(width);
        this.height = checkHeight(height);
        this.fontSize = checkFontSize(fontSize);
        this.charDelimiterLength = 
                checkCharDelimiterLength(charDelimiterLength);
        
        this.font = getFont();
        this.fontCharWidth = getFontWidth();
        this.fontCharHeight = getFontHeight();
        
        backgroundColorGrid = new Color[height][width];
        foregroundColorGrid = new Color[height][width];
        charGrid = new char[height][width];
        cursorGrid = new boolean[height][width];
        
        setDefaultForegroundColors();
        setDefaultBackgroundColors();
        setChars();
        
        this.setWidth(width * (fontCharWidth + charDelimiterLength));
        this.setHeight(height * fontCharHeight);
        this.setFocusTraversable(true);
        this.addEventFilter(MouseEvent.ANY, (e) -> this.requestFocus());
        
        setMouseListeners();
        setMouseMotionListeners();
        setKeyboardListeners();
    }
    
    public Color getTextForegroundColor() {
        return textForegroundColor;
    }
    
    public Color getTextBackgroundColor() {
        return textBackgroundColor;
    }
    
    public Color getBlinkCursorForegroundColor() {
        return blinkCursorForegroundColor;
    }
    
    public Color getBlinkCursorBackgroundColor() {
        return blinkCursorBackgroundColor;
    }
    
    public void setForegroundColor(Color color) {
        textForegroundColor = 
                Objects.requireNonNull(color, "The input color is null.");
    }
    
    public void setBackgroundColor(Color color) {
        textBackgroundColor = 
                Objects.requireNonNull(color, "The input color is null.");
    }
    
    public void turnOffBlink(int charX, int charY) {
        if (checkXandY(charX, charY)) {
            cursorGrid[charY][charX] = false;
        }
    }
    
    public void setBlinkCursorBackgroundColor(Color backgroundColor) {
        this.blinkCursorBackgroundColor =
                Objects.requireNonNull(
                        backgroundColor, 
                        "backgroundColor is null.");
    }
    
    public void setBlinkCursorForegroundColor(Color foregroundColor) {
        this.blinkCursorForegroundColor =
                Objects.requireNonNull(
                        foregroundColor, 
                        "foregroundColor is null.");
    }
    
    public void setTextBackgroundColor(Color backgroundColor) {
        this.textBackgroundColor =
                Objects.requireNonNull(backgroundColor, 
                                       "The input color is null.");
    }
    
    public void setTextForegroundColor(Color foregroundColor) {
        this.textForegroundColor =
                Objects.requireNonNull(foregroundColor, 
                                       "The input color is null.");
    }
    
    public int getGridWidth() {
        return width;
    }
    
    public int getGridHeight() {
        return height;
    }
    
    public void toggleBlinkCursor(int charX, int charY) {
        if (checkXandY(charX, charY)) {
            cursorGrid[charY][charX] = !cursorGrid[charY][charX];
        }
    }
    
    public boolean readCursorStatus(int charX, int charY) {
        if (!checkX(charX)) {
            throw charXToException(charX);
        }
        
        if (!checkY(charY)) {
            throw charYToException(charY);
        }
        
        return cursorGrid[charY][charX];
    }
    
    public void printString(int charX, int charY, String text) {
        if (!checkY(charY)) {
            return;
        }
        
        for (int i = 0; i < text.length(); ++i) {
            setChar(charX + i, charY, text.charAt(i));
            
            if (!checkX(charX + i)) {
                // Once here, the input text string proceeds beyond the right
                // border. Nothing to print, can exit.
                return;
            }
        }
    }
    
    public void addTextUIWindowMouseListener(
            TextUIWindowMouseListener listener) {
        mouseMotionListeners.add(listener);
    }
    
    public void removeTextUIWindowMouseListener(
            TextUIWindowMouseListener listener) {
        mouseMotionListeners.remove(listener);
    }
    
    public void addTextUIWindowKeyboardListener(
            TextUIWindowKeyboardListener listener) {
        keyboardListeners.add(listener);
    }
    
    public void removeTextUIWindowKeyboardListener(
            TextUIWindowKeyboardListener listener) {
        keyboardListeners.remove(listener);
    }
    
    private void setMouseListeners() {
        setMouseClickedListener();
        setMouseEnteredListener();
        setMousePressedListener();
        setMouseReleasedListener();
        setMouseExitedListener();
    }
    
    private void setMouseMotionListeners() {
        setMouseMovedListener();
        setMouseDraggedListener();
    }
    
    private void setKeyboardListeners() {
        setKeyboardPressedListener();
        setKeyboardReleaseListener();
        setKeyboardTypedListener();
    }
    
    private void setKeyboardPressedListener() {
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                for (TextUIWindowKeyboardListener listener 
                        : keyboardListeners) {
                    listener.onKeyPressed(event);
                }
            }
        });
    }
    
    private void setKeyboardReleaseListener() {
        this.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                for (TextUIWindowKeyboardListener listener 
                        : keyboardListeners) {
                    listener.onKeyReleased(event);
                }
            }
        });
    }
    
    private void setKeyboardTypedListener() {
        this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                for (TextUIWindowKeyboardListener listener : keyboardListeners) {
                    listener.onKeyTyped(event);
                }
            }
        });
    }
    
    private void setMouseMovedListener() {
        this.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMouseMove(event, charX, charY);
                }
            }
        });
    }
    
    private void setMouseDraggedListener() {
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMouseClick(event, charX, charY);
                }
            }
        });
    }
    
    private void setMouseClickedListener() {
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMouseClick(event, charX, charY);
                }
            }
        });
    }
    
    private void setMouseEnteredListener() {
        this.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMouseEntered(event, charX, charY);
                }
            }
        });
    }
    
    private void setMouseExitedListener() {
        this.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMouseExited(event, charX, charY);
                }
            }
        });
    }
    
    private void setMousePressedListener() {
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMousePressed(event, charX, charY);
                }
            }
        });
    }
    
    private void setMouseReleasedListener() {
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int pixelX = (int) event.getX();
                int pixelY = (int) event.getY();
                
                int charX = convertPixelXtoCharX(pixelX);
                int charY = convertPixelYtoCharY(pixelY);
                
                for (TextUIWindowMouseListener listener 
                        : mouseMotionListeners) {
                    listener.onMouseReleased(event, charX, charY);
                }
            }
        });
    }
    
    private int convertPixelXtoCharX(int pixelX) {
        return pixelX / (fontCharWidth + charDelimiterLength);
    }
    
    private int convertPixelYtoCharY(int pixelY) {
        return (pixelY - windowTitleBorderThickness) / fontCharHeight;
    }
            
    public void setTitleBorderThickness(int thickness) {
        this.windowTitleBorderThickness = thickness;
    }
    
    public void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        
        for (int y = 0; y < height; y++) {
            repaintRow(gc, y);
        }
    }
    
    private void repaintRow(GraphicsContext gc, int y) {
        for (int x = 0; x < width; x++) {
            repaintCell(gc, x, y);
        }
    }
    
    private void repaintCell(GraphicsContext gc, int x, int y) {
        repaintCellBackground(gc, x, y);
        repaintCellForeground(gc, x, y);
    }
    
    private void repaintCellBackground(GraphicsContext gc,  
                                       int charX, 
                                       int charY) {
        if (cursorGrid[charY][charX]) {
            // Once here, we need to use the cursor's color:
            gc.setFill(blinkCursorBackgroundColor);
        } else {
            gc.setFill(backgroundColorGrid[charY][charX]);
        }
        
        gc.fillRect(charX * (fontCharWidth + charDelimiterLength),
                    charY * fontCharHeight,
                    fontCharWidth + charDelimiterLength,
                    fontCharHeight);
    }
            
    private void repaintCellForeground(GraphicsContext gc,
                                       int charX, 
                                       int charY) {
        gc.setFont(font);
        
        if (cursorGrid[charY][charX]) {
            gc.setFill(blinkCursorForegroundColor);
        } else {
            gc.setFill(foregroundColorGrid[charY][charX]);
        }
        
        int fixY = fontCharHeight - (int) getFontMetrics().getMaxAscent();
        
        gc.fillText("" + charGrid[charY][charX],
                    charDelimiterLength / 2 +
                            (fontCharWidth + charDelimiterLength) * charX,
                    fontCharHeight * (charY + 1) - fixY);
    }
    
    public Color getForegroundColor(int charX, int charY) {
        if (!checkX(charX)) {
            throw charXToException(charX);
        }
        
        if (!checkY(charY)) {
            throw charYToException(charY);
        }
        
        return foregroundColorGrid[charY][charX];
    }
    
    public Color getBackgroundColor(int charX, int charY) {
        if (!checkX(charX)) {
            throw charXToException(charX);
        }
        
        if (!checkY(charY)) {
            throw charYToException(charY);
        }
        
        return backgroundColorGrid[charY][charX];
    }
    
    public void setForegroundColor(int charX, int charY, Color color) {
        if (checkXandY(charX, charY)) {
            foregroundColorGrid[charY][charX] = 
                    Objects.requireNonNull(color, "The color is null.");
        }
    }
    
    public void setBackgroundColor(int x, int y, Color color) {
        if (checkXandY(x, y)) {
            backgroundColorGrid[y][x] = 
                    Objects.requireNonNull(color, "The color is null.");
        }
    }
    
    public char getChar(int charX, int charY) {
        if (!checkX(charX)) {
            throw charXToException(charX);
        }
        
        if (!checkY(charY)) {
            throw charYToException(charY);
        }
        
        return charGrid[charY][charX];
    }
    
    public void setChar(int x, int y, char ch) {
        if (checkXandY(x, y)) {
            charGrid[y][x] = ch;
            foregroundColorGrid[y][x] = textForegroundColor;
            backgroundColorGrid[y][x] = textBackgroundColor;
        }
    }
    
    public int getPreferredWidth() {
        return width * (fontCharWidth + charDelimiterLength);
    }
    
    public int getPreferredHeight() {
        return height * fontCharHeight;
    }
    
    private FontMetrics getFontMetrics() {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
    }
    
    private static int checkWidth(int widthCandidate) {
        if (widthCandidate < MINIMUM_WIDTH) {
            throw new IllegalArgumentException(
                    "Width candidate is invalid (" 
                            + widthCandidate
                            + "). Must be at least " 
                            + MINIMUM_WIDTH
                            + ".");
        }
        
        return widthCandidate;
    }
    
    private static int checkHeight(int heightCandidate) {
        if (heightCandidate < MINIMUM_WIDTH) {
            throw new IllegalArgumentException(
                    "Height candidate is invalid (" 
                            + heightCandidate
                            + "). Must be at least " 
                            + MINIMUM_HEIGHT
                            + ".");
        }
        
        return heightCandidate;
    }
    
    private static int checkFontSize(int fontSizeCandidate) {
        if (fontSizeCandidate < MINIMUM_FONT_SIZE) {
            throw new IllegalArgumentException(
                    "Font size candidate is invalid (" 
                            + fontSizeCandidate
                            + "). Must be at least " 
                            + MINIMUM_FONT_SIZE
                            + ".");
        }
        
        return fontSizeCandidate;
    }
    
    private int checkCharDelimiterLength(int charDelimiterLength) {
        if (charDelimiterLength < 0) {
            throw new IllegalArgumentException(
                    "Char delimiter length negative: (" 
                            + charDelimiterLength 
                            + "). Must be at least 0.");
        }
        
        return charDelimiterLength;
    }
    
    private IndexOutOfBoundsException charXToException(int charX) {
        if (charX < 0) {
            return new IndexOutOfBoundsException(
                    "Character X coordinate is negative: " + charX);
        }
        
        if (charX >= width) {
            return new IndexOutOfBoundsException(
                    "Character X coordinate is too large: " 
                            + charX
                            + ". Must be at most "
                            + (width - 1)
                            + ".");
        }
        
        throw new IllegalStateException("Should not get here.");
    }
    
    private IndexOutOfBoundsException charYToException(int charY) {
        if (charY < 0) {
            throw new IndexOutOfBoundsException(
                    "Character Y coordinate is negative: " + charY);
        }
        
        if (charY >= height) {
            throw new IndexOutOfBoundsException(
                    "Character Y coordinate is too large: " 
                            + charY
                            + ". Must be at most "
                            + (height - 1)
                            + ".");
        }
        
        throw new IllegalStateException("Should not get here.");
    }
    
    private boolean checkX(int x) {
        return x >= 0 && x < width;
    }
    
    private boolean checkY(int y) {
        return y >= 0 && y < height;
    }
    
    private boolean checkXandY(int x, int y) {
        return checkX(x) && checkY(y);
    }
    
    private void setDefaultForegroundColors() {
        for (Color[] colors : foregroundColorGrid) {
            for (int i = 0; i < width; i++) {
                colors[i] = DEFAULT_TEXT_FOREGROUND_COLOR;
            }
        }
    }
        
    private void setDefaultBackgroundColors() {
        for (Color[] colors : backgroundColorGrid) {
            for (int i = 0; i < width; i++) {
                colors[i] = DEFAULT_TEXT_BACKGROUND_COLOR;
            }
        }
    }
        
    private void setChars() {
        for (char[] charRow : charGrid) {
            for (int i = 0; i < width; i++) {
                charRow[i] = DEFAULT_CHAR;
            }
        }
    }
    
    private Font getFont() {
        return Font.font(FONT_NAME, FontWeight.BOLD, fontSize);
    }
    
    private int getFontWidth() {
        return (int) getFontMetrics().getCharWidth('C') + charDelimiterLength;
    }
    
    private int getFontHeight() {
        return (int) getFontMetrics().getLineHeight();
    }
}
