package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.resolver.DiscardLowestResolver;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class PyroxeneDragon extends MinionText {
    public static final String NAME = "Pyroxene Dragon";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Randomly discard 1 of the lowest-cost cards in your hand. Do this 2 times.";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Draw 3 cards.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/pyroxenedragon.png",
            CRAFT, TRAITS, RARITY, 4, 3, 2, 4, true, PyroxeneDragon.class,
            new Vector2f(151, 160), 1.2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new DiscardLowestResolver(this.owner.player, 2));
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.VALUE_OF_DISCARD * 2;
            }

            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new DrawResolver(this.owner.player, 3));
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
