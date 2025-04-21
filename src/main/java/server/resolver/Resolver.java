package server.resolver;

import java.util.*;

import server.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

// my goals are beyond your understanding
// events are solely to record changes in board state, resolvers are for server-side logic
// ideally the resolvers that effects have don't directly process events, they use special resolvers that ensure the board state stays valid
public abstract class Resolver {
    // max depth of non-essential resolvers
    public static final int MAX_DEPTH = 32;

    // max number of "delayed" resolvers
    public static final int MAX_WIDTH = 64;

    public boolean rng;
    public int depth; // current depth

    // if this is critical to maintaining board state invariants
    // isn't limited by max depth
    public boolean essential;

    public Resolver(boolean rng) {
        this.rng = rng;
        this.depth = 0;
        this.essential = false;
    }

    // meant to be overridden, unique to each resolver
    // rl = the list of resolvers to add to, if we need to delay resolving
    // el = the list accumulating all the events, upon leaving method all occurred
    // events should be added to el
    public abstract void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el);

    /*
     * a bit of overhead for transferring resolve contexts, resolve the subresolver
     * and if there was rng involved in it, then the parent resolver also has rng in
     * it, also if the game is over we try our best to back out, next best solution
     * would be a try/catch for when the game ends
     */
    protected final <T extends Resolver> T resolve(ServerBoard b, ResolverQueue rq, List<Event> el, T r) {
        if (r != null && b.getWinner() == 0) {
            r.depth = this.depth + 1;
            if (r.depth > MAX_DEPTH) {
                System.err.println("MAX RESOLVER DEPTH REACHED SOMEHOW");
                for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                    System.out.println(ste);
                }
            }
            if (r.essential || r.depth <= MAX_DEPTH) {
                r.onResolve(b, rq, el);
                if (r.rng) {
                    this.rng = true;
                }
            }
        }
        return r;
    }

    /*
     * Resolves a list of resolvers sequentially, adding them to out, if out == in
     * then it keeps resolving until there's nothing, breaks early if the game has
     * ended
     */
    protected final <T extends Resolver> void resolveQueue(ServerBoard b, ResolverQueue out, List<Event> el, ResolverQueue in) {
        while (!in.isEmpty()) {
            if (b.getWinner() != 0) {
                break;
            }
            this.resolve(b, out, el, in.remove());
        }
    }
}
