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

public class StonetuskBoar extends Minion {
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final TooltipMinion TOOLTIP = new TooltipMinion("Stonetusk Boar",
            "<b>Storm</b>.", "res/card/basic/stonetuskboar.png", CRAFT, 1, 1, 0,
            1, false, StonetuskBoar.class, new Vector2f(), -1, EventAnimationDamageSlash.class, Tooltip.STORM);

    public StonetuskBoar(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect();
        e.effectStats.set.setStat(EffectStats.STORM, 1);
        this.addEffect(true, e);
    }
}
