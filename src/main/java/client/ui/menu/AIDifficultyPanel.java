package client.ui.menu;

import client.ui.*;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AIConfig;

import java.util.ArrayList;
import java.util.List;

public class AIDifficultyPanel extends UIBox {
    private static final AIConfig[] OPTIONS = { AIConfig.BEGINNER, AIConfig.NOVICE, AIConfig.PRO, AIConfig.MASTER };
    private static final float FAN_HEIGHT = 175;
    List<DifficultySelectionButton> buttons;
    int selected;
    public AIDifficultyPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(250, 300), "res/ui/uiboxborder.png");
        Text text = new Text(ui, new Vector2f(0, -100), "Choose AI difficulty", 220, 25, 30, 0, 0);
        this.addChild(text);
        this.buttons = new ArrayList<>();
        for (int i = 0; i < OPTIONS.length; i++) {
            float y = text.getHeight(false) / 2 + FAN_HEIGHT * ((i + 0.5f) / OPTIONS.length - 0.5f);
            DifficultySelectionButton button = new DifficultySelectionButton(ui, new Vector2f(0, y), OPTIONS[i].name, i);
            this.buttons.add(button);
            this.addChild(button);
        }
        this.selected = 2; // first select pro
        this.updateSelection();
    }

    public AIConfig getSelectedDifficulty() {
        return OPTIONS[this.selected];
    }

    private void updateSelection() {
        for (DifficultySelectionButton button : this.buttons) {
            button.setSelected(false);
        }
        this.buttons.get(this.selected).setSelected(true);
    }

    private void onSelect(int i) {
        this.selected = i;
        this.updateSelection();
    }

    private class DifficultySelectionButton extends UIBox {
        int index;
        boolean selected;
        DifficultySelectionButton(UI ui, Vector2f pos, String message, int index) {
            super(ui, pos, new Vector2f(200, 40), new Animation("res/ui/button.png", new Vector2f(2, 1), 0, 0));
            this.margins.set(15, 10);
            this.addChild(new Text(ui, new Vector2f(0, 0), message, this.getWidth(true), 20, 24, 0, 0));
            this.index = index;
            this.updateSelected();
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            this.updateSelected();
        }

        private void updateSelected() {
            if (this.selected) {
                this.getAnimation().setFrame(1);
            } else {
                this.getAnimation().setFrame(0);
            }
        }

        @Override
        public void mouseClicked(int button, int x, int y, int clickCount) {
            onSelect(this.index);
        }
    }
}
