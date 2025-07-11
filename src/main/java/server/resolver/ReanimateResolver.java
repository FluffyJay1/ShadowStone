package server.resolver;

import server.Player;
import server.ServerBoard;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;
import java.util.stream.Collectors;

public class ReanimateResolver extends Resolver {
    Player p;
    int amount, pos;
    public Minion reanimated;

    public ReanimateResolver(Player p, int amount, int pos) {
        super(true);
        this.p = p;
        this.amount = amount;
        this.pos = pos;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Card> relevant = this.p.getGraveyard().stream()
                .filter(c -> c instanceof Minion && c.finalBasicStats.get(Stat.COST) <= this.amount)
                .collect(Collectors.toList());
        relevant.stream()
                .map(c -> c.finalBasicStats.get(Stat.COST))
                .max(Integer::compareTo)
                .ifPresent(cost -> {
                    List<Card> highest = relevant.stream()
                            .filter(c -> c.finalBasicStats.get(Stat.COST) == cost)
                            .collect(Collectors.toList());
                    MinionText selected = ((Minion) SelectRandom.from(highest)).getCardText();
                    CreateCardResolver ccr = this.resolve(b, rq, el, CreateCardResolver.builder()
                            .withCard(selected)
                            .withTeam(this.p.team)
                            .withStatus(CardStatus.BOARD)
                            .withPos(this.pos)
                            .build());
                    if (ccr.event.successful.get(0)) {
                        this.reanimated = (Minion) ccr.event.cards.get(0);
                    }
                });
    }
}
