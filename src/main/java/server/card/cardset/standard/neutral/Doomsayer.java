package server.card.cardset.standard.neutral;

import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.*;
import server.event.Event;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class Doomsayer extends MinionText {
    public static final String NAME = "Doomsayer";
    public static final String DESCRIPTION = "At the start of your turn, destroy all minions.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/doomsayer.png"),
            CRAFT, TRAITS, RARITY, 2, 0, 1, 7, true, Doomsayer.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onTurnStartAllied() {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> targets = b.getMinions(0, false, true).toList();
                        this.resolve(b, rq, el, new DestroyResolver(targets));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_OF_DESTROY * 3;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
