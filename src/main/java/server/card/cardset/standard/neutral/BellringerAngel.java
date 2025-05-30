package server.card.cardset.standard.neutral;

import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class BellringerAngel extends MinionText {
    public static final String NAME = "Bellringer Angel";
    public static final String DESCRIPTION = "<b>Ward</b>.\n<b>Last Words</b>: Draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/bellringerangel.png"),
            CRAFT, TRAITS, RARITY, 1, 0, 0, 2, true, BellringerAngel.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.WARD, Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.WARD, 1)
                .build()
        ) {
            @Override
            public ResolverWithDescription lastWords() {
                String resolverDescription = "<b>Last Words</b>: draw a card.";
                return new ResolverWithDescription(resolverDescription, new DrawResolver(owner.board.getPlayer(owner.team), 1));
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 1;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
