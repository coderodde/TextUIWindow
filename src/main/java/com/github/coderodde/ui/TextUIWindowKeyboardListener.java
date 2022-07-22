package com.github.coderodde.ui;

import javafx.scene.input.KeyEvent;

/**
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 ()
 * @since 1.6 ()
 */
public interface TextUIWindowKeyboardListener {

    default void onKeyPressed(KeyEvent keyEvent) {
        
    }

    default void onKeyReleased(KeyEvent keyEvent) {
        
    }

    default void onKeyTyped(KeyEvent keyEvent) {
        
    }
}
