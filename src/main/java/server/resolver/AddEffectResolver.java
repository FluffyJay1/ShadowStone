package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class AddEffectResolver extends Resolver {
    final List<? extends Card> c;
    final Effect e;

    public List<Effect> effects;
    public final List<Card> destroyed;

    public AddEffectResolver(List<? extends Card> c, Effect e) {
        super(false);
        this.c = c;
        this.e = e;
        this.destroyed = new LinkedList<>();
        this.essential = true;
    }

    public AddEffectResolver(Card c, Effect e) {
        this(List.of(c), e);
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        EventAddEffect addEffect = new EventAddEffect(this.c, e, this.destroyed);
        b.processEvent(rq, el, addEffect);
        this.effects = addEffect.effects;
        this.resolve(b, rq, el, new DestroyResolver(this.destroyed, EventDestroy.Cause.NATURAL));
    }

}
