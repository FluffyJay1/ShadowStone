package server.card.cardpack.basic;

import java.util.*;

import client.*;
import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WellOfDestination extends Amulet {
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Well of Destination",
            "At the start of your turn, give a random allied minion +1/+1/+1.", "res/card/basic/wellofdestination.png",
            CRAFT, 2, WellOfDestination.class);

    public WellOfDestination(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect("At the start of your turn, give a random allied minion +1/+1/+1") {
            @Override
            public Resolver onTurnStart() {
                return new Resolver(true) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true);
                        if (!possible.isEmpty()) {
                            Minion targeted = Game.selectRandom(possible);
                            EffectStatChange e = new EffectStatChange("Gained +1/+1/+1 from Well of Destination", 1, 1,
                                    1);
                            this.resolve(b, rl, el, new AddEffectResolver(targeted, e));
                        }
                    }
                };
            }
        };
        this.addEffect(true, e);
    }

}
