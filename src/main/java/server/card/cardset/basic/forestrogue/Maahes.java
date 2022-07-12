package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOESlice;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Maahes extends MinionText {
    public static final String NAME = "Maahes";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If at least 2 other cards were payed this turn, deal 2 damage to all enemies.";
    public static final String DESCRIPTION = "<b>Ward</b>.\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/maahes.png",
            CRAFT, TRAITS, RARITY, 5, 5, 2, 5, true, Maahes.class,
            new Vector2f(150, 145), 1.5, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (owner.player.cardsPlayedThisTurn > 2) {
                            List<Minion> targets = b.getMinions(owner.team * -1, true, true).collect(Collectors.toList());
                            this.resolve(b, rq, el, new DamageResolver(effect, targets, 2, true, new EventAnimationDamageAOESlice(owner.team * -1, true).toString()));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return (AI.valueOfMinionDamage(2) * 3 + AI.VALUE_PER_DAMAGE * 2) / 2;
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return this.owner.player.cardsPlayedThisTurn >= 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
