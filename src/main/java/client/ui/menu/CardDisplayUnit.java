package client.ui.menu;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.*;
import client.ui.game.*;
import server.card.*;

public class CardDisplayUnit extends UIBox {
    /**
     * Alert Syntax: "cardclick (cardClassString)" [clickCount]
     */
    public static final String CARD_CLICK = "cardclick";
    public static final double SCALE = 0.75;
    private Class<? extends Card> cardClass;
    final Text text;
    Card card;
    final UICard uicard;

    public CardDisplayUnit(UI ui, Vector2f pos) {
        super(ui, pos, UICard.CARD_DIMENSIONS.copy().scale((float) SCALE), "res/ui/uiboxborder.png");
        this.text = new Text(ui, new Vector2f((float) this.getLocalRight(false), (float) this.getLocalTop(false)), "0",
                50, 14, "Verdana", 20, -1, 1);
        this.addChild(this.text);
        this.uicard = new UICard(ui, null, null);
        this.setCardClass(null);
        this.setCount(-1);

    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        this.alert(CARD_CLICK + " " + cardClass.getName(), clickCount);

    }

    public void setCardClass(Class<? extends Card> cardClass) {
        this.cardClass = cardClass;
        if (cardClass == null) {
            this.card = null;
        } else {
            this.card = Card.createFromConstructor(null, cardClass);
            this.card.status = CardStatus.HAND;
            this.uicard.setCard(this.card);
        }
    }

    public Class<? extends Card> getCardClass() {
        return this.cardClass;
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
            this.uicard.drawCard(g, this.getAbsPos(), SCALE);
        }
    }

}
