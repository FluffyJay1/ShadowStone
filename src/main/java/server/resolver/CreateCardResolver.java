package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.event.*;

// when u wanna return a resolver but the only thing u have to do is create card
public class CreateCardResolver extends Resolver {
    public EventCreateCard event;
    private final List<CardText> c;
    private final int team;
    private final CardStatus status;
    private final List<Integer> cardpos;

    // cards provided should be freshly constructed
    public CreateCardResolver(List<? extends CardText> c, int team, CardStatus status, List<Integer> cardpos) {
        super(false);
        this.c = new ArrayList<>(c);
        this.team = team;
        this.status = status;
        this.cardpos = cardpos;
    }

    public CreateCardResolver(CardText c, int team, CardStatus status, int cardpos) {
        this(List.of(c), team, status, List.of(cardpos));
    }

    @Override
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        List<Card> cards = this.c.stream()
                .map(ct -> ct.constructInstance(b))
                .collect(Collectors.toList());;
        this.event = b.processEvent(rl, el, new EventCreateCard(cards, this.team, this.status, this.cardpos));
    }

}
