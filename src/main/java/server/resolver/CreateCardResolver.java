package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class CreateCardResolver extends Resolver {
    public final List<Card> destroyed;
    private final List<Card> c;
    private final int team;
    private final CardStatus status;
    private final List<Integer> cardpos;

    public CreateCardResolver(List<Card> c, int team, CardStatus status, List<Integer> cardpos) {
        super(false);
        this.c = c;
        this.team = team;
        this.status = status;
        this.cardpos = cardpos;
        this.destroyed = new LinkedList<>();
    }

    public CreateCardResolver(Card c, int team, CardStatus status, int cardpos) {
        this(List.of(c), team, status, List.of(cardpos));
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventCreateCard(this.c, this.team, this.status, this.cardpos, destroyed));
        this.resolve(b, rl, el, new DestroyResolver(destroyed));
    }

}
