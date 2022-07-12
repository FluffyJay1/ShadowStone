package server.card.cardset.basic.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.AmuletText;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class SacredPlea extends AmuletText {
    public static final String NAME = "Sacred Plea";
    private static final String LASTWORDS_DESCRIPTION = "<b>Last Words</b>: Draw 2 cards.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + LASTWORDS_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/basic/sacredplea.png",
            CRAFT, TRAITS, RARITY, 1, SacredPlea.class,
            new Vector2f(132, 148), 1.4,
            () -> List.of(Tooltip.COUNTDOWN),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.COUNTDOWN, 3)
                .build()) {
            @Override
            public ResolverWithDescription lastWords() {
                return new ResolverWithDescription(LASTWORDS_DESCRIPTION, new DrawResolver(this.owner.player, 2));
            }

            @Override
            public double getLastWordsValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND * 2;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
