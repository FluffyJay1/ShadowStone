package client.states;

import java.util.ArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import cardpack.basic.*;
import client.Game;
import client.VisualBoard;
import server.Board;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;
import server.card.Minion;
import server.card.leader.Rowen;
import server.event.Event;
import server.event.EventCreateCard;
import server.event.EventDraw;
import server.event.EventMinionAttack;

public class StateGame extends BasicGameState {
	VisualBoard board;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub
		board = new VisualBoard();
		arg0.getInput().addMouseListener(board);
		for (int team = 1; team >= -1; team -= 2) { // deckbuilding 101
			ArrayList<Card> cards = new ArrayList<Card>();
			for (int i = 0; i < 5; i++) {
				cards.add(new Goblin(board.realBoard, team));
				cards.add(new Tiny(board.realBoard, team));
				cards.add(new Fireball(board.realBoard, team));
				cards.add(new Fighter(board.realBoard, team));
				cards.add(new WellOfDestination(board.realBoard, team));
				cards.add(new BellringerAngel(board.realBoard, team));
				cards.add(new GenesisOfLegend(board.realBoard, team));
				cards.add(new WoodOfBrambles(board.realBoard, team));
			}
			while (!cards.isEmpty()) {
				Card selected = Game.selectRandom(cards);
				board.realBoard.eventlist.add(new EventCreateCard(board.realBoard, selected, team, CardStatus.DECK, 0));
				cards.remove(selected);
			}
			board.resolveAll();
		}
		board.realBoard.eventlist
				.add(new EventCreateCard(board.realBoard, new Rowen(board.realBoard, 1), 1, CardStatus.BOARD, 0));
		board.realBoard.eventlist
				.add(new EventCreateCard(board.realBoard, new Rowen(board.realBoard, -1), -1, CardStatus.BOARD, 0));
		board.realBoard.eventlist.add(new EventDraw(board.realBoard.player1, 3));
		board.realBoard.eventlist.add(new EventDraw(board.realBoard.player2, 3));
		board.resolveAll();
		for (int i = 0; i < 3; i++) {
			// board.addBoardObjectToSide(new Tiny(board, 1), 1);

			board.realBoard.eventlist.add(new EventCreateCard(board.realBoard, new BellringerAngel(board.realBoard, 1),
					-1, CardStatus.BOARD, 1));
		}
		board.realBoard.resolveAll();
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
