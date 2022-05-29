package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

import java.util.List;

public class Tanya extends MinionText {
    public static final String NAME = "Tanya, Shadow Enforcer";
    public static final String DESCRIPTION = "<b>Bane</b>. <b>Stealth</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/tanya.png",
            CRAFT, RARITY, 3, 3, 0, 1, true, Tanya.class,
            new Vector2f(128, 149), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.BANE, Tooltip.STEALTH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.BANE, 1)
                .set(EffectStats.STEALTH, 1)
                .build()));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
