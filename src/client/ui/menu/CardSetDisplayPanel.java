package client.ui.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.newdawn.slick.geom.Vector2f;

import client.ui.ScrollingContext;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import server.card.cardpack.CardSet;
import server.card.cardpack.ConstructedDeck;

public class CardSetDisplayPanel extends UIBox {
	public static final String CARDSET_CLICK = "cardsetclick";
	public static final String BACKGROUND_CLICK = "cardsetbackgroundclick";
	CardSet set;
	ScrollingContext scroll;
	ArrayList<CardDisplayUnit> cards = new ArrayList<CardDisplayUnit>();

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
		switch (strarg) {
		case CardDisplayUnit.CARD_CLICK:
			this.alert(CARDSET_CLICK, intarg);
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
			for (int i : set.ids) {
				CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
				this.scroll.addChild(cdu);
				this.cards.add(cdu);
				cdu.setCardID(i);
			}
		}
		this.updateCardPositions();
	}

	public void updateCardPositions() {
		this.cards.sort(new Comparator<CardDisplayUnit>() {
			@Override
			public int compare(CardDisplayUnit a, CardDisplayUnit b) {
				return a.card.tooltip.cost - b.card.tooltip.cost;
			}
		});
		for (int i = 0; i < this.cards.size(); i++) {
			this.cards.get(i).setPos(new Vector2f(i % 8 * 160 - 560, i / 8 * 100 - 70), 0.99);
		}
	}

	private CardDisplayUnit getCardDisplayUnit(int id) {
		for (CardDisplayUnit cdu : this.cards) {
			if (cdu.getCardID() == id) {
				return cdu;
			}
		}
		return null;
	}

}
