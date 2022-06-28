package server.card.cardset.indie.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventPlayCard;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Japhet extends MinionText {
    public static final String NAME = "Japhet";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever a player plays a card, deal 2 damage a random enemy minion and this minion.";
    public static final String DESCRIPTION = ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/indie/japhet.png",
            CRAFT, TRAITS, RARITY, 5, 2, 2, 10, false, Japhet.class,
            new Vector2f(153, 170), 1.3, new EventAnimationDamageOff(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventPlayCard) {
                    Effect effect = this;
                    return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(true) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            List<Minion> targets = new ArrayList<>(2);
                            List<Minion> possible = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                            if (!possible.isEmpty()) {
                                targets.add(SelectRandom.from(possible));
                            }
                            targets.add((Minion) owner);
                            this.resolve(b, rq, el, new DamageResolver(effect, targets, 2, true, new EventAnimationDamageOff().toString()));
                        }
                    });
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueOfMinionDamage(2) * 3; // ??
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
