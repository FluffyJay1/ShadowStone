package server.card.cardset.basic.dragondruid;

import client.tooltip.TooltipAmulet;
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
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "res/card/basic/dracomancersrites.png",
            CRAFT, TRAITS, RARITY, 3, DracomancersRites.class,
            new Vector2f(123, 145), 1.4,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEvent(Event e) {
                if (e instanceof EventDiscard && this.owner.isInPlay()) {
                    EventDiscard ed = (EventDiscard) e;
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
