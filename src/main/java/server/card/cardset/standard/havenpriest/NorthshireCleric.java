package server.card.cardset.standard.havenpriest;

import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageArrow;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventRestore;
import server.resolver.DrawResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class NorthshireCleric extends MinionText {
    public static final String NAME = "Northshire Cleric";
    public static final String DESCRIPTION = "Whenever a minion is healed, draw a card.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/northshirecleric.png"),
            CRAFT, TRAITS, RARITY, 1, 1, 0, 3, true, NorthshireCleric.class,
            new Vector2f(145, 146), 2, new EventAnimationDamageArrow(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventRestore && ((EventRestore) event).m.stream().anyMatch(minion -> minion.status.equals(CardStatus.BOARD))) {
                    return new ResolverWithDescription(DESCRIPTION, new DrawResolver(owner.player, 1));
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_CARD_IN_HAND;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
