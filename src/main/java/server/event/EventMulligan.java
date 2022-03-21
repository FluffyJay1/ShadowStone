package server.event;

import client.Game;
import server.Board;
import server.Player;
import server.card.Card;
import server.card.CardStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class EventMulligan extends Event {
    public static final int ID = 7;

    Player p;
    List<Card> choices;
    List<Integer> shufflePos;

    public EventMulligan(Player p, List<Card> choices, List<Integer> shufflePos) {
        super(ID);
        this.p = p;
        this.choices = choices;
        this.shufflePos = shufflePos;
    }

    @Override
    public void resolve(Board b) {
        if (!this.p.mulliganed) {
            List<Integer> choicePos = this.choices.stream()
                    .map(Card::getIndex)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            // draw replacements
            for (int i = 0; i < this.choices.size() && !this.p.getDeck().isEmpty(); i++) {
                Card drawnCard = this.p.getDeck().remove(0);
                this.p.getHand().add(choicePos.get(i), drawnCard);
                assert drawnCard.status.equals(CardStatus.HAND);
                // drawnCard.status = CardStatus.HAND; // handled automatically by the PositionedList setter
            }
            // send mulliganed shit to deck
            for (int i = 0; i < this.choices.size(); i++) {
                Card c = this.choices.get(i);
                this.p.getHand().remove(c);
                List<Card> deck = this.p.getDeck();
                deck.add(this.shufflePos.get(i), c);
                // c.status = CardStatus.DECK; // handled automatically by the PositionedList setter
            }
            this.p.mulliganed = true;
        }
    }

    @Override
    public void undo(Board b) {
        // shouldn't ever need to undo a mulligan
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.id + " " + this.p.team + " " + this.choices.size() + " ");
        for (int i = 0; i < this.choices.size(); i++) {
            Card c = this.choices.get(i);
            sb.append(c.toReference()).append(this.shufflePos.get(i)).append(" ");
        }
        sb.append(Game.EVENT_END);
        return sb.toString();
    }

    public static EventMulligan fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        int size = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>(size);
        List<Integer> shufflePos = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            cards.add(Card.fromReference(b, st));
            shufflePos.add(Integer.parseInt(st.nextToken()));
        }
        return new EventMulligan(b.getPlayer(team), cards, shufflePos);
    }

    @Override
    public boolean conditions() {
        for (Card c : this.choices) {
            if (!this.p.getHand().contains(c)) {
                return false;
            }
        }
        return true;
    }
}
