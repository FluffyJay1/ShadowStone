package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class RemoveEffectResolver extends Resolver {
    final List<Effect> effects;
    public final List<Card> destroyed;

    public RemoveEffectResolver(List<Effect> effects) {
        super(false);
        this.destroyed = new LinkedList<>();
        this.effects = effects;
        this.essential = true;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventRemoveEffect(this.effects, this.destroyed));
        this.resolve(b, rq, el, new DestroyResolver(this.destroyed));
    }

}
