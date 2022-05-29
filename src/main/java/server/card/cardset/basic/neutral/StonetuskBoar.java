package server.card.cardset.basic.neutral;

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

import java.util.List;

public class StonetuskBoar extends MinionText {
    public static final String NAME = "Stonetusk Boar";
    public static final String DESCRIPTION = "<b>Storm</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/stonetuskboar.png",
            CRAFT, TRAITS, RARITY, 1, 1, 0, 1, false, StonetuskBoar.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.STORM));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.STORM, 1)
                .build()
        ));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
