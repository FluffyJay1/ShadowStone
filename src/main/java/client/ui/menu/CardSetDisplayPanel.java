package client.ui.menu;

import java.util.*;

import client.Game;
import org.newdawn.slick.geom.*;

import client.ui.*;
import server.card.*;
import server.card.cardset.*;

public class CardSetDisplayPanel extends UIBox {
    /**
     * Alert syntax: "cardsetclick (cardClassString)" [clickCount]
     */
    public static final String CARDSET_CLICK = "cardsetclick";
    public static final String BACKGROUND_CLICK = "cardsetbackgroundclick";
    CardSet set;
    final ScrollingContext scroll;
    final List<CardDisplayUnit> cards = new ArrayList<>();

    public CardSetDisplayPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(1600, 500), "res/ui/uiboxborder.png");
        this.margins.set(10, 10);
        this.addChild(new Text(ui, new Vector2f(0, -225), "Cards", 300, 20, Game.DEFAULT_FONT, 34, 0, 0));
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
            for (CardText cardText : set) {
                CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
                this.scroll.addChild(cdu);
                this.cards.add(cdu);
                cdu.setCardText(cardText);
            }
        }
        this.updateCardPositions();
    }

    public void updateCardPositions() {
        this.cards.sort((a, b) -> Card.compareDefault(a.card, b.card));
        for (int i = 0; i < this.cards.size(); i++) {
            this.cards.get(i).setPos(new Vector2f(i % 8 * 160 - 560, i / 8 * 160 - 70), 0.99);
        }
    }

    private CardDisplayUnit getCardDisplayUnit(Class<? extends Card> cardClass) {
        for (CardDisplayUnit cdu : this.cards) {
            if (cdu.getCardText().equals(cardClass)) {
                return cdu;
            }
        }
        return null;
    }

}
