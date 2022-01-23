package server.card.target;

import server.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CardTargetList extends TargetList<Card> {
    public CardTargetList(List<Card> list) {
        this.list = list;
    }

    public Stream<Card> getStillTargetable(CardTargetingScheme scheme) {
        return this.list.stream().filter(scheme::canTarget);
    }

    @Override
    public TargetList<Card> clone() {
        return new CardTargetList(new ArrayList<>(this.list));
    }
}
