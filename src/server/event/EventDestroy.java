package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventDestroy extends Event {
	// killing things
	public static final int ID = 4;
	public List<Card> c;
	private List<Boolean> alive;
	private List<CardStatus> prevStatus;
	private List<Integer> prevPos;

	public EventDestroy(Target t) {
		super(ID, false);
		this.c = new ArrayList<Card>();
		this.c.addAll(t.getTargets());
	}

	public EventDestroy(List<Card> c) {
		super(ID, false);
		this.c = new ArrayList<Card>();
		this.c.addAll(c);
	}

	public EventDestroy(Card c) {
		super(ID, false);
		this.c = new ArrayList<Card>();
		this.c.add(c);
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.alive = new ArrayList<Boolean>();
		this.prevStatus = new ArrayList<CardStatus>();
		this.prevPos = new ArrayList<Integer>();
		for (Card c : this.c) {
			this.alive.add(c.alive);
			this.prevStatus.add(c.status);
			this.prevPos.add(c.cardpos);
			if (c.alive) {
				// TODO increase shadows by 1
				c.alive = false;
				switch (c.status) {
				case HAND:
					c.board.getPlayer(c.team).hand.cards.remove(c);
					c.board.getPlayer(c.team).hand.updatePositions();
					break;
				case BOARD:
					if (c instanceof BoardObject) {
						BoardObject b = (BoardObject) c;
						b.board.removeBoardObject(b.team, b.cardpos);
						if (!loopprotection) {
							eventlist.add(new EventLeavePlay(c));
							eventlist.addAll(b.lastWords());
						}
					}
					break;
				case DECK:
					c.board.getPlayer(c.team).deck.cards.remove(c);
					c.board.getPlayer(c.team).deck.updatePositions();
					break;
				case LEADER:
					eventlist.add(new EventGameEnd(c.board, c.team * -1));
					break;
				default:
					break;
				}
				c.cardpos = c.board.getGraveyard(c.team).size(); // just in case
				c.status = CardStatus.GRAVEYARD;
				c.board.getGraveyard(c.team).add(c);

			}
		}
	}

	@Override
	public void undo() {
		for (int i = 0; i < this.c.size(); i++) {
			Card c = this.c.get(i);
			c.alive = this.alive.get(i);
			if (c.alive) {
				CardStatus status = this.prevStatus.get(i);
				int pos = this.prevPos.get(i);
				c.board.getGraveyard(c.team).remove(c);
				switch (status) {
				case HAND:
					c.board.getPlayer(c.team).hand.cards.add(pos, c);
					c.board.getPlayer(c.team).hand.updatePositions();
					break;
				case BOARD:
					if (c instanceof BoardObject) {
						BoardObject b = (BoardObject) c;
						b.board.addBoardObject(b, b.team, pos);
					}
					break;
				case DECK:
					c.board.getPlayer(c.team).deck.cards.add(pos, c);
					c.board.getPlayer(c.team).deck.updatePositions();
					break;
				default: // undestroy leader lmao
					break;
				}
				c.cardpos = pos; // just in case
				c.status = status;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.id + " " + this.c.size() + " ");
		for (int i = 0; i < this.c.size(); i++) {
			builder.append(this.c.get(i).toReference());
		}
		return builder.append("\n").toString();
	}

	public static EventDestroy fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Card> c = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++) {
			Card card = Card.fromReference(b, st);
			c.add(card);
		}
		return new EventDestroy(c);
	}

	@Override
	public boolean conditions() {
		return !this.c.isEmpty();
	}
}
