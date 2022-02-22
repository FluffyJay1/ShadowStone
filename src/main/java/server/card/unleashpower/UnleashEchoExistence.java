package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class UnleashEchoExistence extends UnleashPower {
    public static final String NAME = "Echo Existence";
    public static final String DESCRIPTION = "<b>Unleash</b> an allied minion. If it has already attacked this turn, add a copy of it to your deck and subtract 2 from its cost.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower(NAME, DESCRIPTION, "res/unleashpower/echoexistence.png",
            CRAFT, RARITY, 2, UnleashEchoExistence.class,
            new Vector2f(430, 445), 1.5,
            () -> List.of(Tooltip.UNLEASH));

    public UnleashEchoExistence(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver onUnleashPost(Minion m) {
                Effect effect = this; // anonymous fuckery
                return new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
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
