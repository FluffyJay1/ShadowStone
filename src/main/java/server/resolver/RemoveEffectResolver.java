package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class RemoveEffectResolver extends Resolver {
    final List<Effect> effects;
    public final List<Card> destroyed;
    EffectAura auraSource;

    public RemoveEffectResolver(List<Effect> effects) {
        super(false);
        this.destroyed = new LinkedList<>();
        this.effects = effects;
    }

    public RemoveEffectResolver(List<Effect> effects, EffectAura auraSource) {
        this(effects);
        this.auraSource = auraSource;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventRemoveEffect(this.effects, this.destroyed, this.auraSource));
        this.resolve(b, rl, el, new DestroyResolver(this.destroyed));
    }

}
