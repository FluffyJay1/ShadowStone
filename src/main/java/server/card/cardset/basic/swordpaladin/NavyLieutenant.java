package server.card.cardset.basic.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;

import java.util.List;

public class NavyLieutenant extends MinionText {
    public static final String NAME = "Navy Lieutenant";
    public static final String DESCRIPTION = "<b>Aura</b>: Allied Officer minions have <b>Ward</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.COMMANDER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/navylieutenant.png",
            CRAFT, TRAITS, RARITY, 3, 2, 1, 3, true, NavyLieutenant.class,
            new Vector2f(150, 125), 1.5, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.AURA, Tooltip.WARD));

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect ward = new Effect("<b>Ward</b> (from <b>Navy Lieutenant's Aura</b>).", EffectStats.builder()
                .set(EffectStats.WARD, 1)
                .build());
        return List.of(new EffectAura(DESCRIPTION, 1, true, false, ward) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion && cardToApply.finalTraits.contains(CardTrait.OFFICER);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
