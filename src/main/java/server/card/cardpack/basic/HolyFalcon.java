package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Board;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

import java.util.function.Supplier;

public class HolyFalcon extends Minion {
    public static final String NAME = "Holy Falcon";
    public static final String DESCRIPTION = "<b>Storm</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/holyfalcon.png",
            CRAFT, 3, 2, 0, 1, false, HolyFalcon.class, new Vector2f(150, 150), 1.3, EventAnimationDamageSlash.class,
            Tooltip.STORM);

    public HolyFalcon(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION);
        e.effectStats.set.setStat(EffectStats.STORM, 1);
        this.addEffect(true, e);
    }
}
