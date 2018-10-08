package client.states;

import java.util.ArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import client.Game;
import client.VisualBoard;
import server.Board;
import server.card.Card;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Leader;
import server.card.Minion;
import server.card.cardpack.CardSet;
import server.card.cardpack.ConstructedDeck;
import server.card.cardpack.basic.*;
import server.card.leader.Rowen;
import server.card.unleashpower.*;
import server.event.*;

public class StateGame extends BasicGameState {
	public static ConstructedDeck tempdeck;
	VisualBoard board;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		board = new VisualBoard();
		arg0.getInput().addMouseListener(board);
		arg0.getInput().addKeyListener(board.ui);
		for (int team = 1; team >= -1; team -= 2) { // deckbuilding 101
			ArrayList<Card> cards = new ArrayList<Card>();
			if (team == board.localteam && tempdeck != null) {
				cards.addAll(tempdeck.convertToCards(board.realBoard));
			} else {
				for (int i = 0; i < 5; i++) {
					cards.add(new Goblin(board.realBoard));
					cards.add(new Tiny(board.realBoard));
					cards.add(new Fireball(board.realBoard));
					cards.add(new Fighter(board.realBoard));
					cards.add(new WellOfDestination(board.realBoard));
					cards.add(new BellringerAngel(board.realBoard));
					cards.add(new GenesisOfLegend(board.realBoard));
					cards.add(new WoodOfBrambles(board.realBoard));
				}
			}
			while (!cards.isEmpty()) {
				Card selected = Game.selectRandom(cards);
				board.realBoard.eventlist.add(new EventCreateCard(board.realBoard, selected, team, CardStatus.DECK, 0));
				cards.remove(selected);
			}

		}
		UnleashPower up = (UnleashPower) Card.createFromConstructor(board.realBoard, -8 - tempdeck.craft.ordinal());
		board.realBoard.eventlist.add(new EventCreateCard(board.realBoard, up, 1, CardStatus.UNLEASHPOWER, 0));
		board.realBoard.eventlist.add(new EventCreateCard(board.realBoard, new UnleashImbueMagic(board.realBoard), -1,
				CardStatus.UNLEASHPOWER, 0));
		board.realBoard.eventlist
				.add(new EventCreateCard(board.realBoard, new Rowen(board.realBoard), 1, CardStatus.BOARD, 0));
		board.realBoard.eventlist
				.add(new EventCreateCard(board.realBoard, new Rowen(board.realBoard), -1, CardStatus.BOARD, 0));
		board.realBoard.eventlist.add(new EventDraw(board.realBoard.player1, 3));
		board.realBoard.eventlist.add(new EventDraw(board.realBoard.player2, 3));
		for (int i = 0; i < 3; i++) {
			// board.addBoardObjectToSide(new Tiny(board, 1), 1);
			board.realBoard.eventlist.add(new EventCreateCard(board.realBoard, new BellringerAngel(board.realBoard), -1,
					CardStatus.BOARD, 1));
		}
		board.realBoard.resolveAll();
		board.realBoard.eventlist.add(new EventTurnStart(board.realBoard.getPlayer(1)));
		board.realBoard.resolveAll();
		if (board.realBoard.localteam != board.realBoard.currentplayerturn) {
			board.realBoard.AIThink();
		}
	}

	@Override
	public void leave(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		arg0.getInput().removeMouseListener(board);
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) throws SlickException {
		// TODO Auto-generated method stub
		board.draw(arg2);
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		// TODO Auto-generated method stub
		board.update(arg2 / 1000.);
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return Game.STATE_GAME;
	}

}
