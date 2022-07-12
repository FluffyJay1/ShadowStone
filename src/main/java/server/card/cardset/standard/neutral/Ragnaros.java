package server.card.cardset.standard.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFireball;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class Ragnaros extends MinionText {
    public static final String NAME = "Ragnaros the Firelord";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, deal 8 damage to a random enemy.";
    public static final String DESCRIPTION = "<b>Disarmed</b>.\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/ragnaros.png",
            CRAFT, TRAITS, RARITY, 8, 8, 3, 8, true, Ragnaros.class,
            new Vector2f(155, 155), 1.3, new EventAnimationDamageFire(),
            () -> List.of(Tooltip.DISARMED),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.DISARMED, 1)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                        if (!possible.isEmpty()) {
                            Minion target = SelectRandom.from(possible);
                            this.resolve(b, rq, el, new DamageResolver(effect, target, 8, true,
                                    new EventAnimationDamageFireball().toString()));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_DAMAGE * 8;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
