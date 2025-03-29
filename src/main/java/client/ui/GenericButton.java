package client.ui;

import org.newdawn.slick.geom.*;

public class GenericButton extends UIBox {
    Text text;
    UIElement icon;
    final Vector2f originalDim;
    Runnable onClick;
    private boolean enabled;

    public GenericButton(UI ui, Vector2f pos, Vector2f dim, String message, Animation icon, Runnable onClick) {
        super(ui, pos, dim, new Animation("ui/button.png", new Vector2f(2, 1), 0, 0));
        this.originalDim = dim.copy();
        if (icon != null) {
            this.icon = new UIElement(ui, new Vector2f(), icon);
            this.icon.ignorehitbox = true;
            this.icon.setParent(this);
        }
        if (message != null) {
            this.text = new Text(ui, new Vector2f(0, 0), message, dim.x * 0.8f, 20, 24, 0, 0);
            this.text.relpos = true;
            this.text.setParent(this);
        }
        this.onClick = onClick;
        this.enabled = true;
    }

    public GenericButton(UI ui, Vector2f pos, Vector2f dim, String message, Runnable onClick) {
        this(ui, pos, dim, message, null, onClick);
    }

    public GenericButton(UI ui, Vector2f pos, Vector2f dim, Animation icon, Runnable onClick) {
        this(ui, pos, dim, null, icon, onClick);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            this.setAlpha(1);
        } else {
            this.setAlpha(0.3f);
        }
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (this.enabled) {
            this.onClick.run();
        }
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        // TODO replace this with an actual animation
        if (this.enabled) {
            this.getAnimation().setFrame(1);
        }
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        this.getAnimation().setFrame(0);
    }

}
