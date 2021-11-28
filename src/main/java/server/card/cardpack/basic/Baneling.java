package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class Baneling extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Baneling", "<b> Last Words: Blast(5). </b>",
            "res/card/basic/baneling.png", CRAFT, 3, 1, 0, 1, false, Baneling.class, new Vector2f(253, 271), 1.5,
            EventAnimationDamageSlash.class,
            Tooltip.LASTWORDS, Tooltip.BLAST);

    public Baneling(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver lastWords() {
                return new BlastResolver(this, 5, null);
            }
        };
        this.addEffect(true, e);
    }
}
