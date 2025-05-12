package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
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
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class GravekeeperSonia extends MinionText {
    public static final String NAME = "Gravekeeper Sonia";
    public static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: If there are no other allied minions in play, gain +1/+0/+0 and <b>Repel</b>.";
    public static final String OTHER_DESCRIPTION = "<b>Ward</b>.";
    public static final String DESCRIPTION = OTHER_DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/gravekeepersonia.png"),
            CRAFT, TRAITS, RARITY, 5, 3, 2, 6, true, GravekeeperSonia.class,
            new Vector2f(120, 157), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.BATTLECRY, Tooltip.REPEL),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        boolean isAlone = b.getMinions(owner.team, false, true)
                                .filter(m -> m != owner)
                                .count() == 0;
                        if (isAlone) {
                            Effect buff = new Effect("+1/+0/+0 and <b>Repel</b> (from <b>" + NAME + "</b>)", EffectStats.builder()
                                    .change(Stat.ATTACK, 1)
                                    .set(Stat.REPEL, 1)
                                    .build());
                            this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                        }
                    }
                });
            }

            @Override
            public boolean battlecrySpecialConditions() {
                return owner.board.getMinions(owner.team, false, true)
                        .filter(m -> m != owner)
                        .count() == 0;
            }

            @Override
            public double getBattlecryValue(int refs) {
                return (AI.valueForBuff(1, 0, 0) + AI.VALUE_OF_REPEL) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
