package server.card.cardset.special.batter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
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

public class Alpha extends MinionText {
    public static final String NAME = "Add-on: Alpha";
    public static final String DESCRIPTION = "<b>Bane</b>.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/special/alpha.png",
            CRAFT, TRAITS, RARITY, 4, 2, 2, 2, true, Alpha.class,
            new Vector2f(), -1, EventAnimationDamageOff.class,
            () -> List.of(Tooltip.BANE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.BANE, 1)
                .build()
        ));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
