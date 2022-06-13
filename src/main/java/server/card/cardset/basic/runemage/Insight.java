package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.SpellText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class Insight extends SpellText {
    public static final String NAME = "Insight";
    public static final String DESCRIPTION = "Draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/insight.png",
            CRAFT, TRAITS, RARITY, 1, Insight.class,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new DrawResolver(this.owner.player, 1));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
