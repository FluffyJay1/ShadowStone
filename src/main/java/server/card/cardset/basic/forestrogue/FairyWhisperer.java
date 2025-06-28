package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.resolver.CreateCardResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.Collections;
import java.util.List;

public class FairyWhisperer extends MinionText {
    public static final String NAME = "Fairy Whisperer";
    public static final String DESCRIPTION = "<b>Battlecry</b>: Add 2 <b>Fairies</b> to your hand.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/fairywhisperer.png"),
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, FairyWhisperer.class,
            new Vector2f(150, 121), 1.5, new EventAnimationDamageOrbFall(),
            () -> List.of(Tooltip.BATTLECRY, Fairy.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, CreateCardResolver.builder()
                        .withCards(List.of(new Fairy(), new Fairy()))
                        .withTeam(owner.team)
                        .withStatus(CardStatus.HAND)
                        .withPos(List.of(-1, -1))
                        .build());
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new Fairy().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToHand(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
