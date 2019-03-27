package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.*;
import client.Game;
import server.event.*;

public class EventAnimationTurnStart extends EventAnimation {
	public EventAnimationTurnStart() {
		this(0.5);
	}

	public EventAnimationTurnStart(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventTurnStart e = (EventTurnStart) this.event;
		UnicodeFont font = Game.getFont("Verdana", 80, true, false);
		String dstring = "TURN START";
		switch (e.p.team * e.p.board.localteam) { // ez hack
		case 1:
			g.setColor(Color.cyan);
			dstring = "YOUR TURN";
			break;
		case -1:
			g.setColor(Color.red);
			dstring = "OPPONENT'S TURN";
			break;
		}
		g.setFont(font);
		g.drawString(dstring, Config.WINDOW_WIDTH / 2 - font.getWidth(dstring) / 2,
				Config.WINDOW_HEIGHT / 2 - font.getHeight(dstring));
		g.setColor(Color.white);
	}
}
