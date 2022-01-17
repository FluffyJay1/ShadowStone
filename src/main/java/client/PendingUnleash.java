package client;

import server.card.Card;
import server.card.Minion;
import utils.PendingManager;

public class PendingUnleash {
    public Card source;
    public Minion m;
    public PendingUnleash(Card source, Minion m) {
        this.source = source;
        this.m = m;
    }

    public interface PendingUnleasher {
        PendingManager.Processor<PendingUnleash> getPendingUnleashProcessor();
    }
}
