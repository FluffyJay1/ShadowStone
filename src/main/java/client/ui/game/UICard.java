package client.ui.game;

import java.awt.Color;
import java.util.*;

import org.newdawn.slick.*;
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
    public static final double SCALE_DEFAULT = 1, SCALE_HAND = 0.75, SCALE_HAND_EXPAND = 1.2,
            SCALE_BOARD = 1, SCALE_TARGETING = 1.3, SCALE_POTENTIAL_TARGET = 1.15, SCALE_ORDERING_ATTACK = 1.3,
            SCALE_COMBAT = 1.2, SCALE_PLAY = 2.5, SCALE_MOVE = 2;
    public static final int Z_DEFAULT = 0, Z_HAND = 2, Z_BOARD = 0, Z_TARGETING = 4,
            Z_MOVE = 4, Z_DRAGGING = 3;
    public static final String CARD_CLICK = "cardclick";
    private Card card;
    private Image cardImage, subImage;
    private final UIBoard uib;
    private final List<Image> icons;
    private boolean flippedOver; // draw the back of the card instead
    private int numAnimating; // ref count of how many events are animating this
    private boolean targeting;
    private boolean potentialTarget;
    private boolean orderingAttack;
    private boolean combat;
    private boolean dragging;

    public UICard(UI ui, UIBoard uib, Card c) {
        super(ui, new Vector2f(), CARD_DIMENSIONS, "");
        this.uib = uib;
        this.setCard(c);
        this.icons = new ArrayList<>();
        this.flippedOver = false;
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        // this.uib.draggingCard = this;
        if (this.flippedOver) {
            return;
        }
        this.uib.mousePressedCard(this, button, x, y);
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
        if (card instanceof UnleashPower) {
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

    public void setFlippedOver(boolean flippedOver) {
        this.flippedOver = flippedOver;
    }

    public boolean isBeingAnimated() {
        return this.numAnimating > 0;
    }

    // tell the uiboard to not do anything with the cards, an event is taking care of it
    public void useInAnimation() {
        this.numAnimating++;
    }

    public void stopUsingInAnimation() {
        this.numAnimating--;
    }

    public void setTargeting(boolean targeting) {
        this.targeting = targeting;
    }

    public void setPotentialTarget(boolean potentialTarget) {
        this.potentialTarget = potentialTarget;
    }

    public void setOrderingAttack(boolean orderingAttack) {
        this.orderingAttack = orderingAttack;
    }

    public void setCombat(boolean combat) {
        this.combat = combat;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void updateCardAnimation() {
        double scale = switch (this.card.status) {
            case BOARD, LEADER -> SCALE_BOARD;
            case HAND -> SCALE_HAND;
            default -> SCALE_DEFAULT;
        };
        int z = switch (this.card.status) {
            case BOARD, LEADER -> Z_BOARD;
            case HAND -> Z_HAND;
            default -> Z_DEFAULT;
        };
        if (this.targeting) {
            scale = SCALE_TARGETING;
            z = Z_TARGETING;
        } else if (this.card.status.equals(CardStatus.HAND) && this.card.team == this.uib.b.localteam && this.uib.expandHand) {
            scale = SCALE_HAND_EXPAND;
        }
        if (this.potentialTarget) {
            scale *= SCALE_POTENTIAL_TARGET;
        }
        if (this.orderingAttack) {
            scale *= SCALE_ORDERING_ATTACK;
        }
        if (this.combat) {
            scale *= SCALE_COMBAT;
        }
        if (this.dragging) {
            z = Math.max(z, Z_DRAGGING);
        }
        this.setScale(scale);
        this.setZ(z);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        if (!this.isBeingAnimated()) {
            this.updateCardAnimation();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isVisible() && this.card != null) {
            this.drawCard(g, this.getAbsPos(), this.getScale());
        }
        this.drawChildren(g);
    }

    public void drawCard(Graphics g, Vector2f pos, double scale) {
        if (this.flippedOver) {
            this.drawCardBack(g, pos, scale);
            return;
        }
        this.drawCardArt(g, pos, scale);
        switch (this.card.status) {
            case BOARD, LEADER -> this.drawOnBoard(g, pos, scale);
            case UNLEASHPOWER -> this.drawUnleashPower(g, pos, scale);
            case HAND, DECK -> this.drawInHand(g, pos, scale);
            default -> {
            }
        }
    }

    public void drawCardBack(Graphics g, Vector2f pos, double scale) {
        Image image = Game.getImage("res/ui/cardback.png").getScaledCopy((int) this.getOriginalDim().x,
                (int) this.getOriginalDim().y).getScaledCopy((float) scale);
        g.drawImage(image, (int) (pos.x - image.getWidth() / 2),
                (int) (pos.y - image.getHeight() / 2));
    }

    public void drawCardArt(Graphics g, Vector2f pos, double scale) {
        // get image if it doesn't exist
        if (this.cardImage == null && this.card.getTooltip().imagepath != null) {
            this.cardImage = Game.getImage(this.card.getTooltip().imagepath).getScaledCopy((int) this.getOriginalDim().x,
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
                TooltipMinion tooltip = (TooltipMinion) this.card.getTooltip();
                if (this.subImage == null) {
                    if (tooltip.artFocusScale <= 0) {
                        // use original art
                        this.subImage = this.cardImage.copy();
                    } else {
                        // for maximum resolution
                        Image originalImage = Game.getImage(this.card.getTooltip().imagepath);
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

        if (this.card.team == this.uib.b.localteam ? // different rules depending on allied team or enemy team
                this.uib.b.realBoard.getPlayer(this.card.realCard.team).canUnleash() && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                this.uib.b.getPlayer(this.card.team).canUnleash() // condition for cards on the enemy team (should wait for animations)
        ) {
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
            if (this.card.realCard != null && this.card.realCard instanceof Minion
                    && (this.card.team == this.uib.b.localteam ? // different rules depending on allied team or enemy team
                        ((Minion) this.card.realCard).canAttack() && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                        ((Minion) this.card).canAttack()) // condition for cards on the enemy team (should wait for animations)
            ) {
                if (this.getMinion().summoningSickness
                        && this.card.realCard.finalStatEffects.getStat(EffectStats.RUSH) > 0
                        && this.card.realCard.finalStatEffects.getStat(EffectStats.STORM) == 0) {
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
        if (this.card instanceof Minion && !this.getMinion().getResolvers(Effect::lastWords).isEmpty()) {
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
        if (font.getWidth(this.card.getTooltip().name) > (CARD_DIMENSIONS.x - 20) * scale) {
            font = Game.getFont("Verdana",
                    (NAME_FONT_SIZE * scale * (CARD_DIMENSIONS.x - 20) * scale / font.getWidth(this.card.getTooltip().name)),
                    true, false);
        }
        font.drawString(pos.x - font.getWidth(this.card.getTooltip().name) / 2,
                pos.y - CARD_DIMENSIONS.y * (float) scale / 2, this.card.getTooltip().name);
        if (this.card.realCard != null
                && (this.card.team == this.uib.b.localteam ? // different rules depending on allied team or enemy team
                this.uib.b.realBoard.getPlayer(this.card.realCard.team).canPlayCard(this.card.realCard) && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                this.uib.b.getPlayer(this.card.team).canPlayCard(this.card)) // condition for cards on the enemy team (should wait for animations)
        ) {
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
