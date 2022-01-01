package client.ui.menu;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.ui.*;
import server.card.*;
import server.card.cardpack.*;

public class DeckDisplayPanel extends UIBox {
    /**
     * Alert syntax: "deckdisplaycardselect (cardClassString)" [clickCount]
     */
    public static final String CARD_CLICK = "deckdisplaycardselect";
    public static final String DECK_CONFIRM = "deckdisplaydeckconfirm";
    public static final String BACKGROUND_CLICK = "deckdisplaybackgroundclick";
    public ConstructedDeck deck;
    final ScrollingContext scroll;
    final ArrayList<CardDisplayUnit> cards = new ArrayList<>();
    final GenericButton okbutton;
    TextField textfield;
    Text text;
    final boolean edit;

    public DeckDisplayPanel(UI ui, Vector2f pos, boolean edit) {
        super(ui, pos, new Vector2f(1600, 500), "res/ui/uiboxborder.png");
        this.margins.set(10, 10);
        this.edit = edit;
        if (edit) {
            this.textfield = new TextField(ui, new Vector2f(0, -225), new Vector2f(400, 50), "Deck",
                    new Text(ui, new Vector2f(0, 0), "Deck", 400, 20, "Verdana", 28, 0, 0));
            this.addChild(this.textfield);
        } else {
            this.text = new Text(ui, new Vector2f(0, -225), "Deck", 300, 20, "Verdana", 34, 0, 0);
            this.addChild(this.text);
        }
        this.scroll = new ScrollingContext(ui, new Vector2f(), new Vector2f((float) this.getWidth(true), 400));
        this.scroll.clip = true;
        this.addChild(this.scroll);
        this.okbutton = new GenericButton(ui, new Vector2f(0, 150), new Vector2f(100, 50), "Ok", 0) {
            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                this.alert(DECK_CONFIRM);
            }
        };
        this.addChild(this.okbutton);
    }

    @Override
    public void onAlert(String strarg, int... intarg) {
        StringTokenizer st = new StringTokenizer(strarg);
        switch (st.nextToken()) {
        case CardDisplayUnit.CARD_CLICK:
            this.alert(CARD_CLICK + " " + st.nextToken(), intarg);
            break;
        case DECK_CONFIRM:
            this.deck.name = this.textfield.getText();
            this.alert(strarg, intarg);
            break;
        case TextField.TEXT_ENTER:
            this.deck.name = this.textfield.getText();
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

    public void setDeck(ConstructedDeck deck) {
        if (this.edit) {
            this.textfield.setText(deck.name);
        } else {
            this.text.setText(deck.name);
        }
        for (CardDisplayUnit cdu : this.cards) {
            this.scroll.removeChild(cdu);
        }
        this.cards.clear();
        this.deck = deck;
        if (deck != null) {
            for (Map.Entry<Class<? extends Card>, Integer> entry : deck.cardClassCounts.entrySet()) {
                CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
                this.scroll.addChild(cdu);
                this.cards.add(cdu);
                cdu.setCardClass(entry.getKey());
                cdu.setCount(entry.getValue());
            }
        }
        this.updateCardPositions();
    }

    public void addCard(Class<? extends Card> cardClass) {
        boolean newpanel = !this.deck.cardClassCounts.containsKey(cardClass);
        if (this.deck.addCard(cardClass)) {
            if (newpanel) {
                CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
                cdu.setCardClass(cardClass);
                this.scroll.addChild(cdu);
                this.cards.add(cdu);

            }

            CardDisplayUnit cdu = this.getCardDisplayUnit(cardClass);
            if (cdu != null) {
                cdu.setCount(this.deck.cardClassCounts.get(cardClass));
            }
            this.updateCardPositions();
        }
    }

    public void removeCard(Class<? extends Card> cardClass) {
        if (this.deck.removeCard(cardClass)) {

            if (!this.deck.cardClassCounts.containsKey(cardClass)) {
                CardDisplayUnit cdu = this.getCardDisplayUnit(cardClass);
                this.scroll.removeChild(cdu);
                this.cards.remove(cdu);
            } else {
                CardDisplayUnit cdu = this.getCardDisplayUnit(cardClass);
                if (cdu != null) {
                    cdu.setCount(this.deck.cardClassCounts.get(cardClass));
                }
            }
            this.updateCardPositions();
        }
    }

    public void updateCardPositions() {
        this.cards.sort((a, b) -> Card.compareDefault(a.card, b.card));
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
