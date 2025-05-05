package server.resolver;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.game.visualboardanimation.eventanimation.putcard.EventAnimationPutCard;
import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class PutCardResolver extends Resolver {

    private final int team;
    private final List<? extends Card> c;
    private final CardStatus status;
    private final List<Integer> pos;
    private final boolean resolveDestroy;
    public final List<Card> destroyed;
    public EventPutCard event;
    private String animationString;

    public PutCardResolver(List<? extends Card> c, CardStatus status, int team, List<Integer> pos, boolean resolveDestroy, EventAnimationPutCard animation) {
        super(false);
        this.c = c;
        this.status = status;
        this.team = team;
        this.pos = pos;
        this.resolveDestroy = resolveDestroy;
        this.destroyed = new LinkedList<>();
        this.animationString = EventAnimation.stringOrNull(animation);
    }

    public PutCardResolver(List<? extends Card> c, CardStatus status, int team, List<Integer> pos, boolean resolveDestroy) {
        this(c, status, team, pos, resolveDestroy, null);
    }

    public PutCardResolver(Card c, CardStatus status, int team, int pos, boolean resolveDestroy) {
        this(List.of(c), status, team, List.of(pos), resolveDestroy);
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        this.event = b.processEvent(rq, el, new EventPutCard(this.c, this.status, this.team, this.pos, false, this.destroyed, this.animationString));
        if (this.resolveDestroy) {
            this.resolve(b, rq, el, new DestroyResolver(this.destroyed, EventDestroy.Cause.NATURAL));
        }
    }

}
