package server.card.target;

import server.card.Card;

import java.util.ArrayList;
import java.util.List;
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
}
