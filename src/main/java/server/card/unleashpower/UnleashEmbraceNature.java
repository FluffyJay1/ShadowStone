package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashEmbraceNature extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Embrace Nature",
            "<b> Unleash </b> an allied minion. If it has already attacked this turn, return it to your hand and subtract 1 from its cost.",
            "res/unleashpower/embracenature.png", CRAFT, 2, UnleashEmbraceNature.class, Tooltip.UNLEASH);

    public UnleashEmbraceNature(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect("Returns an allied minion post-unleash if it has already attacked.") {
            @Override
            public Resolver onUnleashPost(Minion m) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            this.resolve(b, rl, el, new PutCardResolver(m, CardStatus.HAND, effect.owner.team, -1));
                            if (m.alive) {
                                Effect esc = new Effect("Cost reduced by 1 from <b> Embrace Nature. </b>");
                                esc.effectStats.change.setStat(EffectStats.COST, -1);
                                this.resolve(b, rl, el, new AddEffectResolver(m, esc));
                            }
                        }
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
