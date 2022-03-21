package server.resolver;

import server.Player;
import server.ServerBoard;
import server.card.Card;
import server.event.Event;
import server.event.EventMulligan;
import server.event.EventMulliganPhaseEnd;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.List;

public class MulliganResolver extends Resolver {
    Player p;
    List<Card> choices;

    public MulliganResolver(Player p, List<Card> choices) {
        super(true);
        this.p = p;
        this.choices = choices;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Integer> shufflePos = new ArrayList<>(this.choices.size());
        int tempDeckSize = Math.max(0, this.p.getDeck().size() - this.choices.size());
        for (int i = 0; i < this.choices.size(); i++) {
            shufflePos.add((int) (Math.random() * tempDeckSize));
            tempDeckSize++;
        }
        b.processEvent(rq, el, new EventMulligan(this.p, this.choices, shufflePos));
        // if both players mulliganed, proceed to the game
        if (b.getPlayer(1).mulliganed && b.getPlayer(-1).mulliganed) {
            b.processEvent(rq, el, new EventMulliganPhaseEnd());
            this.resolve(b, rq, el, new TurnStartResolver(b.getPlayer(1)));
        }
    }
}