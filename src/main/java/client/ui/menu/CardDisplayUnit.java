package client.ui.menu;

import client.Game;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.*;

import client.ui.*;
import client.ui.game.*;
import server.card.*;
import server.card.effect.EffectStats;

public class CardDisplayUnit extends UIBox {
    /**
     * Alert Syntax: "cardclick (cardClassString)" [mouseButton] [clickCount]
     */
    public static final String CARD_CLICK = "cardclick";
    public static final double SCALE = 0.75;
    private CardText cardText;
    private CardStatus status;
    final Text text;
    Card card;
    final UICard uicard;

    public CardDisplayUnit(UI ui, Vector2f pos) {
        super(ui, pos, UICard.CARD_DIMENSIONS.copy().scale((float) SCALE), "");
        this.text = new Text(ui, new Vector2f((float) this.getWidth(false) / 2, (float) -this.getHeight(false) / 2), "0",
                50, 14, Game.DEFAULT_FONT, 20, -1, 1);
        this.addChild(this.text);
        this.uicard = new UICard(ui, null, null);
        this.setCardText(null);
        this.setCount(-1);
        this.status = CardStatus.HAND;
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        this.alert(CARD_CLICK + " " + cardText.toString(), button, clickCount);
    }

    public void setCardText(CardText cardText) {
        this.cardText = cardText;
        if (this.cardText == null) {
            this.card = null;
        } else {
            this.card = cardText.constructInstance(null);
            this.card.status = this.status;
            this.uicard.setCard(this.card);
        }
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
            m.health = this.card.finalBasicStatEffects.getStat(EffectStats.HEALTH) + health;
        }
    }

    @Override
    public void drawSelf(Graphics g) {
        super.drawSelf(g);
        if (this.card != null) {
            this.uicard.drawCard(g, this.getCenterAbsPos(), SCALE);
        }
    }

}
