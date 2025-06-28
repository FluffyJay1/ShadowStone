package server.card.cardset.basic.runemage;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.resolver.CreateCardResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class ConjureGolem extends SpellText {
    public static final String NAME = "Conjure Golem";
    public static final String DESCRIPTION = "Summon a <b>Clay Golem</b>.";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/conjuregolem.png"),
            CRAFT, TRAITS, RARITY, 2, ConjureGolem.class,
            () -> List.of(ClayGolem.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, CreateCardResolver.builder()
                        .withCard(new ClayGolem())
                        .withTeam(this.owner.team)
                        .withStatus(CardStatus.BOARD)
                        .withPos(-1)
                        .build());
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new ClayGolem().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
