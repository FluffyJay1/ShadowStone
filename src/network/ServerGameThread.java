package network;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.card.cardpack.*;
import server.card.leader.*;
import server.card.unleashpower.*;
import server.event.*;
import server.playeraction.*;

/**
 * thread for p2p battles on same network, one computer acts as server, each
 * thread is a single game
 * 
 * @author Michael
 *
 */
public class ServerGameThread extends Thread {
	DataStreamServer dstream;
	Board b;
	VisualBoard localBoard;
	ConstructedDeck[] decks;

	// if playing vs ai
	public ServerGameThread(VisualBoard localBoard) {
		this.b = new Board();
		this.b.ai = true;
		this.localBoard = localBoard;
		this.b.localteam = localBoard.localteam;
		this.decks = new ConstructedDeck[2];
	}

	public ServerGameThread(DataStreamServer dstream, VisualBoard localBoard) {
		this.dstream = dstream;
		this.b = new Board();
		this.localBoard = localBoard;
		this.b.localteam = localBoard.localteam;
		this.decks = new ConstructedDeck[2];
	}

	@Override
	public void run() {
		// accept decklists
		while (this.decks[0] == null && this.decks[1] == null) {
			if (this.dstream.ready()) {
				MessageType mtype = this.dstream.receive();
				if (mtype == MessageType.DECK) {
					this.setDecklist(this.localBoard.localteam * -1, this.dstream.readDecklist());
				} else {
					this.dstream.discardMessage();
				}
			}
		}
		this.initializeGame();
		// TODO mulligan phase
		// game loop
		while (this.b.winner == 0) {
			while (!this.b.ai && this.dstream.ready()) {
				MessageType mtype = this.dstream.receive();
				switch (mtype) {
				case PLAYERACTION:
					if (this.b.currentplayerturn == this.localBoard.localteam * -1) {
						this.b.executePlayerAction(new StringTokenizer(this.dstream.readPlayerAction().toString()));
						this.sendEvents();
					}
					break;
				default:
					this.dstream.discardMessage();
					break;
				}
			}
			String action = this.localBoard.realBoard.retrievePlayerAction();
			while (action != null) {
				this.b.executePlayerAction(new StringTokenizer(action));
				this.sendEvents();
				action = this.localBoard.realBoard.retrievePlayerAction();
			}
		}
	}

	private void initializeGame() {
		for (int team = 1; team >= -1; team -= 2) { // deckbuilding 101
			ArrayList<Card> cards = new ArrayList<Card>();
			cards.addAll(this.decks[(team - 1) / -2].convertToCards(this.b));
			while (!cards.isEmpty()) {
				Card selected = Game.selectRandom(cards);
				this.b.eventlist.add(new EventCreateCard(this.b, selected, team, CardStatus.DECK, 0));
				cards.remove(selected);
			}
			UnleashPower up = (UnleashPower) Card.createFromConstructor(this.b,
					-8 - this.decks[(team - 1) / -2].craft.ordinal());
			this.b.eventlist.add(new EventCreateCard(this.b, up, team, CardStatus.UNLEASHPOWER, 0));
			// TODO change leader
			this.b.eventlist.add(new EventCreateCard(this.b, new Rowen(this.b), team, CardStatus.LEADER, 0));
		}
		this.b.eventlist.add(new EventDraw(this.b.player1, 3));
		this.b.eventlist.add(new EventDraw(this.b.player2, 3));
		this.b.resolveAll();
		this.b.eventlist.add(new EventTurnStart(this.b.getPlayer(1)));
		this.b.resolveAll();
		if (this.b.ai && this.localBoard.localteam != this.b.currentplayerturn) {
			this.b.AIThink();
		}
		this.sendEvents();
	}

	public void sendEvents() {
		String eventstring = this.b.retrieveEventString();
		this.sendEvent(1, eventstring);
		this.sendEvent(-1, eventstring);
	}

	public void sendEvent(int team, String eventstring) {
		if (team == this.localBoard.localteam) {
			this.localBoard.parseEventString(eventstring);
		} else if (!this.b.ai) {
			this.dstream.sendEvent(eventstring);
		}
	}

	public void sendPlayerAction(int team, PlayerAction action) {
		if (team == this.localBoard.localteam) {
			// display something idk
		} else if (!this.b.ai) {
			this.dstream.sendPlayerAction(action);
		}
	}

	public void sendDecklist(int team, ConstructedDeck deck) {
		if (team == this.localBoard.localteam) {
			// display something idk
		} else if (!this.b.ai) {
			this.dstream.sendDecklist(deck);
		}
	}

	public void setDecklist(int team, ConstructedDeck deck) {
		this.decks[(team - 1) / -2] = deck;
	}
}
