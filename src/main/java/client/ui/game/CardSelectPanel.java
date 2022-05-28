package client.ui.game;

import client.Game;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import client.ui.*;
import server.card.effect.*;

public class CardSelectPanel extends UIBox {
    final UIBoard uib;
    final UnleashButton ub;
    final ScrollingContext scroll;
    Tooltip currTooltip;
    UICard lastCardSelected;
    final TooltipDisplayPanel tooltipPanel;
    final Text effects, info;
    final CardSelectTooltipPanel tooltipReferencePanel;

    public CardSelectPanel(UI ui, UIBoard b) {
        super(ui, new Vector2f(-0.35f, -0.14f), new Vector2f(450, 550), "res/ui/uiboxborder.png");
        this.relpos = true;
        this.uib = b;
        this.margins.set(10, 10);
        this.tooltipReferencePanel = new CardSelectTooltipPanel(ui, new Vector2f(450, 0), 2);
        this.tooltipReferencePanel.setReferenceTooltip(null);
        this.addChild(tooltipReferencePanel);
        this.scroll = new ScrollingContext(ui, new Vector2f(), this.getDim(true));
        this.scroll.clip = true;
        this.addChild(this.scroll);
        this.tooltipPanel = new TooltipDisplayPanel(ui) {
            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                if (this.pointIsInHitbox(x, y)) {
                    ((CardSelectPanel) this.getParent().getParent()).tooltipReferencePanel
                            .setReferenceTooltip(this.tooltip);
                } else {
                    ((CardSelectPanel) this.getParent().getParent()).tooltipReferencePanel.setReferenceTooltip(null);
                }
            }
        };
        this.tooltipPanel.setPos(new Vector2f(0, -this.getHeight(true) / 2), 1);
        this.scroll.addChild(this.tooltipPanel);
        this.ub = new UnleashButton(ui, uib);
        this.scroll.addChild(this.ub);
        this.effects = new Text(ui, new Vector2f(-this.scroll.getWidth(true) / 2, 200), "effects", this.getWidth(true),
                20, 24, -1, -1);
        this.scroll.addChild(this.effects);
        this.info = new Text(ui, new Vector2f(-this.scroll.getWidth(true) / 2, 200), "info:", this.getWidth(true),
                20, 24, -1, -1);
        this.scroll.addChild(this.info);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        if (this.lastCardSelected != this.uib.selectedCard) {
            this.lastCardSelected = this.uib.selectedCard;
            this.tooltipReferencePanel.setReferenceTooltip(null);
        }
        if (this.uib.selectedCard != null) {
            // this.setPos(new Vector2f(200, 400), 1);
            this.setVisible(true);
            if (this.currTooltip != this.uib.selectedCard.getCard().getTooltip()) {
                this.currTooltip = this.uib.selectedCard.getCard().getTooltip();
                this.tooltipPanel.setTooltip(this.currTooltip);
                this.scroll.childoffset.y = 0;
            }
            float lastBottom = this.tooltipPanel.getBottom(false, false);
            if (this.ub.isVisible()) {
                this.ub.setPos(new Vector2f(0, lastBottom + 32), 1);
                lastBottom += this.ub.getHeight(false) + 32;
            }
            String infoText = "";
            if (this.uib.selectedCard.getCard().finalStatEffects.getStat(EffectStats.SPELLBOOSTABLE) > 0) {
                infoText = "Spellboosts: " + this.uib.selectedCard.getCard().spellboosts;
            }
            if (!infoText.isEmpty()) {
                this.info.setVisible(true);
                this.info.setText(infoText);
                this.info.setPos(new Vector2f(-this.getWidth(true) / 2, lastBottom + 10), 1);
                lastBottom += this.info.getHeight(false) + 10;
            } else {
                this.info.setVisible(false);
            }
            StringBuilder effectstext = new StringBuilder("Effects:\n");
            for (Effect e : this.uib.selectedCard.getCard().getEffects(false)) {
                if (!e.description.isEmpty()) {
                    effectstext.append("<b>></b> ").append(e.description).append(e.mute ? " [<b>MUTED</b>]" : "").append("\n");
                }
            }
            this.effects.setText(effectstext.toString());
            this.effects.setPos(new Vector2f(-this.getWidth(true) / 2,
                    lastBottom + 10), 0.99);
        } else {
            this.setVisible(false);
        }

    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (this.tooltipPanel.pointIsInHitbox(x, y)) {
            this.tooltipReferencePanel.setReferenceTooltip(this.uib.selectedCard.getCard().getTooltip());
        } else {
            this.tooltipReferencePanel.setReferenceTooltip(null);
        }
    }
}
