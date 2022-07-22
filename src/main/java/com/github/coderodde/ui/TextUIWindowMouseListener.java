package com.github.coderodde.ui;

import javafx.scene.input.MouseEvent;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 ()
 * @since 1.6 ()
 */
public interface TextUIWindowMouseListener {

    default void onMouseClick(MouseEvent e, int charX, int charY) {
        
    }
    
    default void onMouseEntered(MouseEvent e, int charX, int charY) {
    
    }
    
    default void onMouseExited(MouseEvent e, int charX, int charY) {
        
    }
    
    default void onMousePressed(MouseEvent e, int charX, int charY) {
        
    }
    
    default void onMouseReleased(MouseEvent e, int charX, int charY) {
        
    }
    
    default void onMouseMove(MouseEvent e, int charX, int charY) {
        
    }
    
    default void onMouseDragged(MouseEvent e, int charX, int charY) {
        
    }
}
