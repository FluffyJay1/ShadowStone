package client.ui.game;

import java.awt.Color;
import java.util.*;
import java.util.List;

import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.*;

import client.Game;
import client.tooltip.*;
import client.ui.*;
import server.card.*;
import server.card.effect.*;
import server.card.unleashpower.*;

public class UICard extends UIBox {
    public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 180);
    public static final Vector2f COST_POS = new Vector2f(-0.5f, -0.5f);
    public static final Vector2f COST_ALIGN = new Vector2f(0.5f, 0.5f);
    public static final Vector2f COST_POS_UNLEASHPOWER = new Vector2f(0, -0.25f);
    public static final Vector2f COST_ALIGN_UNLEASHPOWER = new Vector2f(0, 0.5f);
    public static final Vector2f COUNTDOWN_POS = new Vector2f(0.3f, 0.3f);
    public static final Vector2f COUNTDOWN_ALIGN = new Vector2f(-0.5f, -0.5f);
    public static final Vector2f MINION_STAT_ALIGN_BOARD = new Vector2f(0, -0.5f);
    public static final float MINION_STAT_POS_OFFSET_BOARD = 0.4f;
    public static final Vector2f MINION_STAT_ALIGN_HAND = new Vector2f(0.5f, -0.5f);
    public static final float MINION_STAT_POS_CENTER_HAND = 0.25f;
    public static final float MINION_STAT_POS_OFFSET_HAND = 0.25f;
    public static final float UNLEASH_POWER_RADIUS = 50;
    public static final double NAME_FONT_SIZE = 30;
    public static final double STAT_DEFAULT_SIZE = 30;
    public static final int ICON_SPACING = 32;
    public static final String CARD_CLICK = "cardclick";
    private Card card;
    private Image cardImage, subImage;
    private UIBoard uib;
    private List<Image> icons;

    public UICard(UI ui, UIBoard uib, Card c) {
        super(ui, new Vector2f(), CARD_DIMENSIONS, "");
        this.uib = uib;
        this.setCard(c);
        this.icons = new LinkedList<>();
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        // this.uib.draggingCard = this;
        this.uib.mousePressedCard(this, button, x, y);
        this.setZ(UIBoard.CARD_DRAGGING_Z);
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        this.uib.mouseReleased(button, x, y);
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {

    }

    public void setCard(Card card) {
        this.card = card;
        if (card != null && card instanceof UnleashPower) {
            this.setDim(new Vector2f(UNLEASH_POWER_RADIUS * 2, UNLEASH_POWER_RADIUS * 2));
            this.hitcircle = true;
        }
    }

    public Card getCard() {
        return this.card;
    }

    public Minion getMinion() {
        return (Minion) this.card;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (!this.getHide() && this.card != null) {
            this.drawCard(g, this.getFinalPos(), this.getScale());
            // g.drawString("" + this.getZ(), this.getFinalPos().x,
            // this.getFinalPos().y);
        }
    }

    public void drawCard(Graphics g, Vector2f pos, double scale) {
        this.drawCardArt(g, pos, scale);
        g.drawString(this.card.cardPosToString(), pos.x, pos.y);
        switch (this.card.status) {
        case BOARD:
        case LEADER:
            this.drawOnBoard(g, pos, scale);
            break;
        case UNLEASHPOWER:
            this.drawUnleashPower(g, pos, scale);
            break;
        case HAND:
            this.drawInHand(g, pos, scale);
            break;
        default:
            break;
        }
    }

    public void drawCardArt(Graphics g, Vector2f pos, double scale) {
        // get image if it doesn't exist
        if (this.cardImage == null && this.card.tooltip.imagepath != null) {
            this.cardImage = Game.getImage(this.card.tooltip.imagepath).getScaledCopy((int) this.getOriginalDim().x,
                    (int) this.getOriginalDim().y);
        }
        // scale it
        Image scaledCopy = this.cardImage.getScaledCopy((float) scale);
        // for an unleash power, draw it in a circle
        if (this.card instanceof UnleashPower) {
            Circle c = new Circle(pos.x, pos.y, (float) (UNLEASH_POWER_RADIUS * scale));
            g.texture(c, scaledCopy, true);
        } else {
            // if its a minion on board, zoom in
            if (this.card instanceof Minion && this.card.status.equals(CardStatus.BOARD)) {
                TooltipMinion tooltip = (TooltipMinion) this.card.tooltip;
                if (this.subImage == null) {
                    if (tooltip.artFocusScale <= 0) {
                        // use original art
                        this.subImage = this.cardImage.copy();
                    } else {
                        // for maximum resolution
                        Image originalImage = Game.getImage(this.card.tooltip.imagepath);
                        Image scaledOriginal = originalImage.getScaledCopy(
                                (int) (CARD_DIMENSIONS.x * tooltip.artFocusScale),
                                (int) (CARD_DIMENSIONS.y * tooltip.artFocusScale));
                        double normalizedFocusX = tooltip.artFocusPos.x / originalImage.getWidth() * CARD_DIMENSIONS.x;
                        double normalizedFocusY = tooltip.artFocusPos.y / originalImage.getHeight() * CARD_DIMENSIONS.y;
                        this.subImage = scaledOriginal.getSubImage(
                                (int) (normalizedFocusX * tooltip.artFocusScale - CARD_DIMENSIONS.x / 2),
                                (int) (normalizedFocusY * tooltip.artFocusScale - CARD_DIMENSIONS.y / 2),
                                (int) (CARD_DIMENSIONS.x), (int) (CARD_DIMENSIONS.y));
                    }
                }
                scaledCopy = this.subImage.getScaledCopy((float) scale);
            }

            g.drawImage(scaledCopy, (int) (pos.x - scaledCopy.getWidth() / 2),
                    (int) (pos.y - scaledCopy.getHeight() / 2));
        }
    }

    public void drawUnleashPower(Graphics g, Vector2f pos, double scale) {
        this.drawCostStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.COST),
                this.card.finalBasicStatEffects.getStat(EffectStats.COST), COST_POS_UNLEASHPOWER,
                COST_ALIGN_UNLEASHPOWER, STAT_DEFAULT_SIZE);

        if (this.uib.b.getPlayer(this.card.team).realPlayer.canUnleash() && !this.uib.b.disableInput) {
            g.setColor(org.newdawn.slick.Color.cyan);
            g.drawOval((float) (pos.x - UNLEASH_POWER_RADIUS * scale), (float) (pos.y - UNLEASH_POWER_RADIUS * scale),
                    (float) (UNLEASH_POWER_RADIUS * 2 * scale), (float) (UNLEASH_POWER_RADIUS * 2 * scale));
            g.setColor(org.newdawn.slick.Color.white);
        }
    }

    public void drawOnBoard(Graphics g, Vector2f pos, double scale) {
        if (this.card.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
            this.drawStatNumber(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.COUNTDOWN), COUNTDOWN_POS,
                    COUNTDOWN_ALIGN, 50, Color.white);
        }
        if (this.card instanceof Minion) {
            // TODO fix this so it looks like it can
            // attack before unleash animation
            // finishes
            if (this.card.realCard != null && this.card.realCard instanceof Minion
                    && ((Minion) this.card.realCard).canAttack() && this.getMinion().canAttack()) {
                if (this.getMinion().summoningSickness
                        && ((Minion) this.card.realCard).finalStatEffects.getStat(EffectStats.RUSH) > 0
                        && ((Minion) this.card.realCard).finalStatEffects.getStat(EffectStats.STORM) == 0) {
                    g.setColor(org.newdawn.slick.Color.yellow);
                } else {
                    g.setColor(org.newdawn.slick.Color.cyan);
                }

                g.drawRect((float) (pos.x - CARD_DIMENSIONS.x * scale / 2),
                        (float) (pos.y - CARD_DIMENSIONS.y * scale / 2), (float) (CARD_DIMENSIONS.x * scale),
                        (float) (CARD_DIMENSIONS.y * scale));
                g.setColor(org.newdawn.slick.Color.white);
            }
            if (this.card.finalStatEffects.getStat(EffectStats.WARD) > 0) {
                Image i = Game.getImage("res/game/shield.png");
                i = i.getScaledCopy((float) scale);
                g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            }
            this.drawIcons(g, pos, scale);
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.ATTACK),
                    this.card.finalBasicStatEffects.getStat(EffectStats.ATTACK),
                    new Vector2f(-MINION_STAT_POS_OFFSET_BOARD, 0.5f), MINION_STAT_ALIGN_BOARD, STAT_DEFAULT_SIZE);
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.MAGIC),
                    this.card.finalBasicStatEffects.getStat(EffectStats.MAGIC), new Vector2f(0, 0.5f),
                    MINION_STAT_ALIGN_BOARD, STAT_DEFAULT_SIZE);
            this.drawHealthStat(g, pos, scale, this.getMinion().health,
                    this.card.finalStatEffects.getStat(EffectStats.HEALTH),
                    this.card.finalBasicStatEffects.getStat(EffectStats.HEALTH),
                    new Vector2f(MINION_STAT_POS_OFFSET_BOARD, 0.5f), MINION_STAT_ALIGN_BOARD, STAT_DEFAULT_SIZE);
        }
    }

    // called by updateEffectStats in Card
    public void updateIconList() {
        this.icons.clear();
        if (this.card.finalStatEffects.getStat(EffectStats.BANE) > 0) {
            this.icons.add(Game.getImage("res/game/baneicon.png"));
        }
        if (this.card.finalStatEffects.getStat(EffectStats.POISONOUS) > 0) {
            this.icons.add(Game.getImage("res/game/poisonousicon.png"));
        }
        if (this.card instanceof Minion && !this.getMinion().lastWords().isEmpty()) {
            this.icons.add(Game.getImage("res/game/lastwordsicon.png"));
        }
    }

    public void drawIcons(Graphics g, Vector2f pos, double scale) {
        int numIcons = this.icons.size();
        for (int i = 0; i < numIcons; i++) {
            Image scaled = this.icons.get(i).getScaledCopy((float) scale);
            g.drawImage(scaled, pos.x - scaled.getWidth() / 2 - (i - (numIcons - 1f) / 2) * ICON_SPACING,
                    pos.y - scaled.getHeight() / 2 + CARD_DIMENSIONS.y * (float) scale / 2);
        }
    }

    public void drawInHand(Graphics g, Vector2f pos, double scale) {
        UnicodeFont font = Game.getFont("Verdana", (NAME_FONT_SIZE * scale), true, false);
        // TODO: magic number below is space to display mana cost
        if (font.getWidth(this.card.tooltip.name) > (CARD_DIMENSIONS.x - 20) * scale) {
            font = Game.getFont("Verdana",
                    (NAME_FONT_SIZE * scale * (CARD_DIMENSIONS.x - 20) * scale / font.getWidth(this.card.tooltip.name)),
                    true, false);
        }
        font.drawString(pos.x - font.getWidth(this.card.tooltip.name) / 2,
                pos.y - CARD_DIMENSIONS.y * (float) scale / 2, this.card.tooltip.name);
        if (this.card.realCard != null
                && this.card.realCard.board.getPlayer(this.card.team).canPlayCard(this.card.realCard)
                && this.uib.b.getPlayer(this.card.team).canPlayCard(this.card)) {
            g.setColor(org.newdawn.slick.Color.cyan);
            g.drawRect((float) (pos.x - CARD_DIMENSIONS.x * scale / 2), (float) (pos.y - CARD_DIMENSIONS.y * scale / 2),
                    (float) (CARD_DIMENSIONS.x * scale), (float) (CARD_DIMENSIONS.y * scale));
            g.setColor(org.newdawn.slick.Color.white);
        }
        this.drawCostStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.COST),
                this.card.finalBasicStatEffects.getStat(EffectStats.COST), COST_POS, COST_ALIGN, STAT_DEFAULT_SIZE);
        if (this.card instanceof Minion) {
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.ATTACK),
                    this.card.finalBasicStatEffects.getStat(EffectStats.ATTACK),
                    new Vector2f(-0.5f, MINION_STAT_POS_CENTER_HAND - MINION_STAT_POS_OFFSET_HAND),
                    MINION_STAT_ALIGN_HAND, STAT_DEFAULT_SIZE);
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.MAGIC),
                    this.card.finalBasicStatEffects.getStat(EffectStats.MAGIC),
                    new Vector2f(-0.5f, MINION_STAT_POS_CENTER_HAND), MINION_STAT_ALIGN_HAND, STAT_DEFAULT_SIZE);
            this.drawHealthStat(g, pos, scale, this.getMinion().health,
                    this.card.finalStatEffects.getStat(EffectStats.HEALTH),
                    this.card.finalBasicStatEffects.getStat(EffectStats.HEALTH),
                    new Vector2f(-0.5f, MINION_STAT_POS_CENTER_HAND + MINION_STAT_POS_OFFSET_HAND),
                    MINION_STAT_ALIGN_HAND, STAT_DEFAULT_SIZE);
        }
    }

    public void drawStatNumber(Graphics g, Vector2f pos, double scale, int stat, Vector2f relpos, Vector2f textoffset,
            double fontsize, Color c) {
        UnicodeFont font = Game.getFont("Verdana", fontsize * scale, true, false, c, Color.BLACK);
        font.drawString(
                pos.x + CARD_DIMENSIONS.x * relpos.x * (float) scale + font.getWidth("" + stat) * (textoffset.x - 0.5f),
                pos.y + CARD_DIMENSIONS.y * relpos.y * (float) scale
                        + font.getHeight("" + stat) * (textoffset.y - 0.5f),
                "" + stat);
    }

    public void drawCostStat(Graphics g, Vector2f pos, double scale, int cost, int basecost, Vector2f relpos,
            Vector2f textoffset, double fontsize) {
        Color c = Color.white;
        if (cost > basecost) {
            c = Color.red;
        }
        if (cost < basecost) {
            c = Color.green;
        }
        this.drawStatNumber(g, pos, scale, cost, relpos, textoffset, fontsize, c);
    }

    public void drawOffensiveStat(Graphics g, Vector2f pos, double scale, int stat, int basestat, Vector2f relpos,
            Vector2f textoffset, double fontsize) {
        Color c = Color.white;
        if (stat > basestat) {
            c = Color.green;
        }
        if (stat < basestat) {
            c = Color.orange;
        }
        this.drawStatNumber(g, pos, scale, stat, relpos, textoffset, fontsize, c);
    }

    public void drawHealthStat(Graphics g, Vector2f pos, double scale, int health, int maxhealth, int basehealth,
            Vector2f relpos, Vector2f textoffset, double fontsize) {
        Color c = Color.white;
        if (health < maxhealth) {
            c = Color.red;
        } else if (health > basehealth) {
            c = Color.green;
        }
        this.drawStatNumber(g, pos, scale, health, relpos, textoffset, fontsize, c);
    }
}
