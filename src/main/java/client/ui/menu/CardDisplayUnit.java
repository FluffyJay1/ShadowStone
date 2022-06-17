package client.ui.menu;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.*;

import client.ui.*;
import client.ui.game.*;
import server.card.*;
import server.card.effect.Stat;

import java.awt.*;

public class CardDisplayUnit extends UIBox {
    /**
     * Alert Syntax: "cardclick (cardClassString)" [mouseButton] [clickCount]
     */
    public static final String CARD_CLICK = "cardclick";
    public static final double SCALE = 0.75;
    public static final Vector2f PADDING = new Vector2f(10, 10);
    private CardText cardText;
    private CardStatus status;
    final Text text;
    Card card;
    final UICard uicard;

    public CardDisplayUnit(UI ui, Vector2f pos, CardText cardText) {
        super(ui, pos, UICard.CARD_DIMENSIONS.copy().scale((float) SCALE).add(PADDING), "");
        this.margins.set(PADDING); // confusing i kno
        this.text = new Text(ui, new Vector2f(this.getWidth(true) / 2, -this.getHeight(true) / 2), "0",
                50, 14, 20, -1, 1);
        this.addChild(this.text);
        this.status = CardStatus.HAND;
        this.card = getCardFrom(cardText);
        this.uicard = new UICard(ui, null, this.card);
        this.setCardText(cardText);
        this.setCount(-1);
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        this.alert(CARD_CLICK + " " + cardText.toString(), button, clickCount);
    }

    private Card getCardFrom(CardText cardText) {
        Card card = cardText.constructInstance(null);
        card.status = this.status;
        return card;
    }

    public void setCardText(CardText cardText) {
        this.cardText = cardText;
        this.card = getCardFrom(cardText);
        this.uicard.setCard(this.card);
    }

    public CardText getCardText() {
        return this.cardText;
    }

    public void setCardStatus(CardStatus status) {
        this.status = status;
        if (this.card != null) {
            this.card.status = status;
        }
    }

    public void setCount(int count) {
        if (count == -1) {
            this.text.setText("");
            this.text.setVisible(false);
        } else {
            this.text.setText("x" + count);
            this.text.setVisible(true);
        }
    }

    public void setBonusHealth(int health) {
        if (this.card instanceof Minion) {
            Minion m = (Minion) this.card;
            m.health = this.card.finalBasicStats.get(Stat.HEALTH) + health;
        }
    }

    @Override
    public void drawSelf(Graphics g) {
        super.drawSelf(g);
        if (this.card != null) {
            this.uicard.drawCard(g, this.getCenterAbsPos(), SCALE, Color.white);
        }
    }

}
