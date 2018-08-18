package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Target;

public class EventPutCard extends Event {
	// for effects that put specific cards in hand or just draw cards
	public static final int ID = 12;
	Player p;
	Target t;
	CardStatus status;
	int targetTeam, pos; // pos == -1 means random pos

	public EventPutCard(Player p, Target t, CardStatus status, int team, int pos) {
		super(ID);
		this.p = p;
		this.t = t;
		this.status = status;
		this.targetTeam = team;
		this.pos = pos;
	}

	public EventPutCard(Player p, Card c, CardStatus status, int team, int pos) {
		super(ID);
		this.p = p;
		this.t = new Target(c);
		this.status = status;
		this.targetTeam = team;
		this.pos = pos;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (Card c : t.getTargets()) {
			switch (c.status) { // removing from
			case HAND:
				this.p.hand.cards.remove(c);
				this.p.hand.updatePositions();
				break;
			case BOARD:
				this.p.board.removeBoardObject((BoardObject) c);
				break;
			case DECK:
				this.p.deck.cards.remove(c);
				this.p.deck.updatePositions();
				break;
			default:
				break;
			}

			if (this.status.equals(CardStatus.BOARD)) { // now adding to
				if (c instanceof BoardObject) {
					this.p.board.addBoardObject((BoardObject) c, this.targetTeam,
							this.pos == -1 ? this.p.board.getBoardObjects(this.p.team).size() : this.pos);
				}
			} else {

				ArrayList<Card> cards = this.p.board.getCollection(this.p.team, this.status); // YEA
				int temppos = this.pos == -1 ? (int) (Math.random() * cards.size()) : this.pos;
				temppos = Math.min(temppos, cards.size());
				c.cardpos = temppos;
				cards.add(temppos, c);
			}
			c.status = this.status;
		}
	}

	public String toString() {
		return this.id + " " + p.team + " " + this.t.toString() + " " + this.status.toString() + " " + this.targetTeam
				+ " " + this.pos;
	}

	public static EventPutCard fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		Target t = Target.fromString(b, st);
		String sStatus = st.nextToken();
		CardStatus csStatus = null;
		for (CardStatus cs : CardStatus.values()) {
			if (cs.toString().equals(sStatus)) {
				csStatus = cs;
			}
		}
		int targetteam = Integer.parseInt(st.nextToken());
		int pos = Integer.parseInt(st.nextToken());
		return new EventPutCard(p, t, csStatus, targetteam, pos);
	}

	public boolean conditions() {
		return p.hand.cards.size() < p.hand.maxsize;
	}
}
