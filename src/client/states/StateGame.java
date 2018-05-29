package client.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import cardpack.basic.*;
import client.Game;
import client.VisualBoard;
import server.Board;
import server.card.Minion;
import server.event.Event;
import server.event.EventMinionAttack;

public class StateGame extends BasicGameState {
	VisualBoard board;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub
		board = new VisualBoard();
		arg0.getInput().addMouseListener(board);
		/*
		 * Minion p1 = new Goblin(board); Minion p2 = new Goblin(board);
		 * board.addBoardObject(p1, 2); board.addBoardObject(p2, -2);
		 */
		for (int i = 0; i < 5; i++) {
			// board.addBoardObjectToSide(new Tiny(board, 1), 1);
			board.addBoardObjectToSide(new Tiny(board, 1), -1);
		}
		System.out.println(board.stateToString());
		/*
		 * Event e = new EventMinionAttack(p1, p2); board.eventlist.add(e);
		 * board.resolveAll(); board.eventlist.add(e); board.resolveAll();
		 */
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
