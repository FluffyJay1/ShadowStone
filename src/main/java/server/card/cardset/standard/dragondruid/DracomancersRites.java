package server.card.cardset.standard.dragondruid;

import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventDiscard;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;
import java.util.stream.IntStream;

public class DracomancersRites extends AmuletText {
    public static final String NAME = "Dracomancer's Rites";
    public static final String DESCRIPTION = "Whenever you discard cards, draw a card for each card discarded.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/standard/dracomancersrites.png"),
            CRAFT, TRAITS, RARITY, 3, DracomancersRites.class,
            new Vector2f(123, 145), 1.4,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventDiscard) {
                    EventDiscard ed = (EventDiscard) event;
                    int numRelevant = (int) IntStream.range(0, ed.cards.size())
                            .filter(i -> ed.successful.get(i))
                            .mapToObj(ed.cards::get)
                            .filter(c -> c.team == this.owner.team)
                            .count();
                    if (numRelevant > 0) {
                        return new ResolverWithDescription(DESCRIPTION, new DrawResolver(this.owner.player, numRelevant));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
