package server.event;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.card.unleashpower.*;

public class EventCreateCard extends Event {
	public static final int ID = 2;
	Card c;
	Board b;
	CardStatus status;
	int team, cardpos;
	private UnleashPower prevUP;
	private Leader prevLeader;

	String before, after;

	public EventCreateCard(Card c, int team, CardStatus status, int cardpos) {
		super(ID, false);
		this.c = c;
		this.b = c.board;
		this.team = team;
		this.status = status;
		this.cardpos = cardpos;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.before = this.b.stateToString();
		this.c.team = this.team;
		this.c.status = this.status;
		switch (this.status) {
		case HAND:
			Hand relevantHand = this.b.getPlayer(this.team).hand;
			if (relevantHand.cards.size() >= relevantHand.maxsize) {
				eventlist.add(0, new EventDestroy(c));
			}
			int temppos = this.cardpos == -1 ? (int) relevantHand.cards.size() : this.cardpos;
			temppos = Math.min(this.cardpos, relevantHand.cards.size());
			relevantHand.cards.add(temppos, this.c);
			relevantHand.updatePositions();
			break;
		case BOARD:
			if (this.c instanceof BoardObject) {
				this.b.addBoardObject((BoardObject) this.c, this.team,
						this.cardpos == -1 ? this.c.board.getBoardObjects(this.team).size() : this.cardpos);
				if (c instanceof Minion) {
					((Minion) c).summoningSickness = true;
				}
				if (!loopprotection) {
					eventlist.add(new EventEnterPlay(c));
				}
			}
			break;
		case DECK:
			Deck relevantDeck = this.b.getPlayer(this.team).deck;
			temppos = this.cardpos == -1 ? (int) relevantDeck.cards.size() : this.cardpos;
			temppos = Math.min(this.cardpos, relevantDeck.cards.size());
			relevantDeck.cards.add(temppos, this.c);
			relevantDeck.updatePositions();
			break;
		case UNLEASHPOWER:
			this.prevUP = this.b.getPlayer(this.team).unleashPower;
			this.b.getPlayer(this.team).unleashPower = (UnleashPower) this.c;
			((UnleashPower) this.c).p = this.b.getPlayer(this.team);
			break;
		case LEADER:
			this.prevLeader = this.b.getPlayer(this.team).leader;
			this.b.getPlayer(this.team).leader = (Leader) this.c;
		default:
			break;
		}
		if (this.b.isClient) {
			this.b.cardsCreated.add(this.c);
		}
		this.after = this.b.stateToString();
	}

	@Override
	public void undo() {
		CardStatus status = this.c.status;
		switch (status) {
		case HAND:
			Hand relevantHand = this.b.getPlayer(this.team).hand;
			if (relevantHand.cards.size() >= relevantHand.maxsize) {
				// just do nothing, shouldn't try to unmill
			}
			relevantHand.cards.remove(this.c);
			relevantHand.updatePositions();
			break;
		case BOARD:
			if (this.c instanceof BoardObject) {
				this.b.removeBoardObject((BoardObject) this.c);
			}
			break;
		case DECK:
			Deck relevantDeck = this.b.getPlayer(this.team).deck;
			relevantDeck.cards.remove(this.c);
			relevantDeck.updatePositions();
			break;
		case UNLEASHPOWER:
			this.b.getPlayer(this.team).unleashPower = this.prevUP;
			break;
		case LEADER:
			this.b.getPlayer(this.team).leader = this.prevLeader;
		default:
			break;
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.c.toConstructorString() + this.team + " " + this.status.toString() + " "
				+ this.cardpos + "\n";
	}

	public static EventCreateCard fromString(Board b, StringTokenizer st) {
		Card c = Card.createFromConstructorString(b, st);
		if (b instanceof VisualBoard) {
			c.realCard = ((VisualBoard) b).realBoard.cardsCreated.remove(0);
			((VisualBoard) b).uiBoard.addCard(c);
		}
		int team = Integer.parseInt(st.nextToken());
		String sStatus = st.nextToken();
		CardStatus csStatus = null;
		for (CardStatus cs : CardStatus.values()) {
			if (cs.toString().equals(sStatus)) {
				csStatus = cs;
			}
		}
		int cardpos = Integer.parseInt(st.nextToken());
		return new EventCreateCard(c, team, csStatus, cardpos);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
