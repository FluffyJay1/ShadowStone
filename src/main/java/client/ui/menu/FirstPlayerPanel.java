package client.ui.menu;

import client.ui.Animation;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import org.newdawn.slick.geom.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class FirstPlayerPanel extends UIBox {
    private static final String[] OPTIONS = { "Random", "Go first", "Go second" };
    private static final float FAN_HEIGHT = 125;
    List<FirstPlayerSelectionButton> buttons;
    int selected;
    public FirstPlayerPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(250, 150), "ui/uiboxborder.png");
        this.buttons = new ArrayList<>();
        for (int i = 0; i < OPTIONS.length; i++) {
            float y = FAN_HEIGHT * ((i + 0.5f) / OPTIONS.length - 0.5f);
            FirstPlayerSelectionButton button = new FirstPlayerSelectionButton(ui, new Vector2f(0, y), OPTIONS[i], i);
            this.buttons.add(button);
            this.addChild(button);
        }
        this.selected = 0; // first select random
        this.updateSelection();
    }

    public int getTeamMultiplier() {
        if (this.selected == 2) {
            return -1;
        }
        return this.selected;
    }

    private void updateSelection() {
        for (FirstPlayerSelectionButton button : this.buttons) {
            button.setSelected(false);
        }
        this.buttons.get(this.selected).setSelected(true);
    }

    private void onSelect(int i) {
        this.selected = i;
        this.updateSelection();
    }

    private class FirstPlayerSelectionButton extends UIBox {
        int index;
        boolean selected;
        FirstPlayerSelectionButton(UI ui, Vector2f pos, String message, int index) {
            super(ui, pos, new Vector2f(200, 40), new Animation("ui/button.png", new Vector2f(2, 1), 0, 0));
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
