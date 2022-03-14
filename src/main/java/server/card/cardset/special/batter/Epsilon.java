package server.card.cardset.special.batter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.RestoreResolver;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Epsilon extends MinionText {
    public static final String NAME = "Add-on: Epsilon";
    public static final String DESCRIPTION = "When this minion attacks, deal 2 damage to all enemies.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/special/epsilon.png",
            CRAFT, RARITY, 4, 2, 2, 2, true, Epsilon.class,
            new Vector2f(), -1, EventAnimationDamageOff.class,
            () -> List.of(Tooltip.RUSH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver onAttack(Minion target) {
                Effect effect = this;
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team * -1, true, false).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, 2, true, EventAnimationDamageOff.class));
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_HEAL * 5 / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
