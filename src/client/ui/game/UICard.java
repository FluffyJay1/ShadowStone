package client.ui.game;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.*;
import server.card.*;
import server.card.unleashpower.*;

public class UICard extends UIBox {
	public static final String CARD_CLICK = "cardclick";
	private Card card;
	private UIBoard uib;

	public UICard(UI ui, UIBoard uib, Card c) {
		super(ui, new Vector2f(), Card.CARD_DIMENSIONS, "");
		this.uib = uib;
		this.setCard(c);
		if (c instanceof UnleashPower) {
			this.hitcircle = true;
			this.setDim(new Vector2f(2, 2).scale((float) UnleashPower.UNLEASH_POWER_RADIUS));
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// this.uib.draggingCard = this;
		this.uib.mousePressedCard(this, button, x, y);
		this.setZ(UIBoard.CARD_DRAGGING_Z);
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		this.uib.mouseReleased(button, x, y);
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {

	}

	public void setCard(Card card) {
		this.card = card;
	}

	public Card getCard() {
		return this.card;
	}

	public Minion getMinion() {
		return (Minion) this.card;
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		if (!this.getHide() && this.card != null) {
			this.card.draw(g, this.getFinalPos(), this.getScale());
			// g.drawString("" + this.getZ(), this.getFinalPos().x, this.getFinalPos().y);
		}
	}

}
