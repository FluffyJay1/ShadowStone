package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class TimeOwl extends MinionText {
    public static final String NAME = "Time Owl";
    public static final String DESCRIPTION = "<b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/timeowl.png",
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, true, TimeOwl.class,
            new Vector2f(142, 166), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build())
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
