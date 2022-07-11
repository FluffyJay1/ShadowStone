package client.states;

import client.Game;
import client.ui.GenericButton;
import client.ui.Text;
import client.ui.UI;
import client.ui.game.UIBoard;
import client.ui.menu.DeckSelectPanel;
import client.ui.menu.pvp.PVPMenu;
import network.DataStream;
import network.ServerGameThread;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.IOException;
import java.util.StringTokenizer;

public class StatePVP extends BasicGameState {
    private static final double ERROR_DISPLAY_TIME = 5;
    public static ServerGameThread serverGameThread;
    UI ui;
    UIBoard uiBoard;
    GameContainer container;
    StateBasedGame game;
    PVPMenu menu;
    DataStream ds;
    DeckSelectPanel deckSelectPanel;
    Text waitingText;
    Text errorText;
    double errorTimer;

    @Override
    public int getID() {
        return Game.STATE_PVP;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        this.container = container;
        this.game = game;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        this.ui = new UI();
        container.getInput().addListener(this.ui);

        this.ui.addListener(((strarg, intarg) -> {
            StringTokenizer st = new StringTokenizer(strarg);
            switch (st.nextToken()) {
                case DeckSelectPanel.DECK_CONFIRM -> {
                    try {
                        this.ds.sendDecklist(this.deckSelectPanel.selectedDeckUnit.deck);
                        this.waitingText.setVisible(true);
                        this.deckSelectPanel.setVisible(false);
                    } catch (IOException e) {
                        this.uiBoard.connectionClosed = true;
                        this.uiBoard.onConnectionClosed.run();
                    }
                }
                case DeckSelectPanel.DECK_CANCEL -> game.enterState(Game.STATE_MENU);
            }
        }));

        this.menu = new PVPMenu(ui, this::onConnect);
        this.ui.addUIElementParent(this.menu);
        GenericButton quitButton = new GenericButton(this.ui, new Vector2f(0.5f, -0.5f), new Vector2f(150, 50), "Quit",
                () -> game.enterState(Game.STATE_MENU));
        quitButton.alignh = 1;
        quitButton.alignv = -1;
        quitButton.relpos = true;
        quitButton.setZ(1);
        this.ui.addUIElementParent(quitButton);
        this.deckSelectPanel = new DeckSelectPanel(ui, new Vector2f(), false);
        this.deckSelectPanel.setVisible(false);
        this.deckSelectPanel.setZ(1);
        this.ui.addUIElementParent(this.deckSelectPanel);

        this.waitingText = new Text(this.ui, new Vector2f(), "Waiting for them to select their deck...", 1000, 50, 40, 0, 0);
        this.waitingText.setVisible(false);
        this.waitingText.setZ(2);
        this.ui.addUIElementParent(this.waitingText);

        this.errorText = new Text(this.ui, new Vector2f(0, 0.4f), "Error", 1000, 50, 40, 0, 0);
        this.errorText.relpos = true;
        this.errorText.setVisible(false);
        this.errorText.setZ(2);
        this.ui.addUIElementParent(this.errorText);

        this.errorTimer = 0;
    }

    private void onConnect(DataStream ds) {
        this.uiBoard = new UIBoard(this.ui, ds, i -> this.game.enterState(Game.STATE_MENU), () -> this.showError("Connection closed."));
        this.ui.addUIElementParent(this.uiBoard);
        this.ds = ds;
        this.deckSelectPanel.setVisible(true);
    }

    private void showError(String message) {
        this.errorText.setText(message);
        this.errorTimer = ERROR_DISPLAY_TIME;
    }

    @Override
    public void leave(GameContainer container, StateBasedGame game) throws SlickException {
        if (this.uiBoard != null) {
            this.uiBoard.musicThemeController.stop();
        }
        container.getInput().removeListener(this.ui);
        this.menu.shutdownHostServer();
        if (serverGameThread != null) { // just in case
            serverGameThread.interrupt(); // this is graceful im sure
            serverGameThread = null;
        }
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        this.ui.draw(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        double frametime = delta / 1000.;
        this.ui.update(frametime);
        if (this.errorTimer > 0) {
            this.errorTimer -= frametime;
        }
        this.errorText.setVisible(this.errorTimer > 0);
        if (this.uiBoard != null && this.uiBoard.b.getLocalteam() != 0) {
            this.waitingText.setVisible(false);
        }
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ESCAPE) {
            this.container.exit();
        }
    }
}
