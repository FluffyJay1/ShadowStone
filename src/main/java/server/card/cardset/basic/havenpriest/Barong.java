package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageClaw;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class Barong extends MinionText {
    public static final String NAME = "Barong";
    public static final String DESCRIPTION = "<b>Ward</b>. <b>Elusive</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/barong.png"),
            CRAFT, TRAITS, RARITY, 6, 6, 2, 4, true, Barong.class,
            new Vector2f(133, 160), 1.3, new EventAnimationDamageClaw(),
            () -> List.of(Tooltip.WARD, Tooltip.ELUSIVE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .set(Stat.ELUSIVE, 1)
                .build()));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
