package server.card.effect;

import server.Board;
import server.Player;
import server.card.Card;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cards in play can grant other cards effects as long as they are on the board,
 * and stop granting the effect as soon as they leave. This type of effect is
 * called an aura, and requires a little extra work and special treatment to
 * make sure it behaves well with our systems. Most of the logic for this is in
 * ServerBoard.java, as a result.
 */
public abstract class EffectAura extends Effect {
    public Map<Card, Effect> currentActiveEffects;

    // filter which cards can be affected by the aura
    boolean affectHand, affectBoard, affectLeader, affectUnleashPower;
    int affectTeam; // 0 means affect both teams
    public Effect effectToApply;

    // required for reflection
    public EffectAura() {
        this.currentActiveEffects = new HashMap<>();
    }

    /**
     * Create an aura that applies an effect to selected cards, remember to
     * override the applyConditions(Card) method to further specify which cards
     * to apply to
     *
     * @param description The description of the effect
     * @param affectTeam Which team to affect, with 1 being friendly, -1 being enemy, and 0 being both
     * @param affectBoard Whether to affect cards on the board
     * @param affectHand Whether to affect cards in hand
     * @param effectToApply The effect to apply to affected cards
     */
    public EffectAura(String description, int affectTeam, boolean affectBoard, boolean affectHand, Effect effectToApply) {
        this(description, affectTeam, affectBoard, affectHand, false, false, effectToApply);
    }

    /**
     * Create an aura that applies an effect to selected cards, remember to
     * override the applyConditions(Card) method to further specify which cards
     * to apply to
     *
     * @param description The description of the effect
     * @param affectTeam Which team to affect, with 1 being friendly, -1 being enemy, and 0 being both
     * @param affectBoard Whether to affect cards on the board
     * @param affectHand Whether to affect cards in hand
     * @param affectLeader Whether to affect the Leader
     * @param affectUnleashPower Whether to affect the unleash power
     * @param effectToApply The effect to apply to affected cards
     */
    public EffectAura(String description, int affectTeam, boolean affectBoard, boolean affectHand, boolean affectLeader, boolean affectUnleashPower, Effect effectToApply) {
        this();
        this.description = description;
        this.affectTeam = affectTeam;
        this.affectBoard = affectBoard;
        this.affectHand = affectHand;
        this.affectLeader = affectLeader;
        this.affectUnleashPower = affectUnleashPower;
        effectToApply.auraSource = this;
        this.effectToApply = effectToApply;
    }

    public final Set<Card> findAffectedCards() {
        Set<Card> filtered = new HashSet<>();
        if (this.removed || this.mute || !this.owner.isInPlay()) {
            return filtered;
        }
        Board b = this.owner.board;
        int targetTeam = this.affectTeam * this.owner.team;
        if (this.affectBoard) {
            filtered.addAll(b.getPlayerCards(targetTeam, Player::getPlayArea)
                    .filter(this::applyConditions)
                    .collect(Collectors.toSet()));
        }
        if (this.affectHand) {
            filtered.addAll(b.getPlayerCards(targetTeam, Player::getHand)
                    .filter(this::applyConditions)
                    .collect(Collectors.toSet()));
        }
        if (this.affectLeader) {
            filtered.addAll(b.getPlayerCard(targetTeam, Player::getLeader)
                    .filter(this::applyConditions)
                    .collect(Collectors.toSet()));
        }
        if (this.affectUnleashPower) {
            filtered.addAll(b.getPlayerCard(targetTeam, Player::getUnleashPower)
                    .filter(this::applyConditions)
                    .collect(Collectors.toSet()));
        }
        return filtered;
    }

    /**
     * Determine whether this aura should apply to a specific card.
     *
     * @param cardToApply The card to check for
     * @return Whether the aura should apply
     */
    public abstract boolean applyConditions(Card cardToApply);

    // if we're AddEffect'ing an Aura, the aura gets cloned, so we need to update effectToApply to reference the new cloned aura
    @Override
    public EffectAura clone() throws CloneNotSupportedException {
        EffectAura e = (EffectAura) super.clone();
        e.effectToApply.auraSource = e;
        return e;
    }

    @Override
    public String extraStateString() {
        return this.affectTeam + " " + this.affectBoard + " " + this.affectHand + " " + this.affectLeader + " " + this.affectUnleashPower + " " + this.effectToApply.toString();
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        this.affectTeam = Integer.parseInt(st.nextToken());
        this.affectBoard = Boolean.parseBoolean(st.nextToken());
        this.affectHand = Boolean.parseBoolean(st.nextToken());
        this.affectLeader = Boolean.parseBoolean(st.nextToken());
        this.affectUnleashPower = Boolean.parseBoolean(st.nextToken());
        this.effectToApply = Effect.fromString(b, st);
        this.effectToApply.auraSource = this;
    }
}
