package client.ui.game;

import java.util.*;
import java.util.function.Supplier;

import client.ui.Animation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.*;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.Game;
import client.tooltip.*;
import client.ui.*;
import server.UnleashPower;
import server.card.*;
import server.card.effect.*;
import server.card.leader.Rowen;
import utils.PendingManager;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public class UICard extends UIBox {
    public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 180);
    private static final Vector2f BORDER_DIMENSIONS = new Vector2f(182, 212);
    private static final Vector2f COST_POS = new Vector2f(-0.4f, -0.4f);
    private static final Vector2f COST_POS_UNLEASHPOWER = new Vector2f(0, -0.3f);
    private static final Vector2f COUNTDOWN_POS = new Vector2f(0.15f, 0.15f);
    private static final Vector2f DAMAGE_MODIFIERS_POS_HAND = new Vector2f(-0.18f, 0.42f);
    private static final float DAMAGE_MODIFIERS_SPREAD_HAND = 0.2f;
    private static final Vector2f DAMAGE_MODIFIERS_POS_BOARD = new Vector2f(0.4f, 0.2f);
    private static final float DAMAGE_MODIFIERS_SPREAD_BOARD = 0.1f;
    private static final float MINION_STAT_POS_BASE_BOARD = 0.4f;
    private static final float MINION_STAT_POS_OFFSET_BOARD = 0.4f;
    private static final float MINION_STAT_POS_BASE_HAND = -0.4f;
    private static final float MINION_STAT_POS_CENTER_HAND = 0.2f;
    private static final float MINION_STAT_POS_OFFSET_HAND = 0.22f;
    private static final float UNLEASH_POWER_RADIUS = 50;
    private static final double NAME_FONT_SIZE = 30;
    private static final double TRAITS_FONT_SIZE = 20;
    private static final double STAT_DEFAULT_SIZE = 30;
    public static final float SCALE_DEFAULT = 1, SCALE_HAND = 0.75f, SCALE_HAND_EXPAND = 1.2f,
            SCALE_BOARD = 1f, SCALE_TARGETING = 1.3f, SCALE_POTENTIAL_TARGET = 1.15f, SCALE_ORDERING_ATTACK = 1.3f,
            SCALE_COMBAT = 1.2f, SCALE_PLAY = 2.5f, SCALE_MOVE = 2, SCALE_MULLIGAN = 1.5f, SCALE_PLAY_PENDING = 1.25f;
    public static final int Z_DEFAULT = 0, Z_HAND = 2, Z_BOARD = 0, Z_TARGETING = 4,
            Z_MOVE = 4, Z_DRAGGING = 3, Z_MULLIGAN = 6;
    private static final double PENDING_TIME_PER_CYCLE = 0.4;
    private static final float PENDING_ELLIPSIS_SPACING = 0.2f;
    private static final float PENDING_ELLIPSIS_SIZE = 20f;
    private static final Color PENDING_COLOR = new Color(0.6f, 0.6f, 0.7f, 1);
    private static final Color PENDING_POSITION_COLOR = new Color(0.8f, 0.8f, 1f, 0.4f);
    private static final float STAT_ICON_DEFAULT_SCALE = 0.6f;
    private static final float STAT_ICON_COUNTDOWN_SCALE = 1;
    private static final float HAND_TITLE_OFFSET = 0.2f;
    private static final int ICON_SPACING = 32;
    private static final float ICON_SCALE = 0.75f;
    private static final float ICON_Y = 0.55f;
    private static final int READY_BORDER_WIDTH = 2;
    private static final int READY_BORDER_PADDING = 6;
    private static final Color LOCKED_COLOR = new Color(0.3f, 0.2f, 0.3f);
    private static final double STAT_CHANGE_FONT_INCREASE_TIME = 0.4;
    private static final double STAT_CHANGE_FONT_INCREASE_SCALE = 2;

    private static final Supplier<EmissionStrategy> STEALTH_PARTICLES = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(3, 0.08),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/smoke.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.15, new Vector2f(0, 10),
                            () -> new QuadraticInterpolationB(0, 0, 1),
                            () -> new QuadraticInterpolationB(1.5, 1.7, 0.4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(75),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 10)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-50, 50))
            ))
    );

    private static final Supplier<EmissionStrategy> SPECIAL_CONDITION_PARTICLES = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(12, 0.08),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/sparkle.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 1, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(0, 0, 1),
                            () -> new QuadraticInterpolationB(0.6, 0.8, 0.4)
                    ),
                    new RectangleEdgePositionEmissionPropertyStrategy(CARD_DIMENSIONS),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 10)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-10, 10))
            ))
    );

    private static final Supplier<EmissionStrategy> STALWART_PARTICLES = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 1.5),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/board/stalwart.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationA(0, 0, -4),
                            () -> new LinearInterpolation(0.8, 1.2)
                    )
            ))
    );

    private static final Supplier<EmissionStrategy> MUTED_PARTICLES = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 3),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/board/mute.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(3.1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationA(0, 0, -1),
                            () -> new SequentialInterpolation<>(
                                    List.of(new ComposedInterpolation<>(new SpringInterpolation(1), new LinearInterpolation(1, 1.2)),
                                            new ComposedInterpolation<>(new SpringInterpolation(1), new LinearInterpolation(1.2, 1))),
                                    List.of(1., 1.)
                            )
                    ),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(75, 125))
            ))
    );

    private static final Supplier<EmissionStrategy> UNYIELDING_PARTICLES = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 0.25),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/board/unyielding.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationA(0, 0, -4),
                            () -> new LinearInterpolation(0.6, 0.8)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-50, 50))
            ))
    );

    private Card card;
    private Animation cardAnimation;
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
    private final ParticleSystem stealthParticles;
    private final ParticleSystem specialConditionParticles;
    private final ParticleSystem stalwartParticles;
    private final ParticleSystem mutedParticles;
    private final ParticleSystem unyieldingParticles;
    private final EnumMap<Stat, Double> statFontSizeIncreaseAnimationTimer; // e.g. EventAnimationAddEffect bumping stats, should make font size bigger

    public UICard(UI ui, UIBoard uib, Card c) {
        super(ui, new Vector2f(), CARD_DIMENSIONS, "");
        this.uib = uib;
        this.setCard(c);
        this.icons = new ArrayList<>();
        this.updateFlippedOver();
        this.pendingSources = new HashSet<>();
        this.statFontSizeIncreaseAnimationTimer = new EnumMap<>(Stat.class);
        this.stealthParticles = new ParticleSystem(ui, new Vector2f(), STEALTH_PARTICLES.get(), true);
        this.addChild(this.stealthParticles);
        this.specialConditionParticles = new ParticleSystem(ui, new Vector2f(), SPECIAL_CONDITION_PARTICLES.get(), true);
        this.addChild(this.specialConditionParticles);
        this.stalwartParticles = new ParticleSystem(ui, new Vector2f(), STALWART_PARTICLES.get(), true);
        this.addChild(this.stalwartParticles);
        this.mutedParticles = new ParticleSystem(ui, new Vector2f(), MUTED_PARTICLES.get(), true);
        this.addChild(this.mutedParticles);
        this.unyieldingParticles = new ParticleSystem(ui, new Vector2f(MINION_STAT_POS_OFFSET_BOARD, MINION_STAT_POS_BASE_BOARD), UNYIELDING_PARTICLES.get(), true);
        this.unyieldingParticles.relpos = true;
        this.addChild(this.unyieldingParticles);
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
        this.uib.mouseReleasedCard(this, button, x, y);
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {

    }

    public void setCard(Card card) {
        this.card = card;
        this.cardAnimation = card.getCardText().getTooltip().animation.get();
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

    public boolean isFlippedOver() {
        return this.flippedOver;
    }

    public void updateFlippedOver() {
        if (this.uib != null) {
            this.setFlippedOver(!this.card.isVisibleTo(this.uib.b.getLocalteam()));
        }
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

    public void startAnimatingStatChangeFromEffect(EffectStats effectStats) {
        for (Stat stat : Stat.values()) {
            if (effectStats.set.contains(stat) || effectStats.change.get(stat) != 0) {
                this.statFontSizeIncreaseAnimationTimer.put(stat, STAT_CHANGE_FONT_INCREASE_TIME);
            }
        }
    }

    private double getStatFontSize(Stat stat) {
        if (this.statFontSizeIncreaseAnimationTimer.containsKey(stat)) {
            return STAT_CHANGE_FONT_INCREASE_SCALE * STAT_DEFAULT_SIZE;
        }
        return STAT_DEFAULT_SIZE;
    }

    public void updateCardUIProperties() {
        float scale = switch (this.card.status) {
            case BOARD, LEADER -> SCALE_BOARD;
            case HAND -> {
                if (this.card.team == this.uib.b.getLocalteam() && this.uib.b.mulligan) {
                    yield SCALE_MULLIGAN;
                } else {
                    yield SCALE_HAND;
                }
            }
            default -> SCALE_DEFAULT;
        };
        int z = switch (this.card.status) {
            case BOARD, LEADER -> Z_BOARD;
            case HAND -> {
                if (this.card.team == this.uib.b.getLocalteam() && this.uib.b.mulligan) {
                    yield Z_MULLIGAN;
                } else {
                    yield Z_HAND;
                }
            }
            default -> Z_DEFAULT;
        };
        if (this.targeting) {
            scale = SCALE_TARGETING;
            z = Z_TARGETING;
        } else if (this.card.status.equals(CardStatus.HAND) && this.card.team == this.uib.b.getLocalteam() && this.uib.expandHand) {
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
        if (this.isPending() && this.card.status.equals(CardStatus.HAND)) {
            scale *= SCALE_PLAY_PENDING;
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
        this.updateCardAnimation(frametime);
        this.stealthParticles.setScale(this.getScale());
        this.stealthParticles.setPaused(!this.card.isInPlay() || this.card.finalStats.get(Stat.STEALTH) == 0);
        if (this.card.realCard.player != null) {
            this.specialConditionParticles.setScale(this.getScale());
            this.specialConditionParticles.setPaused(this.card.team != this.uib.b.getLocalteam() || this.uib.b.disableInput || switch (this.card.status) {
                case HAND -> !this.card.realCard.player.canPlayCard(this.card.realCard) || !this.card.realCard.battlecrySpecialConditions();
                case BOARD -> !this.uib.draggingUnleash || !this.card.realCard.player.canUnleashCard(this.card.realCard)
                        || !(this.card instanceof Minion) || !((Minion) this.card.realCard).unleashSpecialConditions();
                default -> true;
            });
        }
        this.stalwartParticles.setScale(this.getScale());
        this.stalwartParticles.setPaused(!this.card.isInPlay() || this.card.finalStats.get(Stat.STALWART) == 0);
        this.mutedParticles.setScale(this.getScale());
        this.mutedParticles.setPaused(!this.card.isInPlay() || this.card.getFinalEffects(false).noneMatch(e -> e.mute));
        this.unyieldingParticles.setScale(this.getScale());
        this.unyieldingParticles.setPaused(!this.card.isInPlay() || !(this.card instanceof Minion) || this.card.finalStats.get(Stat.UNYIELDING) == 0);
        if (!this.isBeingAnimated()) {
            this.updateCardUIProperties();
            this.updateFlippedOver();
        }
        if (this.isPending()) {
            this.pendingTimer = (this.pendingTimer + frametime) % PENDING_TIME_PER_CYCLE;
        }
        for (Iterator<Map.Entry<Stat, Double>> it = this.statFontSizeIncreaseAnimationTimer.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Stat, Double> entry = it.next();
            double newTime = entry.getValue() - frametime;
            if (newTime < 0) {
                it.remove();
            } else {
                this.statFontSizeIncreaseAnimationTimer.put(entry.getKey(), newTime);
            }
        }
    }

    public void updateCardAnimation(double frametime) {
        if (this.cardAnimation != null) {
            this.cardAnimation.update(frametime);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isVisible() && this.card != null) {
            Vector2f absPos = this.getAbsPos();
            Color filterColor = Color.white;
            if (this.card instanceof UnleashPower && !this.card.player.unleashAllowed) {
                filterColor = LOCKED_COLOR;
            } else if (this.isPending()) {
                filterColor = PENDING_COLOR;
            }
            this.drawCard(g, absPos, this.getScale(), filterColor);
            if (this.isPending()) {
                float size = this.getScale() * PENDING_ELLIPSIS_SIZE;
                for (int i = -1; i <= 1; i++) {
                    Vector2f pos = this.getAbsPosOfLocal(this.getLocalPosOfRel(new Vector2f(i * PENDING_ELLIPSIS_SPACING, 0)));
                    g.setColor(new Color(1f, 1f, 1f, ((1 + (float)Math.sin((-i / 3f + this.pendingTimer / PENDING_TIME_PER_CYCLE) * Math.PI * 2)) / 2.f)));
                    g.fillOval(pos.x - size/2, pos.y - size/2, size, size);
                }
                g.setColor(Color.white);
            }
        }
    }

    public void drawCard(Graphics g, Vector2f pos, double scale, Color filter) {
        if (this.flippedOver) {
            this.drawCardBack(g, pos, scale);
            return;
        }
        this.drawCardArt(g, pos, scale, this.card.status, filter);
        if (!(this.card instanceof UnleashPower) && !(this.card instanceof Leader)) {
            this.drawCardBorder(g, pos, scale);
        }
        this.stealthParticles.draw(g);
        switch (this.card.status) {
            case BOARD, LEADER -> this.drawOnBoard(g, pos, scale);
            case UNLEASHPOWER -> this.drawUnleashPower(g, pos, scale);
            case HAND, DECK -> this.drawInHand(g, pos, scale);
            default -> {
            }
        }
        this.specialConditionParticles.draw(g);
        this.stalwartParticles.draw(g);
        this.mutedParticles.draw(g);
        this.unyieldingParticles.draw(g);
    }

    public void drawCardBack(Graphics g, Vector2f pos, double scale) {
        Image image = Game.getImage("ui/cardback.png").getScaledCopy((int) this.getOriginalDim().x,
                (int) this.getOriginalDim().y).getScaledCopy((float) scale);
        g.drawImage(image, (int) (pos.x - image.getWidth() / 2),
                (int) (pos.y - image.getHeight() / 2));
    }

    public void drawCardArt(Graphics g, Vector2f pos, double scale, CardStatus status, Color filter) {
        if (this.cardAnimation == null) {
            return;
        }
        Image currentFrame = this.cardAnimation.getCurrentFrame();
        // for an unleash power, draw it in a circle
        if (this.card instanceof UnleashPower) {
            // scale it
            Image scaledCopy = this.generateZoomedSubImage(currentFrame, this.getOriginalDim()).getScaledCopy((float) scale);
            Circle c = new Circle(pos.x, pos.y, (float) (UNLEASH_POWER_RADIUS * scale));
            g.setColor(filter);
            g.texture(c, scaledCopy, true);
            g.setColor(Color.white);
        } else {
            Image scaledCopy;
            // if its a thing on board, zoom in
            if (status.equals(CardStatus.BOARD) || status.equals(CardStatus.LEADER)) {
                scaledCopy = this.generateZoomedSubImage(currentFrame, this.getOriginalDim()).getScaledCopy((float) scale);
            } else {
                scaledCopy = currentFrame.getScaledCopy((int) this.getOriginalDim().x, (int) this.getOriginalDim().y).getScaledCopy((float) scale);
            }
            g.drawImage(scaledCopy, (int) (pos.x - scaledCopy.getWidth() / 2),
                    (int) (pos.y - scaledCopy.getHeight() / 2),
                    filter);
        }
    }

    private Image generateZoomedSubImage(Image originalImage, Vector2f intendedDimensions) {
        TooltipCard tooltip = this.card.getTooltip();
        if (tooltip.artFocusScale <= 0) {
            // use original art, scaled to fill intended dimensions
            float xr = originalImage.getWidth() / intendedDimensions.x;
            float yr = originalImage.getHeight() / intendedDimensions.y;
            Image resized = originalImage.getScaledCopy(1 / Math.min(xr, yr));
            return resized.getSubImage((int) (resized.getWidth() - intendedDimensions.x) / 2,
                    (int) (resized.getHeight() - intendedDimensions.y) / 2,
                    (int) intendedDimensions.x,
                    (int) intendedDimensions.y);
        } else {
            // for maximum resolution
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
        String type = "";
        TooltipCard tooltip = this.card.getTooltip();
        if (tooltip instanceof TooltipSpell) {
            type = "spell";
        } else if (tooltip instanceof TooltipAmulet) {
            type = "amulet";
        } else if (tooltip instanceof TooltipMinion) {
            type = "minion";
        }
        String rarity = switch (tooltip.rarity) {
            case BRONZE -> "bronze";
            case SILVER -> "silver";
            case GOLD -> "gold";
            case LEGENDARY -> "legendary";
        };
        imagePath = "game/border" + rarity + type + ".png";
        Image borderImage = Game.getImage(imagePath).getScaledCopy((int) (scale * BORDER_DIMENSIONS.x), (int) (scale * BORDER_DIMENSIONS.y));
        g.drawImage(borderImage, (int) (pos.x - borderImage.getWidth() / 2),
                (int) (pos.y - borderImage.getHeight() / 2));
    }

    public void drawUnleashPower(Graphics g, Vector2f pos, double scale) {
        if (this.card.realCard != null && this.card.realCard instanceof UnleashPower
                && (this.card.team == this.uib.b.getLocalteam() ? // different rules depending on allied team or enemy team
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
        this.drawCostStat(g, pos, scale, this.card.finalStats.get(Stat.COST),
                this.card.finalBasicStats.get(Stat.COST), COST_POS_UNLEASHPOWER, this.getStatFontSize(Stat.COST));
    }

    public void drawOnBoard(Graphics g, Vector2f pos, double scale) {
        this.drawIcons(g, pos, scale);
        if (this.card.finalStats.get(Stat.ELUSIVE) > 0) {
            Image i = Game.getImage("game/elusive.png");
            i = i.getScaledCopy((float) scale);
            i.setAlpha(this.uib.getElusiveAlpha());
            g.setDrawMode(Graphics.MODE_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
            g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            g.setDrawMode(Graphics.MODE_NORMAL);
        }
        if (this.card instanceof Minion) {
            if (this.card.realCard != null && this.card.realCard instanceof Minion
                    && (!this.uib.b.disableInput || this.card.team != this.uib.b.getLocalteam())) {
                Minion relevantMinion = (Minion) (this.card.team == this.uib.b.getLocalteam() ? this.card.realCard : this.card);
                if (relevantMinion.canAttack()) {
                    Color borderColor;
                    if (relevantMinion.finalStats.get(Stat.CANT_ATTACK_LEADER) > 0 || (relevantMinion.summoningSickness
                            && relevantMinion.finalStats.get(Stat.RUSH) > 0
                            && relevantMinion.finalStats.get(Stat.STORM) == 0)) {
                        borderColor = Color.yellow;
                    } else {
                        borderColor = Color.cyan;
                    }
                    drawReadyBorder(g, pos, scale, borderColor);
                }
            }
            if (this.card.finalStats.get(Stat.WARD) > 0) {
                Image i = Game.getImage("game/ward.png");
                i = i.getScaledCopy((float) scale);
                if (this.card.finalStats.get(Stat.STEALTH) > 0) {
                    i.setAlpha(0.5f);
                }
                g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            }
            if (this.card.finalStats.get(Stat.SHIELD) > 0) {
                Image i = Game.getImage("game/shield.png");
                i = i.getScaledCopy((float) scale);
                g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            }
            if (this.card.finalStats.get(Stat.DISARMED) > 0) {
                Image i = Game.getImage("game/disarmed.png");
                i = i.getScaledCopy((float) scale);
                g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            }
            if (this.card.finalStats.get(Stat.FROZEN) > 0) {
                Image i = Game.getImage("game/frozen.png");
                i = i.getScaledCopy((float) scale);
                g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
            }
            this.drawOffensiveStat(g, pos, scale, this.card.finalStats.get(Stat.ATTACK),
                    this.card.finalBasicStats.get(Stat.ATTACK),
                    new Vector2f(-MINION_STAT_POS_OFFSET_BOARD, MINION_STAT_POS_BASE_BOARD), this.getStatFontSize(Stat.ATTACK), Game.getImage("game/statattack.png"));
            this.drawOffensiveStat(g, pos, scale, this.card.finalStats.get(Stat.MAGIC),
                    this.card.finalBasicStats.get(Stat.MAGIC),
                    new Vector2f(0, MINION_STAT_POS_BASE_BOARD), this.getStatFontSize(Stat.MAGIC), Game.getImage("game/statmagic.png"));
            this.drawHealthStat(g, pos, scale, this.getMinion().health,
                    this.card.finalStats.get(Stat.HEALTH),
                    this.card.finalBasicStats.get(Stat.HEALTH),
                    new Vector2f(MINION_STAT_POS_OFFSET_BOARD, MINION_STAT_POS_BASE_BOARD), this.getStatFontSize(Stat.HEALTH));
            if (this.card.finalStats.get(Stat.ARMOR) != 0) {
                this.drawStatNumber(g, pos, scale, this.card.finalStats.get(Stat.ARMOR),
                        DAMAGE_MODIFIERS_POS_BOARD, this.getStatFontSize(Stat.ARMOR), this.card.finalStats.get(Stat.ARMOR) < 0 ? Color.red : Color.white,
                        Game.getImage("game/statarmor.png"), STAT_ICON_DEFAULT_SCALE);
            }
            if (this.card.finalStats.get(Stat.SHIELD) > 0) {
                Vector2f iconpos = this.card.finalStats.get(Stat.ARMOR) == 0 ? DAMAGE_MODIFIERS_POS_BOARD : new Vector2f(DAMAGE_MODIFIERS_POS_BOARD.x, DAMAGE_MODIFIERS_POS_BOARD.y - DAMAGE_MODIFIERS_SPREAD_BOARD);
                this.drawStatNumber(g, pos, scale, this.card.finalStats.get(Stat.SHIELD),
                        iconpos, this.getStatFontSize(Stat.SHIELD), Color.white, Game.getImage("game/statshield.png"), STAT_ICON_DEFAULT_SCALE);
            }
        }
        if (this.card.finalStats.contains(Stat.COUNTDOWN)) {
            this.drawStatNumber(g, pos, scale, this.card.finalStats.get(Stat.COUNTDOWN), COUNTDOWN_POS,
                    this.getStatFontSize(Stat.COUNTDOWN) * STAT_ICON_COUNTDOWN_SCALE / STAT_ICON_DEFAULT_SCALE, Color.white, Game.getImage("game/statcountdown.png"), STAT_ICON_COUNTDOWN_SCALE);
        }
        // if marked for death
        if (!this.card.alive) {
            Image i = Game.getImage("game/markedfordeath.png");
            i = i.getScaledCopy((float) scale);
            g.drawImage(i, pos.x - i.getWidth() / 2, pos.y - i.getHeight() / 2);
        }
    }

    private void drawReadyBorder(Graphics g, Vector2f pos, double scale, Color color) {
        g.setLineWidth(READY_BORDER_WIDTH);
        g.setColor(color);
        g.drawRect((float) (pos.x - CARD_DIMENSIONS.x * scale / 2 - READY_BORDER_PADDING),
                (float) (pos.y - CARD_DIMENSIONS.y * scale / 2 - READY_BORDER_PADDING),
                (float) (CARD_DIMENSIONS.x * scale + READY_BORDER_PADDING * 2),
                (float) (CARD_DIMENSIONS.y * scale + READY_BORDER_PADDING * 2));
        g.setColor(Color.white);
    }

    // called by updateEffectStats in Card
    public void updateIconList() {
        this.icons.clear();
        if (this.card.finalStats.get(Stat.BANE) > 0) {
            this.icons.add(Game.getImage("game/baneicon.png"));
        }
        if (this.card.finalStats.get(Stat.POISONOUS) > 0) {
            this.icons.add(Game.getImage("game/poisonousicon.png"));
        }
        if (this.card.finalStats.get(Stat.LIFESTEAL) > 0) {
            this.icons.add(Game.getImage("game/lifestealicon.png"));
        }
        if (this.card.finalStats.get(Stat.FREEZING_TOUCH) > 0) {
            this.icons.add(Game.getImage("game/freezingtouchicon.png"));
        }
        if (this.card instanceof BoardObject) {
            BoardObject bo = (BoardObject) this.card;
            if (bo.hasResolvers(Effect::lastWords)) {
                this.icons.add(Game.getImage("game/lastwordsicon.png"));
            }
            if (bo.hasResolvers(e -> e.onListenEventWhileInPlay(null)) || bo.hasResolvers(Effect::onTurnStartAllied)
                    || bo.hasResolvers(Effect::onTurnEndAllied) || bo.hasResolvers(Effect::onTurnStartEnemy)
                    || bo.hasResolvers(Effect::onTurnEndEnemy) || bo.hasResolvers(Effect::onEnterPlay) || bo.hasResolvers(Effect::onLeavePlay)
                    || bo.hasResolvers(e -> e.onDamaged(0))) {
                this.icons.add(Game.getImage("game/flagicon.png"));
            }
        }
        if (this.card instanceof Minion) {
            Minion m = (Minion) this.card;
            Leader dummy = new Leader(this.uib.b, new Rowen());
            if (m.hasResolvers(e -> e.strike(dummy)) || m.hasResolvers(e -> e.minionStrike(dummy)) || m.hasResolvers(e -> e.leaderStrike(dummy))) {
                this.icons.add(Game.getImage("game/attackicon.png"));
            }
            if (m.hasResolvers(e -> e.retaliate(dummy))) {
                this.icons.add(Game.getImage("game/defendicon.png"));
            }
            if (m.hasResolvers(e -> e.clash(dummy))) {
                this.icons.add(Game.getImage("game/clashicon.png"));
            }
        }
    }

    public void drawIcons(Graphics g, Vector2f pos, double scale) {
        int numIcons = this.icons.size();
        for (int i = 0; i < numIcons; i++) {
            Image scaled = this.icons.get(i).getScaledCopy((float) scale * ICON_SCALE);
            g.drawImage(scaled, pos.x - scaled.getWidth() / 2 - (i - (numIcons - 1f) / 2) * ICON_SPACING * (float) scale,
                    pos.y - scaled.getHeight() / 2 + CARD_DIMENSIONS.y * ICON_Y * (float) scale);
        }
    }

    public void drawInHand(Graphics g, Vector2f pos, double scale) {
        UnicodeFont nameFont = Game.getFont((int) (NAME_FONT_SIZE * scale), true, false);
        double targetWidth = CARD_DIMENSIONS.x * scale * (1 - HAND_TITLE_OFFSET);
        if (nameFont.getWidth(this.card.getTooltip().name) > targetWidth) {
            nameFont = Game.getFont((int) (NAME_FONT_SIZE * scale * targetWidth / nameFont.getWidth(this.card.getTooltip().name)),
                    true, false);
        }
        nameFont.drawString(pos.x - nameFont.getWidth(this.card.getTooltip().name) / 2f + HAND_TITLE_OFFSET / 2 * CARD_DIMENSIONS.x * (float) scale,
                pos.y - CARD_DIMENSIONS.y * (float) scale / 2, this.card.getTooltip().name);
        UnicodeFont traitsFont = Game.getFont((int) (TRAITS_FONT_SIZE * scale), true, false);
        String traitsString = TooltipCard.listTraits(this.card.getTooltip().traits);
        if (traitsFont.getWidth(traitsString) > targetWidth) {
            traitsFont = Game.getFont((int) (TRAITS_FONT_SIZE * scale * targetWidth / traitsFont.getWidth(traitsString)),
                    true, false);
        }
        traitsFont.drawString(pos.x - traitsFont.getWidth(traitsString) / 2f + HAND_TITLE_OFFSET / 2 * CARD_DIMENSIONS.x * (float) scale,
                pos.y + CARD_DIMENSIONS.y * (float) scale / 2 - traitsFont.getHeight(traitsString), traitsString);
        if (this.card.realCard != null
                && (this.card.team == this.uib.b.getLocalteam() ? // different rules depending on allied team or enemy team
                this.uib.b.realBoard.getPlayer(this.card.realCard.team).canPlayCard(this.card.realCard) && !this.uib.b.disableInput : // condition for cards on our team (should update instantly)
                this.uib.b.getPlayer(this.card.team).canPlayCard(this.card)) // condition for cards on the enemy team (should wait for animations)
        ) {
            drawReadyBorder(g, pos, scale, Color.cyan);
        }
        this.drawCostStat(g, pos, scale, this.card.finalStats.get(Stat.COST),
                this.card.finalBasicStats.get(Stat.COST), COST_POS, this.getStatFontSize(Stat.COST));
        if (this.card instanceof Minion) {
            this.drawOffensiveStat(g, pos, scale, this.card.finalStats.get(Stat.ATTACK),
                    this.card.finalBasicStats.get(Stat.ATTACK),
                    new Vector2f(MINION_STAT_POS_BASE_HAND, MINION_STAT_POS_CENTER_HAND - MINION_STAT_POS_OFFSET_HAND), this.getStatFontSize(Stat.ATTACK), Game.getImage("game/statattack.png"));
            this.drawOffensiveStat(g, pos, scale, this.card.finalStats.get(Stat.MAGIC),
                    this.card.finalBasicStats.get(Stat.MAGIC),
                    new Vector2f(MINION_STAT_POS_BASE_HAND, MINION_STAT_POS_CENTER_HAND), this.getStatFontSize(Stat.MAGIC), Game.getImage("game/statmagic.png"));
            this.drawHealthStat(g, pos, scale, this.getMinion().health,
                    this.card.finalStats.get(Stat.HEALTH),
                    this.card.finalBasicStats.get(Stat.HEALTH),
                    new Vector2f(MINION_STAT_POS_BASE_HAND, MINION_STAT_POS_CENTER_HAND + MINION_STAT_POS_OFFSET_HAND), this.getStatFontSize(Stat.HEALTH));
            if (this.card.finalStats.get(Stat.ARMOR) != 0) {
                this.drawStatNumber(g, pos, scale, this.card.finalStats.get(Stat.ARMOR),
                        DAMAGE_MODIFIERS_POS_HAND, this.getStatFontSize(Stat.ARMOR), this.card.finalStats.get(Stat.ARMOR) < 0 ? Color.red : Color.white,
                        Game.getImage("game/statarmor.png"), STAT_ICON_DEFAULT_SCALE);
            }
            if (this.card.finalStats.get(Stat.SHIELD) > 0) {
                Vector2f iconpos = this.card.finalStats.get(Stat.ARMOR) == 0 ? DAMAGE_MODIFIERS_POS_HAND : new Vector2f(DAMAGE_MODIFIERS_POS_HAND.x + DAMAGE_MODIFIERS_SPREAD_HAND, DAMAGE_MODIFIERS_POS_HAND.y);
                this.drawStatNumber(g, pos, scale, this.card.finalStats.get(Stat.SHIELD),
                        iconpos, this.getStatFontSize(Stat.SHIELD), Color.white, Game.getImage("game/statshield.png"), STAT_ICON_DEFAULT_SCALE);
            }
        }
        if (this.card.finalStats.contains(Stat.COUNTDOWN)) {
            this.drawStatNumber(g, pos, scale, this.card.finalStats.get(Stat.COUNTDOWN), COUNTDOWN_POS,
                    50, Color.white, Game.getImage("game/statcountdown.png"), STAT_ICON_COUNTDOWN_SCALE);
        }
    }

    public void drawStatNumber(Graphics g, Vector2f pos, double scale, int stat, Vector2f relpos,
            double fontsize, Color c, Image icon, double iconScale) {
        UnicodeFont font = Game.getFont((int) (fontsize * scale), true, false, c, Color.black);
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
        this.drawStatNumber(g, pos, scale, cost, relpos, fontsize, c, Game.getImage("game/statcost.png"), STAT_ICON_DEFAULT_SCALE);
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
        this.drawStatNumber(g, pos, scale, health, relpos, fontsize, c, Game.getImage("game/stathealth.png"), STAT_ICON_DEFAULT_SCALE);
    }

    public void drawPendingPlayPosition(Graphics g, Vector2f drawPos) {
        Vector2f absPos = this.getAbsPos();
        g.setDrawMode(Graphics.MODE_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
        this.drawCardArt(g, drawPos, SCALE_BOARD, CardStatus.BOARD, PENDING_POSITION_COLOR);
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

    // modified comparison to make cards in hand appear layered in order
    @Override
    public int compareTo(UIElement uie) {
        if (this.getZ() == uie.getZ() && uie instanceof UICard &&
                this.card.status.equals(CardStatus.HAND) && ((UICard) uie).card.status.equals(CardStatus.HAND)) {
            return this.card.getIndex() - ((UICard) uie).card.getIndex();
        } else {
            return super.compareTo(uie);
        }
    }

}
