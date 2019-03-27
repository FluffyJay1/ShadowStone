package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import server.event.*;

public class EventAnimation {
	public Event event;
	protected double time, maxtime, delay;

	public EventAnimation(double duration) {
		this.time = 0;
		this.maxtime = duration;
	}

	public void init(Event event) {
		this.init(event, 0);
	}

	public void init(Event event, double delay) {
		this.event = event;
		this.time = 0;
		this.delay += delay;
		if (delay == 0) {
			this.onStart();
		}
	}

	public void update(double frametime) {
		if (this.hasStarted()) {
			this.time += frametime;
			if (this.isFinished()) {
				this.onFinish();
			}
		} else {
			this.delay -= frametime;
			if (this.hasStarted()) {
				this.onStart();
			}
		}
	}

	public double normalizedTime() {
		return this.time / this.maxtime;
	}

	public void onStart() {

	}

	public void onFinish() {

	}

	public boolean hasStarted() {
		return this.delay <= 0;
	}

	public boolean isFinished() {
		return this.hasStarted() && this.time >= maxtime;
	}

	public void draw(Graphics g) {
		g.drawString(this.getClass().getName(), 0, 0);
	}
}
