package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationRestore extends EventAnimation {
	public EventAnimationRestore() {
		this(0.5);
	}

	public EventAnimationRestore(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventRestore e = (EventRestore) this.event;
		g.setColor(Color.green);
		UnicodeFont font = Game.getFont("Verdana", 80, true, false);
		g.setFont(font);
		float yoff = (float) (Math.pow(1 - this.normalizedTime(), 2) * 50) - 12.5f;
		for (int i = 0; i < e.m.size(); i++) {
			String dstring = e.actualHeal.get(i) + "";
			g.drawString(dstring, e.m.get(i).uiCard.getFinalPos().x - font.getWidth(dstring) / 2,
					e.m.get(i).uiCard.getFinalPos().y - font.getHeight(dstring) + yoff);
		}
		g.setColor(Color.white);
	}
}
