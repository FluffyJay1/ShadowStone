package server.card.target;

import server.Board;
import server.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class CardTargetList extends TargetList<Card> {
    public CardTargetList(List<Card> targeted) {
        this.targeted = targeted;
    }

    public Stream<Card> getStillTargetable(CardTargetingScheme scheme) {
        return this.targeted.stream().filter(scheme::canTarget);
    }

    @Override
    public TargetList<Card> clone() {
        return new CardTargetList(new ArrayList<>(this.targeted));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName()).append(" ").append(this.targeted.size()).append(" ");
        for (Card c : this.targeted) {
            sb.append(c.toReference());
        }
        return sb.toString();
    }

    public static TargetList<Card> fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        List<Card> targeted = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            targeted.add(Card.fromReference(b, st));
        }
        return new CardTargetList(targeted);
    }
}
