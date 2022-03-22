package client.ui.game;

import client.ui.*;
import org.newdawn.slick.geom.Vector2f;

public class MulliganConfirmation extends UIElement {
    private static final float WIDTH = 1200;
    private static final float HEIGHT = 860;
    private static final float MIDDLE_GAP = 80;
    private static final float BOX_ALPHA = 0.9f;

    private final GenericButton button;
    private final Text text;

    public MulliganConfirmation(UI ui, Vector2f pos, Runnable onConfirm) {
        super(ui, pos);
        UIBox topBox = new UIBox(ui, new Vector2f(0, -(HEIGHT + MIDDLE_GAP)/4), new Vector2f(WIDTH, (HEIGHT - MIDDLE_GAP) / 2), "res/ui/uiboxborder.png");
        topBox.margins.set(20, 20);
        topBox.setAlpha(BOX_ALPHA);
        this.addChild(topBox);
        UIBox botBox = new UIBox(ui, new Vector2f(0, (HEIGHT + MIDDLE_GAP)/4), new Vector2f(WIDTH, (HEIGHT - MIDDLE_GAP) / 2), "res/ui/uiboxborder.png");
        botBox.margins.set(20, 20);
        botBox.setAlpha(BOX_ALPHA);
        this.addChild(botBox);
        this.text = new Text(ui, new Vector2f(0, 0), "Drag cards up to replace", 500, 30, 30, 0, 0);
        this.addChild(this.text);
        Text replaceText = new Text(ui, new Vector2f(0, -topBox.getHeight(true)/2), "Replace", 500, 30, 30, 0, -1);
        topBox.addChild(replaceText);
        Text keepText = new Text(ui, new Vector2f(0, botBox.getHeight(true)/2), "Keep", 500, 30, 30, 0, 1);
        botBox.addChild(keepText);
        this.button = new GenericButton(ui, new Vector2f(WIDTH/2, 0), new Vector2f(200, 60), "Confirm", onConfirm);
        this.button.alignh = -1;
        this.addChild(this.button);
    }

    public void setEnableInput(boolean enabled) {
        this.button.setVisible(enabled);
        this.text.setVisible(enabled);
    }
}
