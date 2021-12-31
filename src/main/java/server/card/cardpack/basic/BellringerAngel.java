package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class BellringerAngel extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Bellringer Angel",
            "<b> Ward. </b> \n <b> Last Words: </b> draw a card.", "res/card/basic/bellringerangel.png", CRAFT, 2, 0, 0,
            2, false, BellringerAngel.class, new Vector2f(), -1, EventAnimationDamageSlash.class, Tooltip.WARD, Tooltip.LASTWORDS);

    public BellringerAngel(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver lastWords() {
                return new DrawResolver(owner.board.getPlayer(owner.team), 1);
            }

            @Override
            public double getPresenceValue() {
                return AI.VALUE_PER_CARD_IN_HAND * 1 / 2.;
            }
        };
        e.effectStats.set.setStat(EffectStats.WARD, 1);
        this.addEffect(true, e);
    }
}
