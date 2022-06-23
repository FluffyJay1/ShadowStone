package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

// when u wanna return a resolver but the only thing u have to do is create card
public class CreateCardResolver extends Resolver {
    public EventCreateCard event;
    private final List<CardText> c;
    private final int team;
    private final CardStatus status;
    private final List<Integer> cardpos;
    private final CardVisibility visibility;

    // cards provided should be freshly constructed
    public CreateCardResolver(List<? extends CardText> c, int team, CardStatus status, List<Integer> cardpos, CardVisibility visibility) {
        super(false);
        this.c = new ArrayList<>(c);
        this.team = team;
        this.status = status;
        this.cardpos = cardpos;
        this.visibility = visibility;
    }

    public CreateCardResolver(List<? extends CardText> c, int team, CardStatus status, List<Integer> cardpos) {
        this(c, team, status, cardpos, CardVisibility.ALL);
    }
    public CreateCardResolver(CardText c, int team, CardStatus status, int cardpos, CardVisibility visibility) {
        this(List.of(c), team, status, List.of(cardpos), visibility);
    }

    public CreateCardResolver(CardText c, int team, CardStatus status, int cardpos) {
        this(c, team, status, cardpos, CardVisibility.ALL);
    }
    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Card> cards = this.c.stream()
                .map(ct -> ct.constructInstance(b))
                .collect(Collectors.toList());
        this.event = b.processEvent(rq, el, new EventCreateCard(cards, this.team, this.status, this.cardpos, this.visibility));
    }

}
