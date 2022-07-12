package client.states;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.*;

import client.Game;
import client.ui.*;
import client.ui.game.*;
import network.*;
import server.ai.AIConfig;
import server.card.cardset.*;
import utils.SelectRandom;

public class StateGame extends BasicGameState {
    public static ConstructedDeck tempdeck;
    public static AIConfig tempConfig;
    UI ui;
    UIBoard uiBoard;
    ServerGameThread game;
    DataStream dslocal, dsserver;
    GameContainer container;

    @Override
    public void init(GameContainer arg0, StateBasedGame arg1) {
        this.container = arg0;
    }

    @Override
    public void enter(GameContainer arg0, StateBasedGame arg1) {
        this.ui = new UI();
        arg0.getInput().addMouseListener(this.ui);
        arg0.getInput().addKeyListener(this.ui);
        this.dslocal = new DataStream();
        this.dsserver = new DataStream();
        DataStream.pair(dslocal, dsserver);
        this.uiBoard = new UIBoard(this.ui, this.dslocal, i -> System.out.println("team " + i + " won"), () -> {});
        this.ui.addUIElementParent(this.uiBoard);
        GenericButton quitButton = new GenericButton(this.ui, new Vector2f(0.5f, -0.5f), new Vector2f(150, 50), "Quit",
                () -> arg1.enterState(Game.STATE_MENU));
        quitButton.alignh = 1;
        quitButton.alignv = -1;
        quitButton.relpos = true;
        this.ui.addUIElementParent(quitButton);
        this.game = new ServerGameThread(this.dsserver, false);
        this.game.setDecklist(0, tempdeck);
        this.game.setDecklist(1, SelectRandom.from(ConstructedDeck.decks));
        this.game.setAIConfig(tempConfig);
        this.game.start();
    }

    @Override
    public void leave(GameContainer arg0, StateBasedGame arg1) {
        this.game.interrupt();
        this.uiBoard.exit();
        arg0.getInput().removeListener(this.ui);
    }

    @Override
    public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) {
        // TODO Auto-generated method stub
        this.ui.draw(arg2);
    }

    @Override
    public void update(GameContainer arg0, StateBasedGame arg1, int arg2) {
        // TODO Auto-generated method stub
        this.ui.update(arg2 / 1000.);
    }

    @Override
    public int getID() {
        // TODO Auto-generated method stub
        return Game.STATE_GAME;
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ESCAPE) {
            this.container.exit();
        }
    }
}
