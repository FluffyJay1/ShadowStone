package client.ui.menu;

import java.util.*;

import client.Game;
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
                    new Text(ui, new Vector2f(0, 0), "Deck", 400, 20, Game.DEFAULT_FONT, 28, 0, 0));
            this.addChild(this.textfield);
        } else {
            this.text = new Text(ui, new Vector2f(0, -225), "Deck", 300, 20, Game.DEFAULT_FONT, 34, 0, 0);
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
            for (Map.Entry<CardText, Integer> entry : deck.cardClassCounts.entrySet()) {
                CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
                this.scroll.addChild(cdu);
                this.cards.add(cdu);
                cdu.setCardText(entry.getKey());
                cdu.setCount(entry.getValue());
            }
        }
        this.updateCardPositions();
    }

    public void addCard(CardText cardText) {
        boolean newpanel = !this.deck.cardClassCounts.containsKey(cardText);
        if (this.deck.addCard(cardText)) {
            if (newpanel) {
                CardDisplayUnit cdu = new CardDisplayUnit(ui, new Vector2f());
                cdu.setCardText(cardText);
                this.scroll.addChild(cdu);
                this.cards.add(cdu);

            }

            CardDisplayUnit cdu = this.getCardDisplayUnit(cardText);
            if (cdu != null) {
                cdu.setCount(this.deck.cardClassCounts.get(cardText));
            }
            this.updateCardPositions();
        }
    }

    public void removeCard(CardText cardText) {
        if (this.deck.removeCard(cardText)) {

            if (!this.deck.cardClassCounts.containsKey(cardText)) {
                CardDisplayUnit cdu = this.getCardDisplayUnit(cardText);
                this.scroll.removeChild(cdu);
                this.cards.remove(cdu);
            } else {
                CardDisplayUnit cdu = this.getCardDisplayUnit(cardText);
                if (cdu != null) {
                    cdu.setCount(this.deck.cardClassCounts.get(cardText));
                }
            }
            this.updateCardPositions();
        }
    }

    public void updateCardPositions() {
        this.cards.sort((a, b) -> Card.compareDefault(a.card, b.card));
        for (int i = 0; i < this.cards.size(); i++) {
            this.cards.get(i).setPos(new Vector2f(i % 8 * 160 - 560, i / 8 * 160 - 70), 0.99);
        }
    }

    private CardDisplayUnit getCardDisplayUnit(CardText cardText) {
        for (CardDisplayUnit cdu : this.cards) {
            if (cdu.getCardText().equals(cardText)) {
                return cdu;
            }
        }
        return null;
    }

}
