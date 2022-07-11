package server.playeraction;

import server.Board;
import server.Player;
import server.ServerBoard;
import server.card.Card;
import server.resolver.MulliganResolver;
import server.resolver.ResolutionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MulliganAction extends PlayerAction {

    public static final int ID = 5;

    Player p;
    List<Card> choices;

    public MulliganAction(Player p, List<Card> choices) {
        super(ID);
        this.p = p;
        this.choices = choices;
    }

    @Override
    public ResolutionResult perform(ServerBoard b) {
        ResolutionResult result = new ResolutionResult();
        // don't care about whose turn it is
        if (!this.p.mulliganed) {
            result.concat(b.resolve(new MulliganResolver(this.p, this.choices), 0));
            // if both players mulliganed, proceed to the game
            if (b.getPlayer(1).mulliganed && b.getPlayer(-1).mulliganed) {
                result.concat(b.endMulliganPhase());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ID + " " + this.p.team + " " + this.choices.size() + " ");
        for (Card c : this.choices) {
            sb.append(c.toReference());
        }
        return sb.toString();
    }

    public static MulliganAction fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        int size = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            cards.add(Card.fromReference(b, st));
        }
        return new MulliganAction(b.getPlayer(team), cards);
    }
}
