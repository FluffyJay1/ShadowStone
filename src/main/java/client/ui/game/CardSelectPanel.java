package client.ui.game;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import client.ui.*;
import server.Board;
import server.card.Card;
import server.card.effect.*;

import java.util.function.Function;

public class CardSelectPanel extends UIBox {
    final UIBoard uib;
    final UnleashButton ub;
    final ScrollingContext scroll;
    Tooltip currTooltip;
    UICard lastCardSelected;
    final TooltipDisplayPanel tooltipPanel;
    final Text effects, info;
    final CardSelectTooltipPanel tooltipReferencePanel;
    private String trackerText;

    public CardSelectPanel(UI ui, UIBoard b) {
        super(ui, new Vector2f(-0.35f, -0.14f), new Vector2f(450, 550), "ui/uiboxborder.png");
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
            this.updateTrackerText();
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
            String infoText = this.trackerText;
            if (this.uib.selectedCard.getCard().finalStats.get(Stat.SPELLBOOSTABLE) > 0) {
                infoText += "<b>S</b>: " + this.uib.selectedCard.getCard().spellboosts;
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
                    effectstext.append("<b>></b> ").append(e.description);
                    if (e.untilTurnEndTeam != null) {
                        effectstext.append(" (Removed ");
                        if (e.owner.team * e.untilTurnEndTeam * -1 != this.uib.b.getCurrentPlayerTurn() && (e.untilTurnEndCount == null || e.untilTurnEndCount.intValue() <= 0 || (!this.uib.b.getPhase().equals(Board.Phase.AFTER_TURN) && e.untilTurnEndCount.intValue() == 1))) {
                            effectstext.append("at the end of this turn");
                        } else {
                            effectstext.append("after ").append(e.untilTurnEndCount == null ? 1 : e.untilTurnEndCount.intValue()).append(" of ");
                            effectstext.append(switch(e.untilTurnEndTeam) {
                                case 1 -> "your";
                                case -1 -> "your opponent's";
                                default -> "either players'";
                            });
                            effectstext.append(" turns pass");
                        }
                        effectstext.append(".)");
                    }
                    if (e.mute) {
                        effectstext.append(" [<b>MUTED</b>]");
                    }
                    effectstext.append("\n");
                }
            }
            this.effects.setText(effectstext.toString());
            this.effects.setPos(new Vector2f(-this.getWidth(true) / 2,
                    lastBottom + 10), 0.99);
        } else {
            this.setVisible(false);
        }

    }

    public void updateTrackerText() {
        StringBuilder sb = new StringBuilder();
        if (this.uib.selectedCard != null) {
            Card card = this.uib.selectedCard.getCard();
            for (Function<Card, String> trackerfn : card.getTooltip().trackers) {
                String text = trackerfn.apply(card);
                if (text != null && !text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
        }
        this.trackerText = sb.toString();
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
