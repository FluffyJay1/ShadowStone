package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WeatheredVanguard extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Weathered Vanguard",
            "<b> Battlecry: </b> Summon 2 <b> Knights. Unleash: </b> Give all allied minions +1/+0/+1.",
            "res/card/basic/weatheredvanguard.png", CRAFT, 6, 4, 2, 4, false, WeatheredVanguard.class,
            new Vector2f(155, 120), 1.6, Tooltip.BATTLECRY, Knight.TOOLTIP, Tooltip.UNLEASH);

    public WeatheredVanguard(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Card> knights = List.of(new Knight(b), new Knight(b));
                        List<Integer> pos = List.of(owner.cardpos + 1, owner.cardpos);
                        this.resolve(b, rl, el, new CreateCardResolver(knights, owner.team, CardStatus.BOARD, pos));
                    }
                };
            }

            @Override
            public Resolver unleash() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Minion> minions = b.getMinions(owner.team, false, true);
                        if (!minions.isEmpty()) {
                            Effect stats = new EffectStatChange("+1/+0/+1 from Weathered Vanguard.", 1, 0, 1);
                            this.resolve(b, rl, el, new AddEffectResolver(minions, stats));
                        }
                    }
                };
            }
        };
        this.addEffect(true, e);
    }

}
