package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;

public class EventPutCard extends Event {
	// for effects that put specific cards in hand or just draw cards
	public static final int ID = 12;
	Player p;
	public ArrayList<Card> c;
	public ArrayList<Integer> pos; // pos == -1 means last
	public CardStatus status;
	int targetTeam;

	public EventPutCard(Player p, ArrayList<Card> c, CardStatus status, int team, ArrayList<Integer> pos) {
		super(ID);
		this.p = p;
		this.c = new ArrayList<Card>();
		this.c.addAll(c);
		this.status = status;
		this.targetTeam = team;
		this.pos = new ArrayList<Integer>();
		this.pos.addAll(pos);
	}

	public EventPutCard(Player p, Card c, CardStatus status, int team, int pos) {
		super(ID);
		this.p = p;
		this.c = new ArrayList<Card>();
		this.c.add(c);
		this.status = status;
		this.targetTeam = team;
		this.pos = new ArrayList<Integer>();
		this.pos.add(pos);
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (int i = 0; i < this.c.size(); i++) {
			Card card = this.c.get(i);
			switch (card.status) { // removing from
			case HAND:
				this.p.hand.cards.remove(card);
				this.p.hand.updatePositions();
				break;
			case BOARD:
				card.removeAdditionalEffects();
				if (card instanceof Minion) {
					((Minion) card).health = card.finalStatEffects.getStat(EffectStats.HEALTH);
				}
				this.p.board.removeBoardObject((BoardObject) card);
				if (!loopprotection) {
					eventlist.add(new EventLeavePlay(card));
				}
				break;
			case DECK:
				this.p.deck.cards.remove(card);
				this.p.deck.updatePositions();
				break;
			default:
				break;
			}
			card.team = this.targetTeam;
			if (this.status.equals(CardStatus.BOARD)) { // now adding to
				if (card instanceof BoardObject) {
					this.p.board.addBoardObject((BoardObject) card, this.targetTeam,
							this.pos.get(i) == -1 ? this.p.board.getBoardObjects(this.p.team).size() : this.pos.get(i));
					if (card instanceof Minion) {
						((Minion) card).summoningSickness = true;
					}
					if (!loopprotection) {
						eventlist.add(new EventEnterPlay(card));
					}
				}
			} else {
				if (this.status.equals(CardStatus.HAND) && this.p.hand.cards.size() >= this.p.hand.maxsize) {
					eventlist.add(new EventMill(this.p, card));
				} else {
					ArrayList<Card> cards = this.p.board.getCollection(this.targetTeam, this.status); // YEA
					int temppos = this.pos.get(i) == -1 ? (int) cards.size() : this.pos.get(i);
					temppos = Math.min(temppos, cards.size());
					card.cardpos = temppos;
					cards.add(temppos, card);
					if (this.status.equals(CardStatus.HAND)) {
						this.p.board.getPlayer(this.targetTeam).hand.updatePositions();
					}
					if (this.status.equals(CardStatus.DECK)) {
						this.p.board.getPlayer(this.targetTeam).deck.updatePositions();
					}
				}
			}
			card.status = this.status;
		}
	}

	public String toString() {
		String ret = this.id + " " + p.team + " " + this.c.size() + " " + this.status.toString() + " " + this.targetTeam
				+ " ";
		for (int i = 0; i < this.c.size(); i++) {
			ret += this.c.get(i).toReference() + this.pos.get(i) + " ";
		}
		return ret + "\n";
	}

	public static EventPutCard fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		int size = Integer.parseInt(st.nextToken());
		String sStatus = st.nextToken();
		CardStatus csStatus = null;
		for (CardStatus cs : CardStatus.values()) {
			if (cs.toString().equals(sStatus)) {
				csStatus = cs;
			}
		}
		int targetteam = Integer.parseInt(st.nextToken());
		ArrayList<Card> c = new ArrayList<Card>();
		ArrayList<Integer> pos = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			Card card = Card.fromReference(b, st);
			c.add(card);
			int poss = Integer.parseInt(st.nextToken());
			pos.add(poss);
		}
		return new EventPutCard(p, c, csStatus, targetteam, pos);
	}

	public boolean conditions() {
		return true;
	}
}
