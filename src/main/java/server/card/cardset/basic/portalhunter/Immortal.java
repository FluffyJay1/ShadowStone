package server.card.cardset.basic.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageEnergyBeamQuick;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

import java.util.List;

public class Immortal extends MinionText {
    public static final String NAME = "Immortal";
    public static final String DESCRIPTION = "<b>Shield(3)</b>.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/immortal.png",
            CRAFT, RARITY, 6, 5, 2, 3, true, Immortal.class,
            new Vector2f(147, 136), 1.4, EventAnimationDamageEnergyBeamQuick.class,
            () -> List.of(Tooltip.SHIELD));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.SHIELD, 3)
                .build()));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
