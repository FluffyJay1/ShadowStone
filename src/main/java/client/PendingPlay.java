package client;

import server.card.Card;
import utils.PendingManager;

public class PendingPlay {
    public Card card;
    public PendingPlay(Card card) {
        this.card = card;
    }

    public interface PendingPlayer {
        PendingManager.Processor<PendingPlay> getPendingPlayProcessor();
    }
}
