package server.card.cardpack.basic;

import java.util.*;
import java.util.stream.Collectors;

import client.*;
import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WellOfDestination extends Amulet {
    public static final String NAME = "Well of Destination";
    public static final String DESCRIPTION = "At the start of your turn, give a random allied minion +1/+1/+1.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/wellofdestination.png",
            CRAFT, 2, WellOfDestination.class, new Vector2f(), -1);

    public WellOfDestination(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver onTurnStart() {
                return new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!possible.isEmpty()) {
                            Minion targeted = Game.selectRandom(possible);
                            EffectStatChange e = new EffectStatChange("+1/+1/+1 (from <b>Well of Destination</b>).", 1, 1,
                                    1);
                            this.resolve(b, rl, el, new AddEffectResolver(targeted, e));
                        }
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_1_1_STATS * 4 / 2.;
            }
        };
        this.addEffect(true, e);
    }

}
