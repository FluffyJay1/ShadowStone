package client.ui.game.visualboardanimation.eventanimation;

import client.ui.game.UICard;
import client.ui.game.visualboardanimation.VisualBoardAnimation;
import org.newdawn.slick.*;

import client.*;
import server.event.*;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

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
    private Set<UICard> animatingCards;
    private PriorityQueue<ScheduledAnimation> scheduledPre;
    private PriorityQueue<ScheduledAnimation> scheduledPost;

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
        this.animatingCards = new HashSet<>();
        this.scheduledPre = new PriorityQueue<>();
        this.scheduledPost = new PriorityQueue<>();
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
        ScheduledAnimation pre = this.scheduledPre.peek();
        while (pre != null && (this.normalizedPre() > pre.time || !isPre())) {
            pre.animation.run();
            this.scheduledPre.remove();
            pre = this.scheduledPre.peek();
        }
        if (!this.processedEvent && !this.isPre() && this.event != null) {
            this.visualBoard.processEvent(this.event);
            this.onProcess();
            this.processedEvent = true;
        }
        ScheduledAnimation post = this.scheduledPost.peek();
        while (post != null && (this.normalizedPost() > post.time || isFinished())) {
            post.animation.run();
            this.scheduledPost.remove();
            post = this.scheduledPost.peek();
        }
        if (!this.finished && isFinished()) {
            this.onFinish();
            this.finished = true;
            for (UICard c : this.animatingCards) {
                c.stopUsingInAnimation();
            }
            this.animatingCards.clear();
        }
    }

    public void useCardInAnimation(UICard c) {
        if (this.animatingCards.add(c)) {
            c.useInAnimation();
        }
    }

    public void stopUsingCardInAnimation(UICard c) {
        if (this.animatingCards.remove(c)) {
            c.stopUsingInAnimation();
        }
    }

    /**
     * Schedule something to run (like setting a card's scale or position) at an
     * arbitrary time in the animation. Essentially a generalized form of using
     * the onStart(), onProcess(), and onFinish() hooks.
     *
     * @param pre Whether to schedule the thing in the "pre" phase, if false then we use "post"
     * @param normalizedTime The normalized time in the respective phase in which we should execute the thing to run
     * @param animation The thing to run
     */
    public void scheduleAnimation(boolean pre, double normalizedTime, Runnable animation) {
        ScheduledAnimation sa = new ScheduledAnimation(normalizedTime, animation);
        if (pre) {
            this.scheduledPre.add(sa);
        } else {
            this.scheduledPost.add(sa);
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

    }

    private static class ScheduledAnimation implements Comparable<ScheduledAnimation> {
        double time;
        Runnable animation;
        ScheduledAnimation(double time, Runnable animation) {
            this.time = time;
            this.animation = animation;
        }

        @Override
        public int compareTo(ScheduledAnimation o) {
            return Double.compare(this.time, o.time);
        }
    }
}
