package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ServerBoard;
import server.card.CardStatus;
import server.card.CardText;
import server.card.CardVisibility;
import server.card.SpellText;
import server.card.cardset.CardSet;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class TurnEndRandomSpell extends Passive {
    public static final String DESCRIPTION = "At the end of your turn, if you have 5 or less cards in your hand, put a random spell into your hand and subtract 2 from its cost.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("GAMBA", DESCRIPTION, List::of);
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectTurnEndRandomSpell());
    }
    
    public static class EffectTurnEndRandomSpell extends Effect {
        // required for reflection
        public EffectTurnEndRandomSpell() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    if (owner.player.getHand().size() <= 5) {
                        CardText randomCard = SelectRandom.from(CardSet.PLAYABLE_SET.get().stream().filter(ct -> ct instanceof SpellText).toList());
                        Effect costBuff = new Effect("-2 cost (from passive).", EffectStats.builder()
                                .change(Stat.COST, -2)
                                .build());
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                                .withCard(randomCard)
                                .withTeam(owner.team)
                                .withStatus(CardStatus.HAND)
                                .withPos(-1)
                                .withVisibility(CardVisibility.ALLIES)
                                .withAdditionalEffectForAll(costBuff)
                                .build());
                    }
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }
    }
}
