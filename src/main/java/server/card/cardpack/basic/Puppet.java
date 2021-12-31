package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class Puppet extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Puppet",
            "<b> Rush. </b> At the end of your opponent's turn, destroy this minion.", "res/card/basic/puppet.png",
            CRAFT, 0, 1, 0, 1, false, Puppet.class, new Vector2f(161, 143), 1.4,
            EventAnimationDamageSlash.class, Tooltip.RUSH);

    public Puppet(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver onTurnEndEnemy() {
                return new DestroyResolver(owner);
            }
        };
        e.effectStats.set.setStat(EffectStats.RUSH, 1);
        this.addEffect(true, e);
    }
}
