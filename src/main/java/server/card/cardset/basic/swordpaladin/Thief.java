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
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Gain +0/+0/+X and <b>Rush</b>. X equals this minion's magic.";
    public static final String DESCRIPTION = "<b>Clash</b>: Draw a card.\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/thief.png",
            CRAFT, TRAITS, RARITY, 3, 2, 1, 3, false, Thief.class,
            new Vector2f(162, 151), 1.22, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.CLASH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+0/+0/+" + x + " and <b>Rush</b> (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.HEALTH, x)
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
                return (AI.valueForBuff(0, 0, 2) + AI.valueOfRush(this.owner)) / 2 + AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
