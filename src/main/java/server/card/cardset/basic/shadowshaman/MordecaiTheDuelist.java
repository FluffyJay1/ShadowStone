package server.card.cardset.basic.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.common.EffectLastWordsSummon;

import java.util.List;

public class MordecaiTheDuelist extends MinionText {
    public static final String NAME = "Mordecai the Duelist";
    public static final String DESCRIPTION = "<b>Last Words</b>: Summon a <b>Mordecai the Duelist</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/mordecaitheduelist.png",
            CRAFT, TRAITS, RARITY, 8, 5, 2, 5, true, MordecaiTheDuelist.class,
            new Vector2f(150, 155), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LASTWORDS));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectLastWordsSummon(DESCRIPTION, new MordecaiTheDuelist(), 1));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
