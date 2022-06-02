package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.common.EffectLastWordsSummon;

import java.util.List;

public class Leonidas extends MinionText {
    public static final String NAME = "Leonidas";
    public static final String DESCRIPTION = "<b>Last Words</b>: Summon a <b>Leonidas' Resolve</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/leonidas.png",
            CRAFT, TRAITS, RARITY, 9, 7, 3, 8, true, Leonidas.class,
            new Vector2f(161, 126), 1.8, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LASTWORDS, LeonidasResolve.TOOLTIP));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectLastWordsSummon(DESCRIPTION, new LeonidasResolve(), 1));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
