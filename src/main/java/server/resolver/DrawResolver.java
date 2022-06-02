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
        List<Card> markedForDeath = new LinkedList<>();
        int numToDraw = Math.min(this.num, this.p.getDeck().size());
        b.processEvent(rq, el, new EventPutCard(List.copyOf(this.p.getDeck().subList(0, numToDraw)), CardStatus.HAND, this.p.team,
                Collections.nCopies(numToDraw, -1), false, markedForDeath));
        if (!markedForDeath.isEmpty()) {
            b.processEvent(rq, el, new EventDestroy(markedForDeath));
        }
        if (numToDraw < this.num) {
            // lose the game
            b.processEvent(rq, el, new EventGameEnd(p.board, p.team * -1));
        }
    }
}
