package server.card.unleashpower.basic;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class UnleashEmbraceNature extends UnleashPowerText {
    public static final String NAME = "Embrace Nature";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If it has already attacked this turn, return it to your hand and subtract 1 from its cost.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/basic/embracenature.png",
            CRAFT, RARITY, 2, UnleashEmbraceNature.class,
            new Vector2f(653, 565), 3,
            () -> List.of(Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onUnleashPost(Minion m) {
                Effect effect = this; // anonymous fuckery
                String resolverDescription = "If the unleashed minion has attacked this turn, return it to your hand and subtract 1 from its cost.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            this.resolve(b, rq, el, new PutCardResolver(m, CardStatus.HAND, effect.owner.team, -1, true));
                            if (m.alive) {
                                Effect esc = new Effect("-1 cost (from <b>Embrace Nature</b>).");
                                esc.effectStats.change.setStat(EffectStats.COST, -1);
                                this.resolve(b, rq, el, new AddEffectResolver(m, esc));
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public TooltipUnleashPower getTooltip() {
        return TOOLTIP;
    }
}
