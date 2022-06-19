package server.card.cardset.basic.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Chronos extends MinionText {
    public static final String NAME = "Chronos";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Give both leaders the following effect: " +
            "\"At the end of your turn, draw cards equal to the attack of your highest attack minion.\" " +
            "(This effect is not stackable and lasts for the rest of the match.)";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/chronos.png",
            CRAFT, TRAITS, RARITY,8, 4, 3, 3, true, Chronos.class,
            new Vector2f(143, 135), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Leader> leaders = b.getPlayerCard(0, Player::getLeader).collect(Collectors.toList());
                        this.resolve(b, rq, el, new AddEffectResolver(leaders, new EffectChronos()));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                // return some bullshit
                return AI.VALUE_PER_CARD_IN_HAND * 4;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }

    public static class EffectChronos extends Effect {
        private static final String EFFECT_DESCRIPTION = "At the end of your turn, draw cards equal to the attack of your highest attack minion (from <b>Chronos</b>).";

        // required for reflection
        public EffectChronos() {
            super(EFFECT_DESCRIPTION, false);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    int highestAttack = b.getMinions(owner.team, false, true)
                            .map(m -> m.finalStats.get(Stat.ATTACK))
                            .reduce(0, Math::max);
                    this.resolve(b, rq, el, new DrawResolver(b.getPlayer(owner.team), highestAttack));
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            return 4;
        }
    }
}
