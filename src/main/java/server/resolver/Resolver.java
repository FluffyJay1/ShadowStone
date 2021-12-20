package server.resolver;

import java.util.*;

import server.*;
import server.event.*;

// my goals are beyond your understanding
// events are solely to record changes in board state, resolvers are for server-side logic
// ideally the resolvers that effects have don't directly process events, they use special resolvers that ensure the board state stays valid
public abstract class Resolver {
    public boolean rng;

    public Resolver(boolean rng) {
        this.rng = rng;
    }

    // meant to be overridden, unique to each resolver
    // rl = the list of resolvers to add to, if we need to delay resolving
    // el = the list accumulating all the events, upon leaving method all occurred
    // events should be added to el
    public abstract void onResolve(Board b, List<Resolver> rl, List<Event> el);

    /*
     * a bit of overhead for transferring resolve contexts, resolve the subresolver
     * and if there was rng involved in it, then the parent resolver also has rng in
     * it, also if the game is over we try our best to back out, next best solution
     * would be a try/catch for when the game ends
     */
    protected final <T extends Resolver> T resolve(Board b, List<Resolver> rl, List<Event> el, T r) {
        if (b.winner == 0) {
            r.onResolve(b, rl, el);
            if (r.rng) {
                this.rng = true;
            }
        }
        return r;
    }

    /*
     * Resolves a list of resolvers sequentially, adding them to out, if out == in
     * then it keeps resolving until there's nothing, breaks early if the game has
     * ended
     */
    protected final <T extends Resolver> void resolveList(Board b, List<Resolver> out, List<Event> el,
            List<Resolver> in) {
        // TODO prevent infinite loops
        while (!in.isEmpty()) {
            if (b.winner != 0) {
                break;
            }
            this.resolve(b, out, el, in.remove(0));
        }
    }
}
