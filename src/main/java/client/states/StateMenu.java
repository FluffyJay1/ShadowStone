package client.states;

import java.util.*;

import client.Config;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.*;

import client.Game;
import client.ui.*;
import client.ui.menu.*;
import server.card.cardset.ConstructedDeck;

public class StateMenu extends BasicGameState {
    private static final double ERROR_DISPLAY_TIME = 5;
    UI ui;
    PlayButton playButton;
    AIDifficultyPanel aiDifficultyPanel;
    GameContainer container;
    StateBasedGame game;
    Text errorText;
    double errorTimer;

    @Override
    public void init(GameContainer arg0, StateBasedGame arg1) {
        this.container = arg0;
        this.game = arg1;
    }

    @Override
    public void enter(GameContainer arg0, StateBasedGame arg1) {
        this.ui = new UI();
        arg0.getInput().addListener(this.ui);

        this.ui.addListener((strarg, intarg) -> {
            StringTokenizer st = new StringTokenizer(strarg);
            switch (st.nextToken()) {
                case DeckSelectPanel.DECK_CONFIRM:
                    if (playButton.deckspanel.selectedDeckUnit != null) {
                        StateGame.tempdeck = playButton.deckspanel.selectedDeckUnit.deck;
                        StateGame.tempConfig = this.aiDifficultyPanel.getSelectedDifficulty();
                        arg1.enterState(Game.STATE_GAME);
                    }
                    break;
                case DeckSelectPanel.DECK_CANCEL:
                    this.aiDifficultyPanel.setVisible(false);
                    break;
                case PlayButton.CLICKED:
                    this.aiDifficultyPanel.setPos(new Vector2f(0.5f + this.aiDifficultyPanel.getWidth(false) / Config.WINDOW_WIDTH, 0), 1);
                    this.aiDifficultyPanel.setPos(new Vector2f(0.5f, 0), 0.99);
                    this.aiDifficultyPanel.setVisible(true);
                    break;
                default:
                    break;
            }
        });
        GenericButton deckbuildbutton = new GenericButton(this.ui, new Vector2f(0, 0.25f), new Vector2f(120, 80), "Manage Decks",
                () -> arg1.enterState(Game.STATE_DECKBUILD));
        deckbuildbutton.relpos = true;
        this.ui.addUIElementParent(deckbuildbutton);
        GenericButton dungeonrunbutton = new GenericButton(this.ui, new Vector2f(0, -0.25f), new Vector2f(120, 80), "Dungeon Run",
                () -> arg1.enterState(Game.STATE_DUNGEONRUN));
        dungeonrunbutton.relpos = true;
        this.ui.addUIElementParent(dungeonrunbutton);
        GenericButton pvpbutton = new GenericButton(this.ui, new Vector2f(0, 0.35f), new Vector2f(120, 80), "PVP",
                this::tryEnterPVP);
        pvpbutton.relpos = true;
        this.ui.addUIElementParent(pvpbutton);
        GenericButton helpButton = new GenericButton(this.ui, new Vector2f(0, 0.15f), new Vector2f(120, 80), "How to play",
                () -> arg1.enterState(Game.STATE_HELP));
        helpButton.relpos = true;
        this.ui.addUIElementParent(helpButton);
        this.playButton = new PlayButton(ui);
        this.ui.addUIElementParent(this.playButton);
        this.aiDifficultyPanel = new AIDifficultyPanel(this.ui, new Vector2f());
        this.aiDifficultyPanel.setVisible(false);
        this.aiDifficultyPanel.relpos = true;
        this.aiDifficultyPanel.alignh = 1;
        this.ui.addUIElementParent(this.aiDifficultyPanel);

        this.errorText = new Text(this.ui, new Vector2f(0, 0.4f), "Error", 1000, 50, 40, 0, 0);
        this.errorText.relpos = true;
        this.errorText.setVisible(false);
        this.ui.addUIElementParent(this.errorText);
        this.errorTimer = 0;
    }

    private void showError(String message) {
        this.errorText.setText(message);
        this.errorTimer = ERROR_DISPLAY_TIME;
    }

    private void tryEnterPVP() {
        if (ConstructedDeck.decks.isEmpty()) {
            this.showError("You need to create a deck first!");
            return;
        }
        this.game.enterState(Game.STATE_PVP);
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
        double frametime = arg2 / 1000.;
        this.ui.update(frametime);
        if (this.errorTimer > 0) {
            this.errorTimer -= frametime;
        }
        this.errorText.setVisible(this.errorTimer > 0);
    }

    @Override
    public int getID() {
        // TODO Auto-generated method stub
        return Game.STATE_MENU;
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ESCAPE) {
            this.container.exit();
        }
    }
}
