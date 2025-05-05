package server.card.cardset.indie.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.cardset.indie.neutral.Elsen;
import server.card.effect.Effect;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class Dedan extends MinionText {
    public static final String NAME = "Dedan";
    private static final String ONTURNEND_DESCRIPTION = "At the end of your turn, summon 2 <b>Elsens</b>.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever another allied minion comes into play, " +
            "if <b>Vengeance</b> isn't active for you, deal 1 damage to your leader.";
    public static final String DESCRIPTION = ONTURNEND_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/indie/dedan.png"),
            CRAFT, TRAITS, RARITY, 5, 2, 2, 5, true, Dedan.class,
            new Vector2f(135, 106), 1.7, new EventAnimationDamageOff(),
            () -> List.of(Elsen.TOOLTIP, Tooltip.VENGEANCE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;

            @Override
            public ResolverWithDescription onTurnEndAllied() {
                return new ResolverWithDescription(ONTURNEND_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> m = Collections.nCopies(2, new Elsen());
                        List<Integer> pos = List.of(owner.getIndex(), owner.getIndex() + 2);
                        this.resolve(b, rq, el, new CreateCardResolver(m, owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event != null) {
                    int count = (int) event.cardsEnteringPlay().stream()
                            .filter(bo -> bo instanceof Minion && bo.team == this.owner.team && bo != this.owner)
                            .count();
                    if (count > 0) {
                        Effect effect = this;
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                owner.player.getLeader().ifPresent(l -> {
                                    b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                                    for (int i = 0; i < count; i++) {
                                        if (!owner.player.vengeance()) {
                                            this.resolve(b, rq, el, new DamageResolver(effect, l, 1, true, new EventAnimationDamageOff()));
                                        }
                                    }
                                    b.popEventGroup();
                                });
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(2, new Elsen().constructInstance(this.owner.board));
                }
                return AI.valueForSummoning(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
