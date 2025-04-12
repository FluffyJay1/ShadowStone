package server.card.cardset.moba.dragondruid;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOEFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import server.ServerBoard;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Pyro extends MinionText {
    public static final String NAME = "Pyro";
    private static final String STRIKE_DESCRIPTION = "<b>Strike</b>: Deal 2 damage to all enemies, then give all enemies the following effect for 3 turns: " +
    "\"At the end of your turn, take 1 damage.\"";
    public static final String DESCRIPTION = "<b>Rush</b>. <b>Cleave</b>. <b>Elusive</b>.\n" + STRIKE_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/pyro.png"),
            CRAFT, TRAITS, RARITY, 10, 5, 1, 4, true, Pyro.class,
            new Vector2f(), -1, new EventAnimationDamageFire(),
            () -> List.of(Tooltip.RUSH, Tooltip.CLEAVE, Tooltip.ELUSIVE, Tooltip.STRIKE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .set(Stat.ELUSIVE, 1)
                .set(Stat.CLEAVE, 1)
                .build()) {
            @Override
            public ResolverWithDescription strike(Minion target) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, true, true).toList();
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, 2, true, new EventAnimationDamageAOEFire(owner.team * -1, true).toString()));
                        List<Minion> afterburnTargets = b.getMinions(owner.team * -1, true, true).toList();
                        this.resolve(b, rq, el, new AddEffectResolver(afterburnTargets, new EffectAfterburn()));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 10;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectAfterburn extends Effect {
        private static String EFFECT_DESCRIPTION = "At the end of your turn, take 1 damage (from <b>" + NAME + "</b>).";

        // required for reflection
        public EffectAfterburn() {
            super(EFFECT_DESCRIPTION);
            this.setUntilTurnEnd(1, 3);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            Effect effect = this;
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    if (owner instanceof Minion) {
                        this.resolve(b, rq, el, new DamageResolver(effect, (Minion) owner, 1, true, new EventAnimationDamageFire().toString()));
                    }
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE * -1; // lol
        }
    }
}

