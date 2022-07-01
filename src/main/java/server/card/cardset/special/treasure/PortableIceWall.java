package server.card.cardset.special.treasure;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import java.util.List;

public class PortableIceWall extends MinionText {
    public static final String NAME = "Portable Ice Wall";
    public static final String DESCRIPTION = "<b>Disarmed</b>. <b>Ward</b>. <b>Freezing Touch</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/special/portableicewall.png",
            CRAFT, TRAITS, RARITY, 1, 3, 1, 15, true, PortableIceWall.class,
            new Vector2f(147, 184), 1.2, new EventAnimationDamageMagicHit(),
            () -> List.of(Tooltip.DISARMED, Tooltip.WARD, Tooltip.FREEZING_TOUCH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.DISARMED, 1)
                .set(Stat.WARD, 1)
                .set(Stat.FREEZING_TOUCH, 1)
                .build()) {
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
