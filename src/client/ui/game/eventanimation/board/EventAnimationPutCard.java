package client.ui.game.eventanimation.board;

import org.newdawn.slick.*;

import client.ui.game.*;
import server.card.*;
import server.event.*;

public class EventAnimationPutCard extends EventAnimationBoard {
	public EventAnimationPutCard() {
		this(0.5);
	}

	public EventAnimationPutCard(double duration) {
		super(duration);
	}

	@Override
	public void onStart() {
		EventPutCard e = (EventPutCard) this.event;
		for (Card c : e.c) {
			if (e.status.equals(CardStatus.BOARD)) {
				c.uiCard.setScale(UIBoard.CARD_SCALE_BOARD);
			} else if (e.status.equals(CardStatus.HAND)) {
				c.uiCard.setScale(UIBoard.CARD_SCALE_HAND);
			}
		}
	}

	@Override
	public void onFinish() {
		// uh maybe add some particles or something
	}

	@Override
	public void draw(Graphics g) {

	}
}
