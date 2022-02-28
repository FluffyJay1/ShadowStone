package server.card;

import client.tooltip.TooltipMinion;
import server.Board;
import server.BoardObjectText;
import server.ServerBoard;
import server.ai.AI;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.EffectDamageResolver;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.List;

public abstract class MinionText extends BoardObjectText {
    @Override
    public final List<Effect> getEffects() {
        TooltipMinion tooltip = this.getTooltip();
        Effect e = new Effect("", new EffectStats(tooltip.cost, tooltip.attack, tooltip.magic, tooltip.health));
        e.effectStats.set.setStat(EffectStats.ATTACKS_PER_TURN, 1);
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
                        public boolean canTarget(Card c) {
                            return c.status == CardStatus.BOARD && c instanceof Minion
                                    && c.team != this.getCreator().owner.team;
                        }
                    });
                }

                @Override
                public Resolver unleash() {
                    Effect effect = this; // anonymous fuckery
                    return new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            getStillTargetableUnleashCardTargets(0).findFirst().ifPresent(c -> {
                                Minion target = (Minion) c;
                                EffectDamageResolver edr = new EffectDamageResolver(effect, List.of(target),
                                        List.of(effect.owner.finalStatEffects.getStat(EffectStats.MAGIC)), true, null);
                                this.resolve(b, rq, el, edr);
                            });
                        }
                    };
                }

                @Override
                public double getPresenceValue(int refs) {
                    return AI.VALUE_PER_DAMAGE * this.owner.finalStatEffects.getStat(EffectStats.MAGIC) / 2.;
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
