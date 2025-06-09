package server.card.cardset.standard.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
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

public class Stegodon extends MinionText {
    public static final String NAME = "Stegodon";
    public static final String DESCRIPTION = "<b>Ward</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/stegodon.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 1, 6, true, Stegodon.class,
            new Vector2f(150, 200), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.STORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
