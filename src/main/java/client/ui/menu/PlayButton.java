package client.ui.menu;

import org.newdawn.slick.geom.Vector2f;

import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;

public class PlayButton extends UIBox {
    final Text text;
    public final DeckSelectPanel deckspanel;

    public PlayButton(UI ui) {
        super(ui, new Vector2f(0, 0), new Vector2f(128, 128), "res/ui/uiboxborder.png");
        this.relpos = true;
        this.text = new Text(ui, new Vector2f(0, 0), "<b> PLAY GAME", 128, 24, "Verdana", 30, 0, 0);
        text.setParent(this);
        this.deckspanel = new DeckSelectPanel(ui, new Vector2f(), false);
        this.deckspanel.setVisible(false);
        this.addChild(this.deckspanel);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        if (this.pointIsInHitbox(new Vector2f(x, y))) {
            this.deckspanel.updateDecks();
            this.deckspanel.setVisible(true);
            this.deckspanel.setPos(new Vector2f(0, -1000), 1);
            this.deckspanel.setPos(new Vector2f(0, 0), 0.99);
        }
    }
}
