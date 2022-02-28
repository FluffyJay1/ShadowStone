package server.resolver.util;

import server.resolver.Resolver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Simple resolver queue, with an extra constraint of supporting a max number of
 * add operations, to prevent infinite loops
 */
public class ResolverQueue {
    Queue<Resolver> rl;
    int numAdded;

    public ResolverQueue() {
        this.rl = new ArrayDeque<>(Resolver.MAX_WIDTH);
        this.numAdded = 0;
    }

    public ResolverQueue(List<Resolver> resolvers) {
        this();
        this.rl.addAll(resolvers);
    }

    public void add(Resolver r) {
        if (this.numAdded < Resolver.MAX_WIDTH) {
            this.rl.add(r);
            this.numAdded++;
        } else {
            System.err.println("MAX RESOLVER WIDTH REACHED SOMEHOW");
            System.err.println("TRIED TO ADD: " + r.getClass().getName());
            System.err.println("CURRENT RESOLVERS:");
            for (Resolver resolver : this.rl) {
                System.err.println(resolver.getClass().getName());
            }
        }
    }

    public void addAll(ResolverQueue other) {
        if (!other.isEmpty()) {
            if (this.numAdded < Resolver.MAX_WIDTH) {
                this.rl.addAll(other.rl);
                // only counts for 1 addition because the resolvers in other couldn't have triggered the addition of other esolvers
                this.numAdded++;
            } else {
                System.err.println("MAX RESOLVER WIDTH REACHED SOMEHOW");
                System.err.println("TRIED TO ADD:");
                for (Resolver resolver : other.rl) {
                    System.err.println(resolver.getClass().getName());
                }
                System.err.println("CURRENT RESOLVERS:");
                for (Resolver resolver : this.rl) {
                    System.err.println(resolver.getClass().getName());
                }
            }
        }
    }

    public Resolver remove() {
        return this.rl.remove();
    }

    public boolean isEmpty() {
        return this.rl.isEmpty();
    }
}
