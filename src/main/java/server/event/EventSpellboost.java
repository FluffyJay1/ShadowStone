package server.event;

import client.Game;
import server.Board;
import server.card.Card;
import server.card.effect.EffectStats;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class EventSpellboost extends Event {
    public static final int ID = 19;
    public List<Card> cards;
    List<Integer> oldSpellboosts;

    public EventSpellboost(List<Card> cards) {
        super(ID);
        this.cards = cards;
    }

    @Override
    public void resolve(Board b) {
        this.oldSpellboosts = new ArrayList<>(this.cards.size());
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            this.oldSpellboosts.add(c.spellboosts);
            if (c.finalStatEffects.getStat(EffectStats.SPELLBOOSTABLE) > 0) {
                c.spellboosts++;
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            c.spellboosts = this.oldSpellboosts.get(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id).append(" ").append(this.cards.size()).append(" ");
        for (Card c : this.cards) {
            sb.append(c.toReference());
        }
        sb.append(Game.EVENT_END);
        return sb.toString();
    }

    public static EventSpellboost fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(Card.fromReference(b, st));
        }
        return new EventSpellboost(cards);
    }
}
