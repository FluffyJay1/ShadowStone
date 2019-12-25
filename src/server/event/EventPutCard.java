package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventPutCard extends Event {
	// for effects that put specific cards in hand or just draw cards
	public static final int ID = 12;
	Player p;
	public List<Card> c;
	public List<Integer> pos; // pos == -1 means last
	public CardStatus status;
	int targetTeam;
	private List<CardStatus> prevStatus;
	private List<List<Effect>> prevEffects;
	private List<Integer> prevPos;
	private List<Integer> prevTeam;
	private List<Integer> prevHealth;
	private List<Integer> prevAttacks;
	private List<Boolean> prevSick;

	public EventPutCard(Player p, List<Card> c, CardStatus status, int team, List<Integer> pos) {
		super(ID, false);
		this.p = p;
		this.c = new ArrayList<Card>();
		this.c.addAll(c);
		this.status = status;
		this.targetTeam = team;
		this.pos = new ArrayList<Integer>();
		this.pos.addAll(pos);
	}

	public EventPutCard(Player p, Card c, CardStatus status, int team, int pos) {
		super(ID, false);
		this.p = p;
		this.c = new ArrayList<Card>();
		this.c.add(c);
		this.status = status;
		this.targetTeam = team;
		this.pos = new ArrayList<Integer>();
		this.pos.add(pos);
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.prevStatus = new LinkedList<CardStatus>();
		this.prevEffects = new LinkedList<List<Effect>>();
		this.prevPos = new LinkedList<Integer>();
		this.prevTeam = new LinkedList<Integer>();
		this.prevHealth = new LinkedList<Integer>();
		this.prevAttacks = new LinkedList<Integer>();
		this.prevSick = new LinkedList<Boolean>();
		for (int i = 0; i < this.c.size(); i++) {
			Card card = this.c.get(i);
			this.prevStatus.add(card.status);
			this.prevEffects.add(new LinkedList<Effect>());
			this.prevPos.add(card.cardpos);
			this.prevTeam.add(card.team);
			this.prevHealth.add(0);
			this.prevAttacks.add(0);
			if (card instanceof Minion) {
				this.prevHealth.set(i, ((Minion) card).health);
				this.prevAttacks.set(i, ((Minion) card).attacksThisTurn);
			}
			this.prevSick.add(true);
			switch (card.status) { // removing from
			case HAND:
				this.p.hand.cards.remove(card);
				this.p.hand.updatePositions();
				break;
			case BOARD:
				this.p.board.removeBoardObject((BoardObject) card);
				if (!loopprotection) {
					eventlist.add(new EventLeavePlay(card));
				}
				break;
			case DECK:
				this.p.deck.cards.remove(card);
				this.p.deck.updatePositions();
				break;
			case LEADER:
				// wait
				break;
			default:
				break;
			}
			// goes against flow
			if (card.status.ordinal() < this.status.ordinal()) {
				this.prevEffects.set(i, card.removeAdditionalEffects());
				if (card instanceof Minion) {
					((Minion) card).health = card.finalStatEffects.getStat(EffectStats.HEALTH);
					((Minion) card).attacksThisTurn = 0;
				}
			}
			card.team = this.targetTeam;
			if (this.status.equals(CardStatus.BOARD)) { // now adding to
				if (card instanceof BoardObject) {
					this.p.board.addBoardObject((BoardObject) card, this.targetTeam,
							this.pos.get(i) == -1 ? this.p.board.getBoardObjects(this.p.team).size() : this.pos.get(i));
					if (card instanceof Minion) {
						this.prevSick.set(i, ((Minion) card).summoningSickness);
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
					List<Card> cards = this.p.board.getCollection(this.targetTeam, this.status); // YEA
					int temppos = this.pos.get(i) == -1 ? (int) cards.size() : this.pos.get(i);
					temppos = Math.min(temppos, cards.size());
					card.cardpos = temppos;
					cards.add(temppos, card);
					if (this.status.equals(CardStatus.HAND)) {
						this.p.board.getPlayer(this.targetTeam).hand.updatePositions();
					} else if (this.status.equals(CardStatus.DECK)) {
						this.p.board.getPlayer(this.targetTeam).deck.updatePositions();
					}
				}
			}
			card.status = this.status;
		}
	}

	@Override
	public void undo() {
		for (int i = 0; i < this.c.size(); i++) {
			Card card = this.c.get(i);
			switch (card.status) { // removing from
			case HAND:
				this.p.hand.cards.remove(card);
				this.p.hand.updatePositions();
				break;
			case BOARD:
				this.p.board.removeBoardObject((BoardObject) card);
				break;
			case DECK:
				this.p.deck.cards.remove(card);
				this.p.deck.updatePositions();
				break;
			case LEADER:
				// wait
				break;
			default:
				break;
			}
			for (Effect e : this.prevEffects.get(i)) {
				card.addEffect(false, e);
			}
			// goes against flow
			if (card instanceof Minion) {
				((Minion) card).health = this.prevHealth.get(i);
				((Minion) card).attacksThisTurn = this.prevAttacks.get(i);
				((Minion) card).summoningSickness = this.prevSick.get(i);
			}
			card.team = this.prevTeam.get(i);
			card.status = this.prevStatus.get(i);
			switch (this.prevStatus.get(i)) { // adding to
			case HAND:
				this.p.hand.cards.add(this.prevPos.get(i), card);
				this.p.hand.updatePositions();
				break;
			case BOARD:
				this.p.board.addBoardObject((BoardObject) card, card.team, this.prevPos.get(i));
				break;
			case DECK:
				this.p.deck.cards.add(this.prevPos.get(i), card);
				this.p.deck.updatePositions();
				break;
			case LEADER:
				// wait
				break;
			default:
				break;
			}
		}
	}

	@Override
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

	@Override
	public boolean conditions() {
		return true;
	}
}
