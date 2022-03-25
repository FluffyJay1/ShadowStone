package server.card.cardset.basic.neutral;

import java.util.*;
import java.util.stream.Collectors;

import client.*;
import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectStatChange;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class WellOfDestination extends AmuletText {
    public static final String NAME = "Well of Destination";
    public static final String DESCRIPTION = "At the start of your turn, give a random allied minion +1/+1/+1.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/wellofdestination.png",
            CRAFT, RARITY, 2, WellOfDestination.class,
            new Vector2f(), -1,
            List::of);

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnStart() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!possible.isEmpty()) {
                            Minion targeted = Game.selectRandom(possible);
                            EffectStatChange e = new EffectStatChange("+1/+1/+1 (from <b>Well of Destination</b>).", 1, 1,
                                    1);
                            this.resolve(b, rq, el, new AddEffectResolver(targeted, e));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_1_1_STATS * 7 / 2.;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
