package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class BellringerAngel extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Bellringer Angel",
            "<b> Ward. </b> \n <b> Last Words: </b> draw a card.", "res/card/basic/bellringerangel.png", CRAFT, 2, 0, 0,
            2, false, BellringerAngel.class, new Vector2f(), -1, Tooltip.WARD, Tooltip.LASTWORDS);

    public BellringerAngel(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver lastWords() {
                return new DrawResolver(owner.board.getPlayer(owner.team), 1);
            }
        };
        e.set.setStat(EffectStats.WARD, 1);
        this.addEffect(true, e);
    }
}