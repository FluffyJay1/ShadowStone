package server.event;

import client.Game;
import client.PendingPlayPositioner;
import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// like destroy, but as a special action for cards in hand, for other cards to interact with
// destroying cards in hand != discarding them, sometimes we want to destroy cards in hand without discarding them
// Changes references, should not run concurrent with other events
public class EventDiscard extends Event {
    public static final int ID = 21;
    public final List<? extends Card> cards;
    private List<Boolean> alive;
    private List<Integer> prevPos;
    private int prevShadows1, prevShadows2;
    public List<Boolean> successful;

    public EventDiscard(List<? extends Card> c) {
        super(ID);
        this.cards = c;
    }

    public EventDiscard(Card c) {
        this(List.of(c));
    }

    @Override
    public void resolve(Board b) {
        this.alive = new ArrayList<>(this.cards.size());
        this.prevPos = new ArrayList<>(this.cards.size());
        this.successful = new ArrayList<>(this.cards.size());
        this.prevShadows1 = b.getPlayer(1).shadows;
        this.prevShadows2 = b.getPlayer(-1).shadows;
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            Player p = b.getPlayer(c.team);
            this.alive.add(c.alive);
            this.prevPos.add(c.getIndex());
            this.successful.add(false);
            if (c.status.equals(CardStatus.HAND)) {
                this.successful.set(i, true);
                p.shadows++;
                c.alive = false;
                p.getHand().remove(c);
                c.status = CardStatus.GRAVEYARD;
                p.getGraveyard().add(c);
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            Player p = b.getPlayer(c.team);
            c.alive = this.alive.get(i);
            int pos = this.prevPos.get(i);
            if (this.successful.get(i)) {
                p.getGraveyard().remove(c);
                p.getHand().add(pos, c);
            }
        }
        b.getPlayer(1).shadows = this.prevShadows1;
        b.getPlayer(-1).shadows = this.prevShadows2;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.cards.size()).append(" ");
        for (Card card : this.cards) {
            builder.append(card.toReference());
        }
        return builder.append(Game.EVENT_END).toString();
    }

    public static EventDiscard fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Card> c = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
        }
        return new EventDiscard(c);
    }

    @Override
    public boolean conditions() {
        return !this.cards.isEmpty();
    }
}
