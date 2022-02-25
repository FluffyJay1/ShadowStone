package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

import java.util.List;

public class Puppet extends MinionText {
    public static final String NAME = "Puppet";
    public static final String DESCRIPTION = "<b>Rush</b>. <b>Countdown(1)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/puppet.png",
            CRAFT, RARITY, 0, 1, 0, 1, false, Puppet.class,
            new Vector2f(161, 143), 1.4, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.RUSH, Tooltip.COUNTDOWN));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, new EffectStats(
                new EffectStats.Setter(EffectStats.RUSH, false, 1),
                new EffectStats.Setter(EffectStats.COUNTDOWN, false, 1)
        )));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
