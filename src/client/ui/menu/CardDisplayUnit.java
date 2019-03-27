package client.ui.menu;

import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import server.card.Card;
import server.card.CardStatus;

public class CardDisplayUnit extends UIBox {
	public static final String CARD_CLICK = "cardclick";
	public static final double SCALE = 0.75;
	private int cardid;
	Text text;
	Card card;

	public CardDisplayUnit(UI ui, Vector2f pos) {
		super(ui, pos, Card.CARD_DIMENSIONS.copy().scale((float) SCALE), "res/ui/uiboxborder.png");
		this.text = new Text(ui, new Vector2f((float) this.getLocalRight(false), (float) this.getLocalTop(false)), "0",
				50, 14, "Verdana", 20, -1, 1);
		this.addChild(this.text);
		this.setCardID(0);
		this.setCount(-1);

	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		this.alert(CARD_CLICK, cardid, clickCount);

	}

	public void setCardID(int cardid) {
		this.cardid = cardid;
		if (cardid == 0) {
			this.card = null;
		} else {
			this.card = Card.createFromConstructorString(null, new StringTokenizer(cardid + " 1"));
			this.card.status = CardStatus.HAND;
		}
	}

	public int getCardID() {
		return this.cardid;
	}

	public void setCount(int count) {
		if (count == -1) {
			this.text.setText("");
		} else {
			this.text.setText("x" + count);
		}
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		if (this.card != null) {
			card.draw(g, this.getFinalPos(), SCALE);
		}
	}

}
