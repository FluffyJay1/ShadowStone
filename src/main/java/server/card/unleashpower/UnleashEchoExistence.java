package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashEchoExistence extends UnleashPower {
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Echo Existence",
            "<b>Unleash</b> an allied minion. If it has already attacked this turn, add a copy of it to your deck and subtract 2 from its cost.",
            "res/unleashpower/echoexistence.png", CRAFT, 2, UnleashEchoExistence.class, Tooltip.UNLEASH);

    public UnleashEchoExistence(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver onUnleashPost(Minion m) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(true) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        if (m.attacksThisTurn > 0) {
                            Card copy = Card.createFromConstructor(effect.owner.board, m.getClass());
                            this.resolve(b, rl, el,
                                    new CreateCardResolver(copy, effect.owner.team, CardStatus.DECK,
                                            (int) (effect.owner.board.getPlayer(effect.owner.team).getDeck().size()
                                                    * Math.random())));
                            Effect esc = new Effect("-2 cost (from <b>Echo Existence</b>).");
                            esc.effectStats.change.setStat(EffectStats.COST, -2);
                            this.resolve(b, rl, el, new AddEffectResolver(copy, esc));
                        }
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}
