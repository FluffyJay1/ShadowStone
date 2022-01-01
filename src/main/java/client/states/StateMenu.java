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

    @Override
    public void init(GameContainer arg0, StateBasedGame arg1) {
        // TODO Auto-generated method stub

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
            case "deckbuild":
                arg1.enterState(Game.STATE_DECKBUILD);
                break;
            default:
                break;
            }
        });
        GenericButton deckbuildbutton = new GenericButton(this.ui, new Vector2f(0, 0.25f), new Vector2f(120, 80),
                "Manage Decks", 0) {
            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                this.alert("deckbuild");
            }
        };
        deckbuildbutton.relpos = true;
        this.ui.addUIElementParent(deckbuildbutton);
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

}
