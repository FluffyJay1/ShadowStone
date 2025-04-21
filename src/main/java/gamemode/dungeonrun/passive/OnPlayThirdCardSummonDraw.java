package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ServerBoard;
import server.card.CardStatus;
import server.card.cardset.basic.havenpriest.HolyflameTiger;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventPlayCard;
import server.resolver.CreateCardResolver;
import server.resolver.DrawResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class OnPlayThirdCardSummonDraw extends Passive {
    public static final String DESCRIPTION = "After you play the 3rd card in a turn, summon a <b>" + HolyflameTiger.NAME + "</b> and draw a card.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Beast tamer", DESCRIPTION, () -> List.of(HolyflameTiger.TOOLTIP));
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectOnPlayThirdCardSummonDraw());
    }

    public static class EffectOnPlayThirdCardSummonDraw extends Effect {
        // required for reflection
        public EffectOnPlayThirdCardSummonDraw() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onListenEventWhileInPlay(Event event) {
            if (event instanceof EventPlayCard && this.owner.board.getCurrentPlayerTurn() == this.owner.team) {
                EventPlayCard epc = (EventPlayCard) event;
                if (epc.p.team == this.owner.team && this.owner.player.cardsPlayedThisTurn == 3) {
                    return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            this.resolve(b, rq, el, new CreateCardResolver(new HolyflameTiger(), owner.team, CardStatus.BOARD, -1));
                            this.resolve(b, rq, el, new DrawResolver(owner.player, 1));
                        }
                    });
                }
            }
            return null;
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }
    }
}
