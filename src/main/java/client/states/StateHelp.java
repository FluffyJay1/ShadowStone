package client.states;

import client.Game;
import client.ui.GenericButton;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIElement;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class StateHelp extends BasicGameState {
    UI ui;
    GameContainer container;

    @Override
    public int getID() {
        return Game.STATE_HELP;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        this.container = container;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        this.ui = new UI();
        container.getInput().addListener(this.ui);

        GenericButton quitButton = new GenericButton(this.ui, new Vector2f(0.5f, -0.5f), new Vector2f(150, 50), "Quit",
                () -> game.enterState(Game.STATE_MENU));
        quitButton.alignh = 1;
        quitButton.alignv = -1;
        quitButton.relpos = true;
        quitButton.setZ(1);
        this.ui.addUIElementParent(quitButton);
        UIElement helpGraphic = new UIElement(ui, new Vector2f(), "howtoplay.png");
        this.ui.addUIElementParent(helpGraphic);
        Text extraText = new Text(ui, new Vector2f(0, 0.5f), "Ingame, click on cards to read what they do, and click on text to read about what bolded words mean",
                1500, 50, 40, 0, 1);
        extraText.relpos = true;
        this.ui.addUIElementParent(extraText);
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        this.ui.draw(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        double frametime = delta / 1000.;
        this.ui.update(frametime);
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ESCAPE) {
            this.container.exit();
        }
    }
}
