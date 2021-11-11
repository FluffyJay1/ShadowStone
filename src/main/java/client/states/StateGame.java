package client.states;

import org.newdawn.slick.*;
import org.newdawn.slick.state.*;

import client.Game;
import client.ui.*;
import client.ui.game.*;
import network.*;
import server.card.cardpack.*;

public class StateGame extends BasicGameState {
    public static ConstructedDeck tempdeck;
    UI ui;
    UIBoard uiBoard;
    ServerGameThread game;
    DataStream dslocal, dsserver;

    @Override
    public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
        // TODO Auto-generated method stub
    }

    @Override
    public void enter(GameContainer arg0, StateBasedGame arg1) throws SlickException {
        this.ui = new UI();
        arg0.getInput().addMouseListener(this.ui);
        arg0.getInput().addKeyListener(this.ui);
        this.dslocal = new DataStream();
        this.dsserver = new DataStream();
        DataStream.pair(dslocal, dsserver);
        this.uiBoard = new UIBoard(this.ui, 1, this.dslocal);
        this.ui.addUIElementParent(this.uiBoard);
        game = new ServerGameThread(this.dsserver, false, this.uiBoard.b);
        game.setDecklist(1, tempdeck);
        game.setDecklist(-1, Game.selectRandom(ConstructedDeck.decks));
        game.start();
    }

    @Override
    public void leave(GameContainer arg0, StateBasedGame arg1) throws SlickException {
        arg0.getInput().removeListener(this.ui);
    }

    @Override
    public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) throws SlickException {
        // TODO Auto-generated method stub
        this.ui.draw(arg2);
    }

    @Override
    public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
        // TODO Auto-generated method stub
        this.ui.update(arg2 / 1000.);
    }

    @Override
    public int getID() {
        // TODO Auto-generated method stub
        return Game.STATE_GAME;
    }

}
