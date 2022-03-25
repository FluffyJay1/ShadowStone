package server.card.cardset.basic.neutral;

import java.util.*;
import java.util.stream.Collectors;

import client.*;
import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.effect.common.EffectStatChange;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class GenesisOfLegend extends AmuletText {
    public static final String NAME = "Gensis of Legend";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>. At the end of your turn, give a random allied minion +0/+0/+1 and <b>Bane</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/genesisoflegend.png",
            CRAFT, RARITY, 2, GenesisOfLegend.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BANE));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, new EffectStats(
                new EffectStats.Setter(EffectStats.COUNTDOWN, false, 3)
        )) {
            @Override
            public ResolverWithDescription onTurnEnd() {
                String resolverDescription = "At the end of your turn, give a random allied minion +0/+0/+1 and <b>Bane</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!possible.isEmpty()) {
                            Minion selected = Game.selectRandom(possible);
                            EffectStatChange esc = new EffectStatChange(
                                    "+0/+0/+1 and <b>Bane</b> (from <b>Genesis of Legend</b>).", 0, 0, 1);
                            esc.effectStats.set.setStat(EffectStats.BANE, 1);
                            this.resolve(b, rq, el, new AddEffectResolver(selected, esc));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 4;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
