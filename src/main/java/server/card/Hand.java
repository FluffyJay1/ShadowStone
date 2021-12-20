package server.card;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Graphics;

import server.Board;

public class Hand { // its just a list of cards aaaaaa
    public static final int DEFAULT_MAX_SIZE = 10;
    public final List<Card> cards;
    public final int maxsize;
    public final int team;
    public final Board board;

    public Hand(Board board, int team) {
        this.board = board;
        this.maxsize = DEFAULT_MAX_SIZE;
        this.cards = new ArrayList<>();
        this.team = team;
    }

    /*
     * public void update(double frametime) { for (int i = 0; i < this.cards.size();
     * i++) { this.cards.get(i).update(frametime); } }
     */
    public void updatePositions() {
        for (int i = 0; i < this.cards.size(); i++) {
            this.cards.get(i).cardpos = i;
        }
    }

}
