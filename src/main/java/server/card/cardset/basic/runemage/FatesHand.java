package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ai.AI;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.effect.common.EffectSpellboostDiscount;
import server.card.target.TargetList;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class FatesHand extends SpellText {
    public static final String NAME = "Fate's Hand";
    public static final String DESCRIPTION = "Draw 2 cards. " + EffectSpellboostDiscount.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/fateshand.png",
            CRAFT, RARITY, 5, FatesHand.class,
            () -> List.of(Tooltip.SPELLBOOST));
    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect("Draw 2 cards.") {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                String resolverDescription = "Draw 2 cards.";
                return new ResolverWithDescription(resolverDescription, new DrawResolver(owner.board.getPlayer(owner.team), 2));
            }

            @Override
            public double getBattlecryValue(int ref) {
                return AI.VALUE_PER_CARD_IN_HAND * 2;
            }
        }, new EffectSpellboostDiscount());
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
