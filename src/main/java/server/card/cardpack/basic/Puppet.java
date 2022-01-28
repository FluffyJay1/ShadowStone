package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

import java.util.List;

public class Puppet extends Minion {
    public static final String NAME = "Puppet";
    public static final String DESCRIPTION = "<b>Rush</b>. <b>Countdown(1)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/puppet.png",
            CRAFT, 0, 1, 0, 1, false, Puppet.class, new Vector2f(161, 143), 1.4, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.RUSH, Tooltip.COUNTDOWN));

    public Puppet(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION);
        e.effectStats.set.setStat(EffectStats.RUSH, 1);
        e.effectStats.set.setStat(EffectStats.COUNTDOWN, 1);
        this.addEffect(true, e);
    }
}
