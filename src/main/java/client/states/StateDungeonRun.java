package client.states;

import client.Config;
import client.Game;
import client.ui.UI;
import client.ui.dungeonrun.DungeonRunLootPanel;
import client.ui.dungeonrun.DungeonRunMenu;
import client.ui.dungeonrun.DungeonRunStatusPanel;
import client.ui.game.CardSelectTooltipPanel;
import client.ui.game.UIBoard;
import client.ui.menu.CardDisplayUnit;
import client.ui.menu.ClassSelectPanel;
import client.ui.menu.DeckDisplayPanel;
import gamemode.dungeonrun.controller.DungeonRunController;
import network.DataStream;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import server.card.CardText;
import server.card.ClassCraft;

import java.util.StringTokenizer;

public class StateDungeonRun extends BasicGameState {
    UI ui;
    GameContainer container;
    DungeonRunMenu dmu;
    DungeonRunStatusPanel drsp;
    DungeonRunLootPanel drlp;
    ClassSelectPanel classSelectPanel;
    DeckDisplayPanel deckDisplayPanel;
    CardSelectTooltipPanel cardTooltip;
    UIBoard uib;
    Thread gameThread;

    @Override
    public void init(GameContainer arg0, StateBasedGame arg1) {
        this.container = arg0;
    }

    @Override
    public void enter(GameContainer arg0, StateBasedGame arg1) {
        this.ui = new UI();
        arg0.getInput().addListener(this.ui);
        this.ui.addListener((strarg, intarg) -> {
            StringTokenizer st = new StringTokenizer(strarg);
            switch (st.nextToken()) {
                case ClassSelectPanel.SELECT -> {
                    if (DungeonRunController.run == null) {
                        ClassCraft craft = ClassCraft.values()[intarg[0]];
                        DungeonRunController.generateRun(craft);
                        this.onUpdateRunStatus();
                    }
                }
                case DeckDisplayPanel.CARD_CLICK, CardDisplayUnit.CARD_CLICK -> {
                    if (intarg[0] == Input.MOUSE_LEFT_BUTTON) {
                        CardText cardText = CardText.fromString(st.nextToken());
                        assert cardText != null;
                        this.cardTooltip.setTooltip(cardText.getTooltip());
                    }
                }
                case DeckDisplayPanel.DECK_CONFIRM -> {
                    this.deckDisplayPanel.setVisible(false);
                }
            }
        });
        this.ui.setOnPress(element -> {
            if (element == null || (element != this.cardTooltip && !element.isChildOf(this.cardTooltip) && !(element instanceof CardDisplayUnit))) {
                this.cardTooltip.setTooltip(null);
            }
        });
        this.dmu = new DungeonRunMenu(this.ui, new Vector2f(-0.5f, 0),
                this::startGame, // start game
                () -> {
                    // view deck
                    this.deckDisplayPanel.setDeck(DungeonRunController.run.player.deck);
                    this.deckDisplayPanel.setVisible(true);
                },
                () -> {
                    // end run
                    DungeonRunController.endRun();
                    this.onUpdateRunStatus();
                },
                () -> arg1.enterState(Game.STATE_MENU)); // return to menu
        this.dmu.relpos = true;
        this.dmu.alignh = -1;
        this.ui.addUIElementParent(this.dmu);
        this.drsp = new DungeonRunStatusPanel(this.ui, new Vector2f((float) this.dmu.getRight(false, false), 0));
        this.drsp.alignh = -1;
        this.ui.addUIElementParent(this.drsp);
        this.drlp = new DungeonRunLootPanel(this.ui, new Vector2f(150, Config.WINDOW_HEIGHT * -0.5f), this::onUpdateRunStatus);
        this.drlp.alignv = -1;
        this.ui.addUIElementParent(this.drlp);
        this.classSelectPanel = new ClassSelectPanel(this.ui, new Vector2f(), false);
        this.ui.addUIElementParent(this.classSelectPanel);
        this.deckDisplayPanel = new DeckDisplayPanel(this.ui, new Vector2f(150, Config.WINDOW_HEIGHT * 0.25f), false);
        this.deckDisplayPanel.setVisible(false);
        this.ui.addUIElementParent(this.deckDisplayPanel);
        this.cardTooltip = new CardSelectTooltipPanel(this.ui, new Vector2f(-0.45f, -0.45f), 3);
        this.cardTooltip.relpos = true;
        this.cardTooltip.alignh = -1;
        this.cardTooltip.alignv = -1;
        this.cardTooltip.setVisible(false);
        this.ui.addUIElementParent(this.cardTooltip);
        this.onUpdateRunStatus();
    }

    private void onUpdateRunStatus() {
        this.dmu.onUpdateRunStatus();
        this.classSelectPanel.setVisible(DungeonRunController.run == null);
        if (DungeonRunController.run == null) {
            this.deckDisplayPanel.setVisible(false);
        }
        this.drsp.onUpdateRunStatus();
        this.drlp.onUpdateRunStatus();
    }

    private void startGame() {
        DataStream dslocal = new DataStream();
        DataStream dsserver = new DataStream();
        DataStream.pair(dslocal, dsserver);
        this.uib = new UIBoard(this.ui, 1, dslocal, this::endGame);
        this.ui.addUIElementParent(this.uib);
        this.gameThread = DungeonRunController.startGame(dsserver, 1);
        this.onUpdateRunStatus();
    }

    private void endGame(int team) {
        this.gameThread.interrupt();
        this.ui.removeUIElementParent(this.uib);
        DungeonRunController.endGame(team == this.uib.b.localteam);
        this.onUpdateRunStatus();
    }

    @Override
    public void leave(GameContainer arg0, StateBasedGame arg1) {
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
        return Game.STATE_DUNGEONRUN;
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ESCAPE) {
            this.container.exit();
        }
    }
}
