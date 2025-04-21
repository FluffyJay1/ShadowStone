package server.card.cardset.moba.shadowshaman;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.NecromancyResolver;
import server.resolver.RemoveEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Spy extends MinionText {
    public static final String NAME = "Spy";
    public static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: At the start of your next turn, perform <b>Necromancy(5)</b> to summon a <b>Spy</b>.";
    public static final String DEPENDENT_STATS_DESCRIPTION = "Has <b>Poisonous</b> while <b>Stealthed</b>.";
    public static final String OTHER_DESCRIPTION = "<b>Stealth</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + DEPENDENT_STATS_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/spy.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 1, 3, true, Spy.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STEALTH, Tooltip.POISONOUS, Tooltip.LASTWORDS, Tooltip.NECROMANCY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectWithDependentStats(DEPENDENT_STATS_DESCRIPTION, true) {
                    @Override
                    public EffectStats calculateStats() {
                        if (owner.finalStats.get(Stat.STEALTH) > 0) {
                            return EffectStats.builder()
                                    .set(Stat.POISONOUS, 1)
                                    .build();
                        }
                        return new EffectStats();
                    }

                    @Override
                    public boolean isActive() {
                        return this.owner.isInPlay();
                    }
                },
                new Effect(OTHER_DESCRIPTION + LASTWORDS_DESCRIPTION, EffectStats.builder()
                        .set(Stat.STEALTH, 1)
                        .build()) {
                    @Override
                    public ResolverWithDescription lastWords() {
                        return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                Effect buff = new EffectDeadRinger();
                                owner.player.getLeader().ifPresent(l -> {
                                    this.resolve(b, rq, el, new AddEffectResolver(l, buff));
                                });
                            }
                        });
                    }

                    @Override
                    public double getLastWordsValue(int refs) {
                        return AI.valueForSummoning(List.of(new Spy().constructInstance(owner.board)), refs) / 5;
                    }
                }
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectDeadRinger extends Effect {
        public static final String EFFECT_DESCRIPTION = "At the start of your turn, perform <b>Necromancy(5)</b> to summon a <b>Spy</b> (from <b>" + NAME + "</b>).";

        public EffectDeadRinger() {
            super(EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnStartAllied() {
            Effect effect = this;
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(true) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    this.resolve(b, rq, el, new NecromancyResolver(effect, 5, new CreateCardResolver(new Spy(), owner.team, CardStatus.BOARD, -1)));
                    this.resolve(b, rq, el, new RemoveEffectResolver(List.of(effect)));
                }
            });
        }
    }
}
