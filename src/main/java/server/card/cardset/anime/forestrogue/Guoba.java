package server.card.cardset.anime.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageFire;
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

public class Guoba extends AmuletText {
    public static final String NAME = "Guoba";
    private static final String ONTURNEND_DESCRIPTION = "At the end of each player's turn, deal 2 damage to a random enemy minion.";
    public static final String DESCRIPTION = "<b>Countdown(1)</b>.\n" + ONTURNEND_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/anime/guoba.png"),
            CRAFT, TRAITS, RARITY, 2, Guoba.class,
            new Vector2f(153, 149), 1.3,
            () -> List.of(Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 1)
                .build()) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        if (!targets.isEmpty()) {
                            Minion selected = SelectRandom.from(targets);
                            this.resolve(b, rq, el, new DamageResolver(effect, selected, 2, true, new EventAnimationDamageFire()));
                        }
                    }
                });
            }

            @Override
            public ResolverWithDescription onTurnEndEnemy() {
                Effect effect = this;
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        if (!targets.isEmpty()) {
                            Minion selected = SelectRandom.from(targets);
                            this.resolve(b, rq, el, new DamageResolver(effect, selected, 2, true, new EventAnimationDamageFire()));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfMinionDamage(2) * 4;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
