package client.ui.game.visualboardanimation.eventgroupanimation;

import client.VisualBoard;
import client.ui.game.visualboardanimation.VisualBoardAnimation;
import server.event.eventgroup.EventGroup;

public abstract class EventGroupAnimation implements VisualBoardAnimation {
    protected VisualBoard visualBoard;
    public EventGroup eventgroup;
    double time;
    final double maxTime;

    public EventGroupAnimation(double time) {
        this.time = 0;
        this.maxTime = time;
    }

    public void init(VisualBoard b, EventGroup eventgroup) {
        this.visualBoard = b;
        this.eventgroup = eventgroup;
    }

    @Override
    public void update(double frametime) {
        this.time += frametime;
    }

    @Override
    public double getTime() {
        return this.time;
    }

    @Override
    public boolean isStarted() {
        return this.time > 0;
    }

    @Override
    public boolean isFinished() {
        return this.time > this.maxTime;
    }

    public double normalizedTime() {
        return this.time / this.maxTime;
    }
}
