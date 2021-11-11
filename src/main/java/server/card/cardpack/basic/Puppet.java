package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class Puppet extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Puppet",
            "<b> Rush. </b> At the end of your opponent's turn, destroy this minion.", "res/card/basic/puppet.png",
            CRAFT, 0, 1, 0, 1, false, Puppet.class, new Vector2f(161, 143), 1.4, Tooltip.RUSH);

    public Puppet(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver onTurnEndEnemy() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        b.processEvent(rl, el, new EventFlag(owner));
                        this.resolve(b, rl, el, new DestroyResolver(owner));
                    }
                };

            }
        };
        e.set.setStat(EffectStats.RUSH, 1);
        this.addEffect(true, e);
    }
}
