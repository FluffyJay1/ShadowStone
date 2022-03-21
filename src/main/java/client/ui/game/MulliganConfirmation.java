package client.ui.game;

import client.Game;
import client.ui.GenericButton;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIElement;
import org.newdawn.slick.geom.Vector2f;

public class MulliganConfirmation extends UIElement {
    public MulliganConfirmation(UI ui, Vector2f pos, Runnable onConfirm) {
        super(ui, pos);
        Text text = new Text(ui, new Vector2f(0, -100), "Choose cards to replace", 500, 30, Game.DEFAULT_FONT, 30, 0, 0);
        this.addChild(text);
        GenericButton button = new GenericButton(ui, pos, new Vector2f(200, 100), "Confirm", onConfirm);
        this.addChild(button);
    }
}
