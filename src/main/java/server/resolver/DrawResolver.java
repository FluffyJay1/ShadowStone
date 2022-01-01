package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class DrawResolver extends Resolver {
    final Player p;
    final int num;

    public DrawResolver(Player p, int num) {
        super(false);
        this.p = p;
        this.num = num;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        for (int i = 0; i < num; i++) {
            if (p.deck.cards.size() == 0) {
                // lose the game
                b.processEvent(rl, el, new EventGameEnd(p.board, p.team * -1));
                break;
            } else {
                if (p.hand.cards.size() < p.hand.maxsize) {
                    b.processEvent(rl, el, new EventPutCard(this.p.deck.cards.get(0), CardStatus.HAND, this.p.team,
                            this.p.hand.maxsize, null));
                } else {
                    b.processEvent(rl, el, new EventDestroy(this.p.deck.cards.get(0)));
                    // hmm
                    // this.resolve(b, rl, el, new MillResolver(this.p.deck.cards.get(0)));
                }
            }
        }
    }
}
