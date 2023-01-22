package client.ui.game.visualboardanimation.eventgroupanimation;

import client.VisualBoard;
import client.ui.game.visualboardanimation.VisualBoardAnimation;
import server.card.CardStatus;
import server.event.eventgroup.EventGroup;

public abstract class EventGroupAnimation implements VisualBoardAnimation {
    protected VisualBoard visualBoard;
    public EventGroup eventgroup;
    double time;
    double maxTime;

    public EventGroupAnimation(double time) {
        this.time = 0;
        this.maxTime = time;
    }

    public void init(VisualBoard b, EventGroup eventgroup) {
        this.visualBoard = b;
        this.eventgroup = eventgroup;
        if (!this.shouldAnimate()) {
            this.maxTime = 0;
        }
    }

    @Override
    public boolean shouldAnimate() {
        return this.eventgroup.cards.stream().anyMatch(c -> c.isVisibleTo(this.visualBoard.getLocalteam()));
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
