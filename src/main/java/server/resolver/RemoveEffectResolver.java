package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class RemoveEffectResolver extends Resolver {
    List<Effect> effects;
    public List<Card> destroyed;

    public RemoveEffectResolver(List<Effect> effects) {
        super(false);
        this.destroyed = new LinkedList<>();
        this.effects = effects;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventRemoveEffect(this.effects, this.destroyed));
        this.resolve(b, rl, el, new DestroyResolver(this.destroyed));
    }

}
