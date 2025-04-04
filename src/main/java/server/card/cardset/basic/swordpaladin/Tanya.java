package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageDoubleSlice;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class Tanya extends MinionText {
    public static final String NAME = "Tanya, Shadow Enforcer";
    public static final String DESCRIPTION = "<b>Bane</b>. <b>Stealth</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/tanya.png"),
            CRAFT, TRAITS, RARITY, 2, 3, 0, 1, true, Tanya.class,
            new Vector2f(128, 149), 1.5, new EventAnimationDamageDoubleSlice(),
            () -> List.of(Tooltip.BANE, Tooltip.STEALTH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.BANE, 1)
                .set(Stat.STEALTH, 1)
                .build()));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
