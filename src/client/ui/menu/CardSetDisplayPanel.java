package client.ui.menu;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.ui.*;
import server.card.*;
import server.card.cardpack.*;

public class CardSetDisplayPanel extends UIBox {
	/**
	 * Alert syntax: "cardsetclick (cardClassString)" [clickCount]
	 */
	public static final String CARDSET_CLICK = "cardsetclick";
	public static final String BACKGROUND_CLICK = "cardsetbackgroundclick";
	CardSet set;
	ScrollingContext scroll;
	List<CardDisplayUnit> cards = new ArrayList<CardDisplayUnit>();

	public CardSetDisplayPanel(UI ui, Vector2f pos) {
		super(ui, pos, new Vector2f(1600, 500), "res/ui/uiboxborder.png");
		this.margins.set(10, 10);
		this.addChild(new Text(ui, new Vector2f(0, -225), "Cards", 300, 20, "Verdana", 34, 0, 0));
		this.scroll = new ScrollingContext(ui, new Vector2f(), new Vector2f((float) this.getWidth(true), 400));
		this.scroll.clip = true;
		this.addChild(this.scroll);
	}

	@Override
	public void onAlert(String strarg, int... intarg) {
		StringTokenizer st = new StringTokenizer(strarg);
		switch (st.nextToken()) {
		case CardDisplayUnit.CARD_CLICK:
			this.alert(CARDSET_CLICK + " " + st.nextToken(), intarg);
			break;
		default:
			this.alert(strarg, intarg);
			break;
		}
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		this.alert(BACKGROUND_CLICK);
	}

	public void setCardSet(CardSet set) {
		for (CardDisplayUnit cdu : this.cards) {
			this.scroll.removeChild(cdu);
		}
		this.cards.clear();
		this.set = set;
		if (set != null) {
			for (Class<? extends Card> cardClass : set.cardClasses) {
				CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
				this.scroll.addChild(cdu);
				this.cards.add(cdu);
				cdu.setCardClass(cardClass);
			}
		}
		this.updateCardPositions();
	}

	public void updateCardPositions() {
		this.cards.sort(new Comparator<CardDisplayUnit>() {
			@Override
			public int compare(CardDisplayUnit a, CardDisplayUnit b) {
				return Card.compareDefault(a.card, b.card);
			}
		});
		for (int i = 0; i < this.cards.size(); i++) {
			this.cards.get(i).setPos(new Vector2f(i % 8 * 160 - 560, i / 8 * 100 - 70), 0.99);
		}
	}

	private CardDisplayUnit getCardDisplayUnit(Class<? extends Card> cardClass) {
		for (CardDisplayUnit cdu : this.cards) {
			if (cdu.getCardClass().equals(cardClass)) {
				return cdu;
			}
		}
		return null;
	}

}
