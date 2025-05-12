package server.card.cardset.basic.forestrogue;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.resolver.CreateCardResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class WaterFairy extends MinionText {
    public static final String NAME = "Water Fairy";
    public static final String DESCRIPTION = "<b>Last Words</b>: Put a <b>Fairy</b> into your hand.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/waterfairy.png"),
            CRAFT, TRAITS, RARITY, 1, 2, 1, 1, true, WaterFairy.class,
            new Vector2f(130, 170), 1.3, new EventAnimationDamageOrbFall(),
            () -> List.of(Tooltip.LASTWORDS, Fairy.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(DESCRIPTION, new CreateCardResolver(List.of(new Fairy()), owner.team, CardStatus.HAND, List.of(-1)));
            }

            @Override
            public double getLastWordsValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(new Fairy().constructInstance(this.owner.board));
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
