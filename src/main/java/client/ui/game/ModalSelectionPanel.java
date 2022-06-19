package client.ui.game;

import client.Config;
import client.ui.*;
import org.newdawn.slick.geom.Vector2f;
import server.card.target.ModalOption;
import server.card.target.ModalTargetingScheme;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ModalSelectionPanel extends UIBox {
    private static final Vector2f SELECTION_DIM = new Vector2f(300, 200);
    private List<UIElement> childButtons;
    private Function<Integer, Boolean> onClick;
    public ModalSelectionPanel(UI ui, Function<Integer, Boolean> onClick) {
        super(ui, new Vector2f(), new Vector2f(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        this.ignorehitbox = true;
        this.onClick = onClick;
        this.childButtons = new LinkedList<>();
    }

    public void setTargetingScheme(ModalTargetingScheme scheme) {
        for (UIElement childButton : this.childButtons) {
            this.removeChild(childButton);
        }
        this.childButtons.clear();
        for (int i = 0; i < scheme.getOptions().size(); i++) {
            float destX = (float) (i + 1) / (scheme.getOptions().size() + 1) - 0.5f;
            ModalOption mo = scheme.getOptions().get(i);
            ModalSelectionButton button = new ModalSelectionButton(this.ui, new Vector2f(destX, 0), mo.getName(), i, mo.conditions(scheme));
            this.childButtons.add(button);
            this.addChild(button);
        }
    }

    private class ModalSelectionButton extends UIBox {
        int index;
        boolean enabled;
        ModalSelectionButton(UI ui, Vector2f pos, String message, int index, boolean enabled) {
            super(ui, pos, SELECTION_DIM, new Animation("res/ui/button.png", new Vector2f(2, 1), 0, 0));
            this.addChild(new Text(ui, new Vector2f(0, 0), message, SELECTION_DIM.x * 0.8f, 20, 24, 0, 0));
            this.relpos = true;
            this.index = index;
            this.enabled = enabled;
            if (!enabled) {
                this.setAlpha(0.3f);
            }
        }

        @Override
        public void mouseClicked(int button, int x, int y, int clickCount) {
            if (this.enabled) {
                boolean selected = onClick.apply(this.index);
                if (selected) {
                    this.getAnimation().setFrame(1);
                } else {
                    this.getAnimation().setFrame(0);
                }
            }
        }
    }
}
