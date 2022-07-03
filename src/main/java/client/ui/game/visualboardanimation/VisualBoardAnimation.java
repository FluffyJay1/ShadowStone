package client.ui.game.visualboardanimation;

import org.newdawn.slick.Graphics;

/**
 * To handle playing animations on the board, e.g. an animation of tiny throwing rocks for his attack
 */
public interface VisualBoardAnimation {
    void update(double frametime);
    double getTime(); // gets the time elapsed
    boolean isStarted();
    boolean isFinished();
    boolean shouldAnimate();
    void draw(Graphics g);
}
