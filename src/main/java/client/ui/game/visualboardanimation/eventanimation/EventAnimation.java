package client.ui.game.visualboardanimation.eventanimation;

import client.ui.game.visualboardanimation.VisualBoardAnimation;
import org.newdawn.slick.*;

import client.*;
import server.event.*;

/**
 * Base class for handling animations on the board. The general idea is given a
 * board and a specific event, an instance of this class will handle the timing
 * and the drawing for the event animation
 * 
 * These will be created by the EventAnimationFactory class
 * 
 * @author Michael
 *
 * @param <T> The type of event this animation depends on
 */
public abstract class EventAnimation<T extends Event> implements VisualBoardAnimation {
    public T event;
    protected VisualBoard visualBoard;
    protected double time, preTime, postTime;
    boolean started;
    protected boolean processedEvent;
    boolean finished;

    /**
     * Construct an EventAnimation, which when updated, will automatically execute
     * the event after preTime
     * 
     * @param preTime  The amount of time dedicated to the animation before the
     *                 event is processed
     * @param postTime The amount of time dedicated to the animation after the event
     *                 is processed
     */
    public EventAnimation(double preTime, double postTime) {
        this.time = 0;
        this.preTime = preTime;
        this.postTime = postTime;
        this.started = false;
        this.processedEvent = false;
        this.finished = false;
    }

    public void init(VisualBoard b, T event) {
        this.visualBoard = b;
        this.event = event;
    }

    public void update(double frametime) {
        this.time += frametime;
        if (!this.started) {
            this.onStart();
            this.started = true;
        }
        if (!this.processedEvent && !this.isPre() && this.event != null) {
            this.visualBoard.processEvent(null, null, this.event);
            this.onProcess();
            this.processedEvent = true;
        }
        if (!this.finished && isFinished()) {
            this.onFinish();
            this.finished = true;
        }
    }

    @Override
    public double getTime() {
        return this.time;
    }

    public double getTimeUntilProcess() {
        return this.preTime - this.time;
    }

    public boolean isPre() {
        return this.time < this.preTime;
    }

    public double normalizedPre() {
        return this.time / this.preTime;
    }

    public double normalizedPost() {
        return (this.time - this.preTime) / this.postTime;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public boolean isFinished() {
        return this.time > this.preTime + this.postTime;
    }

    // simple hooks
    public void onStart() {

    }

    public void onProcess() {

    }

    public void onFinish() {

    }

    // override this placeholder
    public void draw(Graphics g) {
        g.drawString(this.getClass().getName(), 0, 0);
    }
}
