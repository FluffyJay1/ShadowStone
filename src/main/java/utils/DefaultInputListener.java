package utils;

import org.newdawn.slick.Input;
import org.newdawn.slick.InputListener;

/**
 * its like if u want all the thing at once
 * 
 * @author Michael
 *
 */
public interface DefaultInputListener extends InputListener {
    @Override
    default void inputStarted() {
    }

    @Override
    default void inputEnded() {
    }

    @Override
    default void setInput(Input inp) {

    }

    @Override
    default boolean isAcceptingInput() {
        return true;
    }

    @Override
    default void mouseWheelMoved(int change) {
        // TODO Auto-generated method stub

    }

    @Override
    default void mouseClicked(int button, int x, int y, int clickCount) {
        // TODO Auto-generated method stub

    }

    @Override
    default void mousePressed(int button, int x, int y) {
        // TODO Auto-generated method stub

    }

    @Override
    default void mouseReleased(int button, int x, int y) {
        // TODO Auto-generated method stub

    }

    @Override
    default void mouseMoved(int oldx, int oldy, int newx, int newy) {
        // TODO Auto-generated method stub

    }

    @Override
    default void mouseDragged(int oldx, int oldy, int newx, int newy) {
        // TODO Auto-generated method stub

    }

    @Override
    default void keyPressed(int key, char c) {
        // TODO Auto-generated method stub

    }

    @Override
    default void keyReleased(int key, char c) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerButtonPressed(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerButtonReleased(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerDownPressed(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerDownReleased(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerLeftPressed(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerLeftReleased(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerRightPressed(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerRightReleased(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerUpPressed(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    default void controllerUpReleased(int arg0) {
        // TODO Auto-generated method stub

    }
}
