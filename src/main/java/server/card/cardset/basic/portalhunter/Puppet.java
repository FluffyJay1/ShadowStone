package server.card.cardset.basic.portalhunter;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.card.*;
import server.card.effect.*;

import java.util.List;

public class Puppet extends MinionText {
    public static final String NAME = "Puppet";
    public static final String DESCRIPTION = "<b>Rush</b>. <b>Countdown(1)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/puppet.png",
            CRAFT, TRAITS, RARITY, 0, 1, 0, 1, false, Puppet.class,
            new Vector2f(161, 143), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .set(Stat.COUNTDOWN, 1)
                .build()
        ));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
