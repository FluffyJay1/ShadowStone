package server.card.cardset.basic.runemage;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageIceFall;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class Snowman extends MinionText {
    public static final String NAME = "Snowman";
    public static final String DESCRIPTION = "<b>Freezing Touch</b>.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/snowman.png"),
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, true, Snowman.class,
            new Vector2f(150, 130), 1.5, new EventAnimationDamageIceFall(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.FREEZING_TOUCH, 1)
                .build()));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
