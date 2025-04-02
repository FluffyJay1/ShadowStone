package client.ui.menu;

import client.Game;
import client.ui.Animation;
import client.ui.Checkbox;
import client.ui.GenericButton;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import client.ui.UIElement;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.newdawn.slick.geom.Vector2f;

public class SettingsButton extends UIElement {
    public SettingsButton(UI ui) {
        super(ui, new Vector2f(-0.5f, -0.5f));
        this.setZ(100);
        this.relpos = true;
        UIBox panel = new UIBox(ui, new Vector2f(-0.5f, -0.5f), new Vector2f(250, 250), "ui/uiboxborder.png");
        panel.relpos = true;
        panel.alignh = -1;
        panel.alignv = -1;
        panel.margins = new Vector2f(16, 16);
        panel.setVisible(false);
        this.addChild(panel);
        this.addOption(ui, panel, -0.1f, "Fullscreen", Game::getFullscreen, Game::setFullscreen);
        this.addOption(ui, panel, 0.2f, "Music", Game::getMusic, Game::setMusic);
        GenericButton visibilityButton = new GenericButton(ui, new Vector2f(-0.5f, -0.5f), new Vector2f(64, 64), new Animation("ui/settings.png", new Vector2f(1, 1), 0, 0), () -> panel.setVisible(!panel.isVisible()));
        visibilityButton.relpos = true;
        visibilityButton.alignh = -1;
        visibilityButton.alignv = -1;
        this.addChild(visibilityButton);
    }

    private void addOption(UI ui, UIBox panel, float y, String message, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Checkbox checkbox = new Checkbox(ui, new Vector2f(-0.5f, y), getter, setter);
        checkbox.relpos = true;
        checkbox.alignh = -1;
        panel.addChild(checkbox);
        Text text = new Text(ui, new Vector2f(checkbox.getRight(false, false), checkbox.getCenterPos().y), message, 200, 20, 24, -1, 0);
        panel.addChild(text);
    }
}
