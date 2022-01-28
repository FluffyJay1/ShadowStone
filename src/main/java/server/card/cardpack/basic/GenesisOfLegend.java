package server.card.cardpack.basic;

import java.util.*;
import java.util.stream.Collectors;

import client.*;
import client.tooltip.*;
import org.newdawn.slick.geom.Vector2f;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class GenesisOfLegend extends Amulet {
    public static final String NAME = "Gensis of Legend";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>. At the end of your turn, give a random allied minion +0/+0/+1 and <b>Bane</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/genesisoflegend.png",
            CRAFT, 2, GenesisOfLegend.class, new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.BANE));

    public GenesisOfLegend(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public Resolver onTurnEnd() {
                return new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        List<Minion> possible = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        if (!possible.isEmpty()) {
                            Minion selected = Game.selectRandom(possible);
                            EffectStatChange esc = new EffectStatChange(
                                    "+0/+0/+1 and <b>Bane</b> (from <b>Genesis of Legend</b>).", 0, 0, 1);
                            esc.effectStats.set.setStat(EffectStats.BANE, 1);
                            this.resolve(b, rl, el, new AddEffectResolver(selected, esc));
                        }
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                return 4;
            }
        };
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 3);
        this.addEffect(true, e);
    }
}
