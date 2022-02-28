package server.resolver;

import server.ServerBoard;
import server.card.Card;
import server.card.CardText;
import server.event.Event;
import server.event.EventTransform;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransformResolver extends Resolver {
    public EventTransform event;
    List<Card> c;
    List<CardText> into;

    public TransformResolver(List<Card> c, List<CardText> into) {
        super(false);
        this.c = c;
        this.into = into;
    }

    public TransformResolver(List<Card> c, CardText into) {
        this(c, Collections.nCopies(c.size(), into));
    }

    public TransformResolver(Card c, CardText into) {
        this(List.of(c), List.of(into));
    }

    @Override
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        List<Card> transformInto = this.into.stream()
                .map(ct -> ct.constructInstance(b))
                .collect(Collectors.toList());
        this.event = b.processEvent(rl, el, new EventTransform(this.c, transformInto));
    }
}
