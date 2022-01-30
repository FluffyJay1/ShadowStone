package client.ui;

import client.Game;
import org.newdawn.slick.geom.*;

public class GenericButton extends UIBox {
    final Text text;
    final Vector2f originalDim;
    // in case you want to create a buttload of genericbuttons
    public final int index;

    public GenericButton(UI ui, Vector2f pos, Vector2f dim, String message, int index) {
        super(ui, pos, dim, new Animation("res/ui/button.png", new Vector2f(2, 1), 0, 0));
        this.index = index;
        this.originalDim = dim.copy();
        this.text = new Text(ui, new Vector2f(0, 0), message, dim.x * 0.8, 20, Game.DEFAULT_FONT, 24, 0, 0);
        text.setParent(this);
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        // TODO replace this with an actual animation
        this.getAnimation().setFrame(1);
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        this.getAnimation().setFrame(0);
    }

}
