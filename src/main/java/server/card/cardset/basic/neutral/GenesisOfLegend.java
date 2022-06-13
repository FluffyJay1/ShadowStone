package server.card.cardset.basic.neutral;

import java.util.*;
import java.util.stream.Collectors;

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
import utils.SelectRandom;

public class GenesisOfLegend extends AmuletText {
    public static final String NAME = "Gensis of Legend";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>. At the end of your turn, give a random allied minion +0/+0/+1 and <b>Bane</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/genesisoflegend.png",
            CRAFT, TRAITS, RARITY, 2, GenesisOfLegend.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BANE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()
        ) {
            @Override
            public ResolverWithDescription onTurnEndAllied() {
                String resolverDescription = "At the end of your turn, give a random allied minion +0/+0/+1 and <b>Bane</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!possible.isEmpty()) {
                            Minion selected = SelectRandom.from(possible);
                            EffectStatChange esc = new EffectStatChange(
                                    "+0/+0/+1 and <b>Bane</b> (from <b>Genesis of Legend</b>).", 0, 0, 1);
                            esc.effectStats.set.set(Stat.BANE, 1);
                            this.resolve(b, rq, el, new AddEffectResolver(selected, esc));
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return (AI.VALUE_OF_BANE + AI.valueForBuff(0, 0, 1)) * 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
