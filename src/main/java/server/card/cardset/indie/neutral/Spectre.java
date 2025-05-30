package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class Spectre extends MinionText {
    public static final String NAME = "Spectre";
    public static final String DESCRIPTION = "<b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/indie/spectre.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, Spectre.class,
            new Vector2f(150, 180), 1.2, new EventAnimationDamageOff(),
            () -> List.of(Tooltip.RUSH),
            List.of());
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()
        ));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
