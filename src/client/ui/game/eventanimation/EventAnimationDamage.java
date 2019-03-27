package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationDamage extends EventAnimation {
	public EventAnimationDamage() {
		this(0.5);
	}

	public EventAnimationDamage(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventDamage e = (EventDamage) this.event;
		g.setColor(Color.red);
		UnicodeFont font = Game.getFont("Verdana", 80, true, false);
		g.setFont(font);
		float yoff = (float) (Math.pow(0.5 - this.normalizedTime(), 2) * 200) - 25f;
		for (int i = 0; i < e.m.size(); i++) {
			String dstring = e.damage.get(i) + "";
			g.drawString(dstring, e.m.get(i).uiCard.getFinalPos().x - font.getWidth(dstring) / 2,
					e.m.get(i).uiCard.getFinalPos().y - font.getHeight(dstring) + yoff);
		}
		g.setColor(Color.white);
	}
}
