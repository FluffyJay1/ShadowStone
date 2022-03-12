package server.card.cardset.basic.neutral;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageRocks;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.util.ResolverQueue;

public class Tiny extends MinionText {
    public static final String NAME = "Tiny";
    public static final String DESCRIPTION = "<b>Unleash</b>: Gain +2/+0/+2 and <b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/tiny.png",
            CRAFT, RARITY, 3, 2, 2, 3, false, Tiny.class,
            new Vector2f(), -1, EventAnimationDamageRocks.class,
            () -> List.of(Tooltip.UNLEASH, Tooltip.RUSH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public Resolver unleash() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        EffectStatChange ef = new EffectStatChange("+2/+0/+2 and <b>Rush</b> (from <b>Unleash</b>).", 2,
                                0, 2);
                        ef.effectStats.set.setStat(EffectStats.RUSH, 1);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, ef));
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_1_1_STATS * 2 / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
