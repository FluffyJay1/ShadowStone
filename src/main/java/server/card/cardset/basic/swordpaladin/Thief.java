package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
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
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Thief extends MinionText {
    public static final String NAME = "Thief";
    public static final String DESCRIPTION = "<b>Clash</b>: Draw a card.\n<b>Unleash</b>: Gain +0/+0/+2 and <b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/thief.png",
            CRAFT, TRAITS, RARITY, 3, 2, 1, 2, false, Thief.class,
            new Vector2f(162, 151), 1.22, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.CLASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Unleash</b>: Gain +0/+0/+2 and <b>Rush</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new Effect("+0/+0/+2 and <b>Rush</b> (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.HEALTH, 2)
                                .set(Stat.RUSH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }
            @Override
            public ResolverWithDescription clash(Minion target) {
                String resolverDescription = "<b>Clash</b>: Draw a card.";
                return new ResolverWithDescription(resolverDescription, new DrawResolver(owner.player, 1));
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.valueForBuff(0, 0, 2) + AI.VALUE_OF_RUSH) / 2 + AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
