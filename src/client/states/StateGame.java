package client.states;

import org.newdawn.slick.*;
import org.newdawn.slick.state.*;

import client.*;
import client.Game;
import network.*;
import server.card.cardpack.*;

public class StateGame extends BasicGameState {
	public static ConstructedDeck tempdeck;
	VisualBoard board;
	ServerGameThread game;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		board = new VisualBoard(1);
		arg0.getInput().addMouseListener(board);
		arg0.getInput().addKeyListener(board.ui);
		game = new ServerGameThread(board);
		game.setDecklist(1, tempdeck);
		game.setDecklist(-1, Game.selectRandom(ConstructedDeck.decks));
		game.start();
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
