package client.ui.game;

import client.Game;
import client.ui.*;
import org.newdawn.slick.geom.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ManaOrbPanel extends UIBox {
    private static final int MAX_DISPLAYED_ORBS = 10;
    private static final float ORB_SCALE = 0.5f;
    private static final float FAN_WIDTH = 30 * MAX_DISPLAYED_ORBS;
    private static final float TEXT_WIDTH = 30;
    List<UIElement> orbs;
    Text manaText;
    public ManaOrbPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(TEXT_WIDTH + FAN_WIDTH + 62, 45), "res/ui/uiboxborder.png");
        this.margins.set(15, 10);
        this.orbs = new ArrayList<>();
        for (int i = 0; i < MAX_DISPLAYED_ORBS; i++) {
            float x = TEXT_WIDTH + FAN_WIDTH * ((i + 0.5f) / MAX_DISPLAYED_ORBS - 0.5f);
            UIElement orb = new UIElement(ui, new Vector2f(x, 0), new Animation("res/game/manaorb.png", new Vector2f(2, 1), 0, 0));
            orb.setVisible(false);
            orb.setScale(ORB_SCALE);
            this.orbs.add(orb);
            this.addChild(orb);
        }
        this.manaText = new Text(ui, new Vector2f(-this.getWidth(true)/2, 0), "0/0", TEXT_WIDTH, 30, Game.DEFAULT_FONT, 30, -1, 0);
        this.addChild(this.manaText);
    }

    public void updateMana(int num, int max) {
        for (int i = 0; i < max && i < MAX_DISPLAYED_ORBS; i++) {
            this.orbs.get(i).setVisible(true);
        }
        for (int i = max; i < MAX_DISPLAYED_ORBS; i++) {
            this.orbs.get(i).setVisible(false);
        }
        for (int i = 0; i < num && i < MAX_DISPLAYED_ORBS; i++) {
            this.orbs.get(i).getAnimation().setFrame(0);
        }
        for (int i = num; i < MAX_DISPLAYED_ORBS; i++) {
            this.orbs.get(i).getAnimation().setFrame(1);
        }
        this.manaText.setText(num + "/" + max);
    }
}
