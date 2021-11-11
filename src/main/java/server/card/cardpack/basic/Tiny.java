package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class Tiny extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Tiny",
            "<b> Unleash: </b> Gain +2/+0/+2 and <b> Rush. </b>", "res/card/basic/tiny.png", CRAFT, 3, 2, 2, 3, false,
            Tiny.class, new Vector2f(), -1, Tooltip.UNLEASH, Tooltip.RUSH);

    public Tiny(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver unleash() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        EffectStatChange ef = new EffectStatChange("Gained +2/+0/+2 and <b> Rush </b> from Unleash", 2,
                                0, 2);
                        ef.set.setStat(EffectStats.RUSH, 1);
                        this.resolve(b, rl, el, new AddEffectResolver(owner, ef));
                    }
                };
            }
        };
        this.addEffect(true, e);
    }
}