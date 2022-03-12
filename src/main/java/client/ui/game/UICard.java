package client.ui.game;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.Game;
import client.tooltip.*;
import client.ui.*;
import server.UnleashPower;
import server.card.*;
import server.card.effect.*;
import utils.PendingManager;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public class UICard extends UIBox {
    public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 180);
    public static final Vector2f BORDER_DIMENSIONS = new Vector2f(158, 188);
    public static final Vector2f COST_POS = new Vector2f(-0.4f, -0.4f);
    public static final Vector2f COST_POS_UNLEASHPOWER = new Vector2f(0, -0.3f);
    public static final Vector2f COUNTDOWN_POS = new Vector2f(0.3f, 0.1f);
    public static final float MINION_STAT_POS_BASE_BOARD = 0.4f;
    public static final float MINION_STAT_POS_OFFSET_BOARD = 0.4f;
    public static final float MINION_STAT_POS_BASE_HAND = -0.4f;
    public static final float MINION_STAT_POS_CENTER_HAND = 0.2f;
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
    private static final double PENDING_PLAY_TIME_PER_CYCLE = 0.4;
    private static final float PENDING_PLAY_ELLIPSIS_SPACING = 0.2f;
    private static final float PENDING_PLAY_ELLIPSIS_SIZE = 20f;
    private static final Color PENDING_PLAY_COLOR = new Color(0.6f, 0.6f, 0.7f, 1);
    private static final Color PENDING_PLAY_POSITION_COLOR = new Color(0.8f, 0.8f, 1f, 0.4f);
    private static final float STAT_ICON_DEFAULT_SCALE = 0.6f;
    private static final float STAT_ICON_COUNTDOWN_SCALE = 1;
    private static final float HAND_TITLE_OFFSET = 0.2f;
    private static final float ICON_Y = 0.6f;
    private static final int READY_BORDER_WIDTH = 2;
    private static final int READY_BORDER_PADDING = 4;

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
    private final Set<PendingManager<?>> pendingSources;
    private double pendingTimer;

    public UICard(UI ui, UIBoard uib, Card c) {
        super(ui, new Vector2f(), CARD_DIMENSIONS, "");
        this.uib = uib;
        this.setCard(c);
        this.icons = new ArrayList<>();
        this.flippedOver = false;
        this.pendingSources = new HashSet<>();
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

    public void addPendingSource(PendingManager<?> source) {
        this.pendingSources.add(source);
    }

    public void removePendingSource(PendingManager<?> source) {
        this.pendingSources.remove(source);
    }

    public boolean isPending() {
        return !this.pendingSources.isEmpty();
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
        if (this.isPending()) {
            this.pendingTimer = (this.pendingTimer + frametime) % PENDING_PLAY_TIME_PER_CYCLE;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isVisible() && this.card != null) {
            Vector2f absPos = this.getAbsPos();
            this.drawCard(g, absPos, this.getScale());
            if (this.isPending()) {
                float size = (float) this.getScale() * PENDING_PLAY_ELLIPSIS_SIZE;
                for (int i = -1; i <= 1; i++) {
                    Vector2f pos = this.getAbsPosOfLocal(this.getLocalPosOfRel(new Vector2f(i * PENDING_PLAY_ELLIPSIS_SPACING, 0)));
                    g.setColor(new Color(1f, 1f, 1f, ((1 + (float)Math.sin((-i / 3f + this.pendingTimer / PENDING_PLAY_TIME_PER_CYCLE) * Math.PI * 2)) / 2.f)));
                    g.fillOval(pos.x - size/2, pos.y - size/2, size, size);
                }
                g.setColor(Color.white);
            }
        }
        this.drawChildren(g);
    }

    public void drawCard(Graphics g, Vector2f pos, double scale) {
        if (this.flippedOver) {
            this.drawCardBack(g, pos, scale);
            return;
        }
        this.drawCardArt(g, pos, scale, this.card.status, this.isPending() ? PENDING_PLAY_COLOR : Color.white);
        if (!(this.card instanceof UnleashPower) && !(this.card instanceof Leader)) {
            this.drawCardBorder(g, pos, scale);
        }
        if (this.card.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
            this.drawStatNumber(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.COUNTDOWN), COUNTDOWN_POS,
                    50, Color.white, Game.getImage("res/game/statcountdown.png"), STAT_ICON_COUNTDOWN_SCALE);
        }
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

    public void drawCardArt(Graphics g, Vector2f pos, double scale, CardStatus status, Color filter) {
        // get image if it doesn't exist
        if (this.cardImage == null && this.card.getTooltip().imagepath != null) {
            this.cardImage = Game.getImage(this.card.getTooltip().imagepath).getScaledCopy((int) this.getOriginalDim().x,
                    (int) this.getOriginalDim().y);
        }
        // generate the zoomed in image if we don't have one yet
        if (this.subImage == null) {
            this.subImage = this.generateZoomedSubImage(this.getOriginalDim());
        }
        // for an unleash power, draw it in a circle
        if (this.card instanceof UnleashPower) {
            // scale it
            Image scaledCopy = this.subImage.getScaledCopy((float) scale);
            Circle c = new Circle(pos.x, pos.y, (float) (UNLEASH_POWER_RADIUS * scale));
            g.setColor(filter);
            g.texture(c, scaledCopy, true);
            g.setColor(Color.white);
        } else {
            Image scaledCopy;
            // if its a thing on board, zoom in
            if (status.equals(CardStatus.BOARD)) {
                scaledCopy = this.subImage.getScaledCopy((float) scale);
            } else {
                scaledCopy = this.cardImage.getScaledCopy((float) scale);
            }
            g.drawImage(scaledCopy, (int) (pos.x - scaledCopy.getWidth() / 2),
                    (int) (pos.y - scaledCopy.getHeight() / 2),
                    filter);
        }
    }

    private Image generateZoomedSubImage(Vector2f intendedDimensions) {
        TooltipCard tooltip = this.card.getTooltip();
        if (tooltip.artFocusScale <= 0) {
            // use original art
            return this.cardImage.copy();
        } else {
            // for maximum resolution
            Image originalImage = Game.getImage(tooltip.imagepath);
            Image scaledOriginal = originalImage.getScaledCopy(
                    (int) (CARD_DIMENSIONS.x * tooltip.artFocusScale),
                    (int) (CARD_DIMENSIONS.y * tooltip.artFocusScale));
            double normalizedFocusX = tooltip.artFocusPos.x / originalImage.getWidth() * CARD_DIMENSIONS.x;
            double normalizedFocusY = tooltip.artFocusPos.y / originalImage.getHeight() * CARD_DIMENSIONS.y;
            return scaledOriginal.getSubImage(
                    (int) (normalizedFocusX * tooltip.artFocusScale - intendedDimensions.x / 2),
                    (int) (normalizedFocusY * tooltip.artFocusScale - intendedDimensions.y / 2),
                    (int) (intendedDimensions.x), (int) (intendedDimensions.y));
        }
    }

    public void drawCardBorder(Graphics g, Vector2f pos, double scale) {
        String imagePath = null;
        switch (this.card.getTooltip().rarity) {
            case BRONZE -> imagePath = "res/game/borderbronze.png";
            case SILVER -> imagePath = "res/game/bordersilver.png";
            case GOLD -> imagePath = "res/game/bordergold.png";
            case LEGENDARY -> imagePath = "res/game/borderlegendary.png";
        }
        Image borderImage = Game.getImage(imagePath).getScaledCopy((int) (scale * BORDER_DIMENSIONS.x), (int) (scale * BORDER_DIMENSIONS.y));
        g.drawImage(borderImage, (int) (pos.x - borderImage.getWidth() / 2),
                (int) (pos.y - borderImage.getHeight() / 2));
    }

    public void drawUnleashPower(Graphics g, Vector2f pos, double scale) {
        if (this.card.realCard != null && this.card.realCard instanceof UnleashPower
                && (this.card.team == this.uib.b.localteam ? // different rules depending on allied team or enemy team
                this.uib.b.realBoard.getPlayer(this.card.realCard.team).canUnleash() && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                this.uib.b.getPlayer(this.card.team).canUnleash()) // condition for cards on the enemy team (should wait for animations)
        ) {
            g.setColor(Color.cyan);
            g.setLineWidth(READY_BORDER_WIDTH);
            g.drawOval((float) (pos.x - UNLEASH_POWER_RADIUS * scale - READY_BORDER_PADDING),
                    (float) (pos.y - UNLEASH_POWER_RADIUS * scale - READY_BORDER_PADDING),
                    (float) (UNLEASH_POWER_RADIUS * 2 * scale + READY_BORDER_PADDING * 2),
                    (float) (UNLEASH_POWER_RADIUS * 2 * scale + READY_BORDER_PADDING * 2));
            g.setColor(org.newdawn.slick.Color.white);
        }
        this.drawCostStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.COST),
                this.card.finalBasicStatEffects.getStat(EffectStats.COST), COST_POS_UNLEASHPOWER, STAT_DEFAULT_SIZE);
    }

    public void drawOnBoard(Graphics g, Vector2f pos, double scale) {
        this.drawIcons(g, pos, scale);
        if (this.card instanceof Minion) {
            if (this.card.realCard != null && this.card.realCard instanceof Minion
                    && (this.card.team == this.uib.b.localteam ? // different rules depending on allied team or enemy team
                        ((Minion) this.card.realCard).canAttack() && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                        ((Minion) this.card).canAttack()) // condition for cards on the enemy team (should wait for animations)
            ) {
                Color borderColor;
                if (this.getMinion().summoningSickness
                        && this.card.realCard.finalStatEffects.getStat(EffectStats.RUSH) > 0
                        && this.card.realCard.finalStatEffects.getStat(EffectStats.STORM) == 0) {
                    borderColor = Color.yellow;
                } else {
                    borderColor = Color.cyan;
                }
                drawReadyBorder(g, pos, scale, borderColor);
            }
            if (this.card.finalStatEffects.getStat(EffectStats.WARD) > 0) {
                Image i = Game.getImage("res/game/shield.png");
                i = i.getScaledCopy((float) scale);
                g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            }
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.ATTACK),
                    this.card.finalBasicStatEffects.getStat(EffectStats.ATTACK),
                    new Vector2f(-MINION_STAT_POS_OFFSET_BOARD, MINION_STAT_POS_BASE_BOARD), STAT_DEFAULT_SIZE, Game.getImage("res/game/statattack.png"));
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.MAGIC),
                    this.card.finalBasicStatEffects.getStat(EffectStats.MAGIC),
                    new Vector2f(0, MINION_STAT_POS_BASE_BOARD), STAT_DEFAULT_SIZE, Game.getImage("res/game/statmagic.png"));
            this.drawHealthStat(g, pos, scale, this.getMinion().health,
                    this.card.finalStatEffects.getStat(EffectStats.HEALTH),
                    this.card.finalBasicStatEffects.getStat(EffectStats.HEALTH),
                    new Vector2f(MINION_STAT_POS_OFFSET_BOARD, MINION_STAT_POS_BASE_BOARD), STAT_DEFAULT_SIZE);
        }
        // if marked for death
        if (!this.card.alive) {
            Image i = Game.getImage("res/game/markedfordeath.png");
            i = i.getScaledCopy((float) scale);
            g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
        }
    }

    private void drawReadyBorder(Graphics g, Vector2f pos, double scale, Color color) {
        g.setLineWidth(READY_BORDER_WIDTH);
        g.setColor(color);
        g.drawRect((float) (pos.x - BORDER_DIMENSIONS.x * scale / 2 - READY_BORDER_PADDING),
                (float) (pos.y - BORDER_DIMENSIONS.y * scale / 2 - READY_BORDER_PADDING),
                (float) (BORDER_DIMENSIONS.x * scale + READY_BORDER_PADDING * 2),
                (float) (BORDER_DIMENSIONS.y * scale + READY_BORDER_PADDING * 2));
        g.setColor(Color.white);
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
        if (this.card instanceof BoardObject && !((BoardObject) this.card).lastWords().isEmpty()) {
            this.icons.add(Game.getImage("res/game/lastwordsicon.png"));
        }
    }

    public void drawIcons(Graphics g, Vector2f pos, double scale) {
        int numIcons = this.icons.size();
        for (int i = 0; i < numIcons; i++) {
            Image scaled = this.icons.get(i).getScaledCopy((float) scale);
            g.drawImage(scaled, pos.x - scaled.getWidth() / 2 - (i - (numIcons - 1f) / 2) * ICON_SPACING,
                    pos.y - scaled.getHeight() / 2 + CARD_DIMENSIONS.y * ICON_Y * (float) scale);
        }
    }

    public void drawInHand(Graphics g, Vector2f pos, double scale) {
        UnicodeFont font = Game.getFont(Game.DEFAULT_FONT, (NAME_FONT_SIZE * scale), true, false);
        double targetWidth = CARD_DIMENSIONS.x * scale * (1 - HAND_TITLE_OFFSET);
        if (font.getWidth(this.card.getTooltip().name) > targetWidth) {
            font = Game.getFont(Game.DEFAULT_FONT,
                    (NAME_FONT_SIZE * scale * targetWidth / font.getWidth(this.card.getTooltip().name)),
                    true, false);
        }
        font.drawString(pos.x - font.getWidth(this.card.getTooltip().name) / 2f + HAND_TITLE_OFFSET / 2 * CARD_DIMENSIONS.x * (float) scale,
                pos.y - CARD_DIMENSIONS.y * (float) scale / 2, this.card.getTooltip().name);
        if (this.card.realCard != null
                && (this.card.team == this.uib.b.localteam ? // different rules depending on allied team or enemy team
                this.uib.b.realBoard.getPlayer(this.card.realCard.team).canPlayCard(this.card.realCard) && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                this.uib.b.getPlayer(this.card.team).canPlayCard(this.card)) // condition for cards on the enemy team (should wait for animations)
        ) {
            drawReadyBorder(g, pos, scale, Color.cyan);
        }
        this.drawCostStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.COST),
                this.card.finalBasicStatEffects.getStat(EffectStats.COST), COST_POS, STAT_DEFAULT_SIZE);
        if (this.card instanceof Minion) {
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.ATTACK),
                    this.card.finalBasicStatEffects.getStat(EffectStats.ATTACK),
                    new Vector2f(MINION_STAT_POS_BASE_HAND, MINION_STAT_POS_CENTER_HAND - MINION_STAT_POS_OFFSET_HAND), STAT_DEFAULT_SIZE, Game.getImage("res/game/statattack.png"));
            this.drawOffensiveStat(g, pos, scale, this.card.finalStatEffects.getStat(EffectStats.MAGIC),
                    this.card.finalBasicStatEffects.getStat(EffectStats.MAGIC),
                    new Vector2f(MINION_STAT_POS_BASE_HAND, MINION_STAT_POS_CENTER_HAND), STAT_DEFAULT_SIZE, Game.getImage("res/game/statmagic.png"));
            this.drawHealthStat(g, pos, scale, this.getMinion().health,
                    this.card.finalStatEffects.getStat(EffectStats.HEALTH),
                    this.card.finalBasicStatEffects.getStat(EffectStats.HEALTH),
                    new Vector2f(MINION_STAT_POS_BASE_HAND, MINION_STAT_POS_CENTER_HAND + MINION_STAT_POS_OFFSET_HAND), STAT_DEFAULT_SIZE);
        }
    }

    public void drawStatNumber(Graphics g, Vector2f pos, double scale, int stat, Vector2f relpos,
            double fontsize, Color c, Image icon, double iconScale) {
        UnicodeFont font = Game.getFont(Game.DEFAULT_FONT, fontsize * scale, true, false, c, Color.black);
        float x = pos.x + CARD_DIMENSIONS.x * relpos.x * (float) scale;
        float y = pos.y + CARD_DIMENSIONS.y * relpos.y * (float) scale;
        Image i = icon.getScaledCopy((float) (scale * iconScale));
        g.drawImage(i, x - i.getWidth() / 2, y - i.getHeight() / 2);
        String text = "" + stat;
        font.drawString( x - font.getWidth(text) / 2f, y - font.getLineHeight() / 2f, text);
    }

    public void drawCostStat(Graphics g, Vector2f pos, double scale, int cost, int basecost, Vector2f relpos, double fontsize) {
        Color c = Color.white;
        if (cost > basecost) {
            c = Color.red;
        }
        if (cost < basecost) {
            c = Color.green;
        }
        this.drawStatNumber(g, pos, scale, cost, relpos, fontsize, c, Game.getImage("res/game/statcost.png"), STAT_ICON_DEFAULT_SCALE);
    }

    public void drawOffensiveStat(Graphics g, Vector2f pos, double scale, int stat, int basestat, Vector2f relpos, double fontsize, Image icon) {
        Color c = Color.white;
        if (stat > basestat) {
            c = Color.green;
        }
        if (stat < basestat) {
            c = Color.orange;
        }
        this.drawStatNumber(g, pos, scale, stat, relpos, fontsize, c, icon, STAT_ICON_DEFAULT_SCALE);
    }

    public void drawHealthStat(Graphics g, Vector2f pos, double scale, int health, int maxhealth, int basehealth,
            Vector2f relpos, double fontsize) {
        Color c = Color.white;
        if (health < maxhealth) {
            c = Color.red;
        } else if (health > basehealth) {
            c = Color.green;
        }
        this.drawStatNumber(g, pos, scale, health, relpos, fontsize, c, Game.getImage("res/game/stathealth.png"), STAT_ICON_DEFAULT_SCALE);
    }

    public void drawPendingPlayPosition(Graphics g, Vector2f drawPos) {
        Vector2f absPos = this.getAbsPos();
        g.setDrawMode(Graphics.MODE_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
        this.drawCardArt(g, drawPos, SCALE_BOARD, CardStatus.BOARD, PENDING_PLAY_POSITION_COLOR);
        g.setColor(new Color(0, 1.0f, 0, 0.3f));
        g.setLineWidth(10);
        g.drawLine(absPos.x, absPos.y, drawPos.x, drawPos.y);
        g.setDrawMode(Graphics.MODE_NORMAL);
        g.setColor(Color.white);
        g.setLineWidth(1);
    }

    public void drawPendingAttack(Graphics g, UICard pendingAttackTarget) {
        Vector2f absPos = this.getAbsPos();
        Vector2f targetPos = pendingAttackTarget.getAbsPos();
        g.setDrawMode(Graphics.MODE_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
        g.setColor(new Color(1.0f, 0.2f, 0, 0.3f));
        g.setLineWidth(10);
        g.drawLine(absPos.x, absPos.y, targetPos.x, targetPos.y);
        g.setDrawMode(Graphics.MODE_NORMAL);
        g.setColor(Color.white);
        g.setLineWidth(1);
    }

    public void drawPendingUnleash(Graphics g, UICard pendingUnleashTarget) {
        Vector2f absPos = this.getAbsPos();
        Vector2f targetPos = pendingUnleashTarget.getAbsPos();
        g.setDrawMode(Graphics.MODE_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
        g.setColor(new Color(0.9f, 0.9f, 0, 0.3f));
        g.setLineWidth(10);
        g.drawLine(absPos.x, absPos.y, targetPos.x, targetPos.y);
        g.setDrawMode(Graphics.MODE_NORMAL);
        g.setColor(Color.white);
        g.setLineWidth(1);
    }

}
