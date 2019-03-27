package client.ui.game.eventanimation.board;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.*;
import server.event.*;

public class EventAnimationPlayCard extends EventAnimationBoard {
	public EventAnimationPlayCard() {
		this(0.7);
	}

	public EventAnimationPlayCard(double duration) {
		super(duration);
	}

	@Override
	public void onStart() {
		this.uiboard.visualPlayingCard = ((EventPlayCard) this.event).c.uiCard;
		this.uiboard.visualPlayingCard.setScale(UIBoard.CARDS_SCALE_PLAY);
		this.uiboard.visualPlayingCard.setPos(new Vector2f(0, 0), 0.999f);
		this.uiboard.visualPlayingCard.setZ(UIBoard.CARD_VISUALPLAYING_Z);
	}

	@Override
	public void onFinish() {
		// uh maybe add some particles or something
	}

	@Override
	public void draw(Graphics g) {

	}
}
