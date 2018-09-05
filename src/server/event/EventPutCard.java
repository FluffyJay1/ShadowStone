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
	public Target t;
	public CardStatus status;
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
				c.removeAdditionalEffects();
				if (c instanceof Minion) {
					((Minion) c).health = c.finalStatEffects.getStat(EffectStats.HEALTH);
				}
				this.p.board.removeBoardObject((BoardObject) c);
				if (!loopprotection) {
					eventlist.add(new EventLeavePlay(c));
				}
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
					if (c instanceof Minion) {
						((Minion) c).summoningSickness = true;
					}
					if (!loopprotection) {
						eventlist.add(new EventEnterPlay(c));
					}
				}
			} else {
				if (this.status.equals(CardStatus.HAND) && this.p.hand.cards.size() >= this.p.hand.maxsize) {
					eventlist.add(new EventMill(this.p, c));
				} else {
					ArrayList<Card> cards = this.p.board.getCollection(this.targetTeam, this.status); // YEA
					int temppos = this.pos == -1 ? (int) (Math.random() * cards.size()) : this.pos;
					temppos = Math.min(temppos, cards.size());
					c.cardpos = temppos;
					cards.add(temppos, c);
					if (this.status.equals(CardStatus.HAND)) {
						this.p.board.getPlayer(this.targetTeam).hand.updatePositions();
					}
					if (this.status.equals(CardStatus.DECK)) {
						this.p.board.getPlayer(this.targetTeam).deck.updatePositions();
					}
				}
			}
			c.status = this.status;
		}
	}

	public String toString() {
		return this.id + " " + p.team + " " + this.t.toString() + " " + this.status.toString() + " " + this.targetTeam
				+ " " + this.pos + "\n";
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
		return true;
	}
}
