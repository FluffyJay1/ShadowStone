package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class DrawResolver extends Resolver {
    final Player p;
    final int num;

    public DrawResolver(Player p, int num) {
        super(false);
        this.p = p;
        this.num = num;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        for (int i = 0; i < num; i++) {
            if (p.getDeck().size() == 0) {
                // lose the game
                b.processEvent(rq, el, new EventGameEnd(p.board, p.team * -1));
                break;
            } else {
                List<Card> markedForDeath = new LinkedList<>();
                b.processEvent(rq, el, new EventPutCard(List.of(this.p.getDeck().get(0)), CardStatus.HAND, this.p.team,
                        List.of(-1), markedForDeath));
                if (!markedForDeath.isEmpty()) {
                    b.processEvent(rq, el, new EventDestroy(markedForDeath));
                }
            }
        }
    }
}
