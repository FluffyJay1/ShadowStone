package server.card.cardset.anime.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEMegumin;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.DiscardResolver;
import server.resolver.ManaChangeResolver;
import server.resolver.MuteResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.SpellboostResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Megumin extends MinionText {
    public static final String NAME = "Megumin";
    public static final int MAGIC_THRESHOLD = 6;
    public static final int MANA_THRESHOLD = 6;
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Randomly put a card with <b>Spellboost</b> from your deck into your hand. Restore <b>S</b> mana orbs.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: <b>Spellboost</b> a card in your hand M times."
            + "Then if M is at least " + MAGIC_THRESHOLD + " and you have at least " + MANA_THRESHOLD + " mana left, deal X damage split evenly among all enemies, discard your hand, destroy 6 of your mana orbs, then <b>Mute</b> this minion. "
            + "X equals the total sum of <b>Spellboosts</b> of all cards in your hand.";
    private static final String OTHER_DESCRIPTION = "<b>Stealth</b>. <b>Disarmed</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/megumin.png"),
            CRAFT, TRAITS, RARITY, 6, 1, 3, 6, false, Megumin.class,
            new Vector2f(118, 145), 1.6, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STEALTH, Tooltip.DISARMED, Tooltip.BATTLECRY, Tooltip.SPELLBOOST, Tooltip.UNLEASH, Tooltip.MUTE),
            List.of(card -> String.format("(Total <b>Spellboosts</b> in hand: %d)", card.player.getHand().stream().filter(c -> c.finalStats.get(Stat.SPELLBOOSTABLE) >= 1).map(c -> c.spellboosts).reduce(Integer::sum).orElse(0))));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STEALTH, 1)
                .set(Stat.DISARMED, 1)
                .set(Stat.SPELLBOOSTABLE, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Card> relevant = owner.player.getDeck().stream()
                                .filter(c -> c.finalStats.get(Stat.SPELLBOOSTABLE) > 0)
                                .toList();
                        if (!relevant.isEmpty()) {
                            List<Card> selection = SelectRandom.from(relevant, 1);
                            List<Integer> pos = Collections.nCopies(selection.size(), -1);
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, pos, true));
                        }
                        int x = owner.spellboosts;
                        this.resolve(b, rq, el, new ManaChangeResolver(owner.player, x, true, false, false));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return Math.min(owner.spellboosts, 10);
            }

            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 0, 1, "Choose a card in your hand to <b>Spellboost M</b> times.") {
                    @Override
                    protected boolean criteria(Card c) {
                        return c.status.equals(CardStatus.HAND) && c.team == this.getCreator().owner.team && c.finalStats.get(Stat.SPELLBOOSTABLE) > 0
                                && c != this.getCreator().owner;
                    }
                });
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int m = owner.finalStats.get(Stat.MAGIC);
                        List<Card> cardsToSpellboost = getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).toList();
                        if (!cardsToSpellboost.isEmpty()) {
                            for (int i = 0; i < m; i++) {
                                this.resolve(b, rq, el, new SpellboostResolver(cardsToSpellboost));
                            }
                        }
                        if (m >= MAGIC_THRESHOLD && owner.player.mana >= MANA_THRESHOLD) {
                            int x = owner.player.getHand().stream().filter(c -> c.finalStats.get(Stat.SPELLBOOSTABLE) >= 1).map(c -> c.spellboosts).reduce(Integer::sum).orElse(0);
                            List<Minion> targets = b.getMinions(owner.team * -1, true, true).toList();
                            List<Integer> damage = IntStream.range(0, targets.size())
                                    .map(i -> (x + i) / targets.size()) // math wizardry
                                    .boxed()
                                    .toList();
                            this.resolve(b, rq, el, new DamageResolver(effect, targets, damage, true, new EventAnimationDamageAOEMegumin(owner.team * -1)));
                            this.resolve(b, rq, el, new DiscardResolver(owner.player.getHand()));
                            this.resolve(b, rq, el, new ManaChangeResolver(owner.player, -6, true, true, false));
                            this.resolve(b, rq, el, new MuteResolver(owner, true));
                        }
                    }
                });
            }

            @Override
            public boolean unleashSpecialConditions() {
                return owner.player.mana >= MANA_THRESHOLD + owner.player.getUnleashPower().map(up -> up.finalStats.get(Stat.COST)).orElse(0) && owner.finalStats.get(Stat.MAGIC) >= MAGIC_THRESHOLD - 1;
            }

            @Override
            public double getPresenceValue(int refs) {
                return owner.player.getHand().stream().filter(c -> c.finalStats.get(Stat.SPELLBOOSTABLE) >= 1).map(c -> c.spellboosts).reduce(Integer::sum).orElse(0) / (2 * Math.max(MAGIC_THRESHOLD - owner.finalStats.get(Stat.MAGIC), 1));
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
