package client.states;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.*;

import client.Game;
import client.ui.*;
import client.ui.menu.*;

public class StateMenu extends BasicGameState {
    UI ui;
    PlayButton playButton;
    GameContainer container;

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
            case DeckSelectPanel.DECK_CONFIRM:
                if (playButton.deckspanel.selectedDeckUnit != null) {
                    StateGame.tempdeck = playButton.deckspanel.selectedDeckUnit.deck;
                    arg1.enterState(Game.STATE_GAME);
                }

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
        this.playButton = new PlayButton(ui);
        this.ui.addUIElementParent(this.playButton);
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
