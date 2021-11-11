package server.card.cardpack.basic;

import java.util.*;

import client.*;
import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class GenesisOfLegend extends Amulet {
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;

    public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Gensis of Legend",
            "<b> Countdown(3). </b> At the end of your turn, give a random allied minion +0/+0/+1 and <b> Bane. </b>",
            "res/card/basic/genesisoflegend.png", CRAFT, 2, GenesisOfLegend.class, Tooltip.COUNTDOWN, Tooltip.BANE);

    public GenesisOfLegend(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver onTurnEnd() {
                return new Resolver(true) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true);
                        if (!possible.isEmpty()) {
                            b.processEvent(rl, el, new EventFlag(owner));
                            Minion selected = Game.selectRandom(possible);
                            EffectStatChange e = new EffectStatChange(
                                    "Gained +0/+0/+1 and <b> Bane </b> from Genesis of Legend.", 0, 0, 1);
                            e.set.setStat(EffectStats.BANE, 1);
                            this.resolve(b, rl, el, new AddEffectResolver(selected, e));
                        }
                    }
                };
            }
        };
        e.set.setStat(EffectStats.COUNTDOWN, 3);
        this.addEffect(true, e);
    }
}