package server.resolver.meta;

import server.resolver.Resolver;

// literally a <String, Resolver> pair
public class ResolverWithDescription {
    String description;
    Resolver r;

    public ResolverWithDescription(String description, Resolver r) {
        this.description = description;
        this.r = r;
    }
}
