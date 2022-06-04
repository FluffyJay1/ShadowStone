package server.card;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageEnergyBeam;
import server.Board;
import server.BoardObjectText;
import server.ServerBoard;
import server.ai.AI;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.List;

public abstract class MinionText extends BoardObjectText {
    @Override
    public final List<Effect> getEffects() {
        TooltipMinion tooltip = this.getTooltip();
        EffectStats stats = new EffectStats(tooltip.cost, tooltip.attack, tooltip.magic, tooltip.health);
        stats.traits.addAll(tooltip.traits);
        stats.set.set(Stat.ATTACKS_PER_TURN, 1);
        Effect e = new Effect("", stats);
        e.effectStats.set.set(Stat.ATTACKS_PER_TURN, 1);
        List<Effect> special = this.getSpecialEffects();
        int specialSize = 0;
        if (special != null) {
            specialSize = special.size();
        }
        List<Effect> ret = new ArrayList<>(specialSize + (tooltip.basicUnleash ? 2 : 1));
        ret.add(e);
        if (tooltip.basicUnleash) {
            Effect unl = new Effect(
                    "<b>Unleash</b>: Deal X damage to an enemy minion. X equals this minion's magic.") {
                public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                    return List.of(new CardTargetingScheme(this, 0, 1, "Deal X damage to an enemy minion. X equals this minion's magic.") {
                        @Override
                        protected boolean criteria(Card c) {
                            return c.status == CardStatus.BOARD && c instanceof Minion
                                    && c.team != this.getCreator().owner.team;
                        }
                    });
                }

                @Override
                public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                    Effect effect = this; // anonymous fuckery
                    String resolverDescription = "<b>Unleash</b>: Deal X damage to an enemy minion. X equals this minion's magic.";
                    return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 0).findFirst().ifPresent(c -> {
                                Minion target = (Minion) c;
                                DamageResolver dr = new DamageResolver(effect, List.of(target),
                                        List.of(effect.owner.finalStats.get(Stat.MAGIC)), true,
                                        EventAnimationDamageEnergyBeam.class);
                                this.resolve(b, rq, el, dr);
                            });
                        }
                    });
                }

                @Override
                public double getPresenceValue(int refs) {
                    return AI.valueOfMinionDamage(this.owner.finalStats.get(Stat.MAGIC)) / 2.;
                }
            };
            ret.add(unl);
        }
        if (special != null) {
            ret.addAll(special);
        }
        return ret;
    }

    @Override
    public Minion constructInstance(Board b) {
        return new Minion(b, this);
    }

    protected abstract List<Effect> getSpecialEffects();
    public abstract TooltipMinion getTooltip();
}
