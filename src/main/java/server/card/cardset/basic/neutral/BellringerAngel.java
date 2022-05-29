package server.card.cardset.basic.neutral;

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
    public static final String DESCRIPTION = "<b>Ward</b>.\n<b>Last Words</b>: draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/bellringerangel.png",
            CRAFT, RARITY,2, 0, 0, 2, false, BellringerAngel.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.WARD, Tooltip.LASTWORDS));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.WARD, 1)
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
