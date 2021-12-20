package server.card;

import java.util.*;

import server.*;

public class Deck {
    final Board board;
    public final List<Card> cards;
    public final int team;

    public Deck(Board board, int team) {
        this.board = board;
        this.team = team;
        this.cards = new ArrayList<>();
    }

    public void updatePositions() {
        for (int i = 0; i < this.cards.size(); i++) {
            this.cards.get(i).cardpos = i;
        }
    }
}
