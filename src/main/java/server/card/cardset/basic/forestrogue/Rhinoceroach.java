package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectUntilTurnEnd;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Rhinoceroach extends MinionText {
    public static final String NAME = "Rhinoceroach";
    public static final String DESCRIPTION = "<b>Storm</b>.\n<b>Battlecry</b>: Gain +X/+0/+0 until the end of the turn. X equals the number of other cards played this turn.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/rhinoceroach.png",
            CRAFT, RARITY,2, 1, 1, 1, true, Rhinoceroach.class,
            new Vector2f(182, 183), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.STORM, Tooltip.BATTLECRY));
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.STORM, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Battlecry</b>: Gain +X/+0/+0 until the end of the turn. X equals the number of other cards played this turn.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int bonus = b.getPlayer(owner.team).cardsPlayedThisTurn - 1;
                        EffectUntilTurnEnd buff = new EffectUntilTurnEnd("+" + bonus + "/+0/+0 until end of turn (from <b>Battlecry</b>).", EffectStats.builder()
                                .change(EffectStats.ATTACK, bonus)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
