package server.card.cardset.basic.neutral;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageRocks;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectStatChange;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Tiny extends MinionText {
    public static final String NAME = "Tiny";
    public static final String DESCRIPTION = "<b>Unleash</b>: Gain +2/+0/+2 and <b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/tiny.png",
            CRAFT, TRAITS, RARITY, 3, 2, 2, 3, false, Tiny.class,
            new Vector2f(), -1, EventAnimationDamageRocks.class,
            () -> List.of(Tooltip.UNLEASH, Tooltip.RUSH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectStatChange ef = new EffectStatChange("+2/+0/+2 and <b>Rush</b> (from <b>Unleash</b>).", 2,
                                0, 2);
                        ef.effectStats.set.set(Stat.RUSH, 1);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, ef));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.valueForBuff(2, 0, 2) + AI.VALUE_OF_RUSH) / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
