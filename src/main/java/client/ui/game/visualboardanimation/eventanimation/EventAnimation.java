package client.ui.game.visualboardanimation.eventanimation;

import client.ui.game.UICard;
import client.ui.game.visualboardanimation.VisualBoardAnimation;
import org.newdawn.slick.*;

import client.*;
import server.event.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.StringTokenizer;

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
    private final Set<UICard> animatingCards;
    private final PriorityQueue<ScheduledAnimation> scheduledPre;
    private final PriorityQueue<ScheduledAnimation> scheduledPost;

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
        if (!this.shouldAnimate()) {
            this.preTime = 0;
            this.postTime = 0;
        }
    }

    // delay this animation's start, such that this event processes after the other one does by a delay
    public void delayProcess(EventAnimation<?> other, double delay) {
        this.time = this.preTime - other.getTimeUntilProcess() - delay;
    }

    public void update(double frametime) {
        this.time += frametime;
        if (this.time < 0) {
            return;
        }
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
            this.beforeProcess();
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

    public void beforeProcess() {
        
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

    public static String stringOrNull(EventAnimation ed) {
        if (ed == null) {
            return "null ";
        }
        return ed.toString();
    }

    public String toString() {
        String s = this.extraParamString();
        int numTokens = new StringTokenizer(s).countTokens() + 1;
        return numTokens + " " + this.getClass().getName() + " " + s;
    }

    // override this
    public String extraParamString() {
        return "";
    }

    // if animation info is stored in a string, and that string gets serialized
    // as part of a larger object, when deserializing that object this will
    // retrieve the animation string and not actually do reflection to create
    // the animation
    public static String extractAnimationString(StringTokenizer st) {
        String firstToken = st.nextToken();
        if (firstToken.equals("null")) {
            return "null ";
        }
        int numTokens = Integer.parseInt(firstToken);
        StringBuilder sb = new StringBuilder(firstToken).append(" ");
        for (int i = 0; i < numTokens; i++) {
            sb.append(st.nextToken()).append(" ");
        }
        return sb.toString();
    }

    // so much catch
    public static EventAnimation fromString(StringTokenizer st) {
        String firstToken = st.nextToken();
        if (firstToken.equals("null")) {
            return null;
        }
        String className = st.nextToken();
        try {
            Class<? extends EventAnimation> edclass = Class.forName(className).asSubclass(EventAnimation.class);
            try {
                return (EventAnimation) edclass.getMethod("fromExtraParams", StringTokenizer.class).invoke(null, st);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // assume it has the default constructor then
                return edclass.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
