package server.card.cardset.special.batter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Epsilon extends MinionText {
    public static final String NAME = "Add-on: Epsilon";
    public static final String DESCRIPTION = "<b>Strike</b>: Deal 2 damage to all enemies.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/special/epsilon.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 2, 2, true, Epsilon.class,
            new Vector2f(), -1, new EventAnimationDamageOff(),
            () -> List.of(Tooltip.MINIONSTRIKE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription strike(Minion target) {
                Effect effect = this;
                String resolverDescription = "<b>Strike</b>: Deal 2 damage to all enemies.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team * -1, true, false).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, relevant, 2, true, new EventAnimationDamageOff().toString()));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 14 / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
