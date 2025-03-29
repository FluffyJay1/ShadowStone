package client.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.newdawn.slick.geom.*;

public class Checkbox extends UIBox {
    Supplier<Boolean> getter;
    Consumer<Boolean> setter;

    public Checkbox(UI ui, Vector2f pos, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(ui, pos, new Vector2f(64, 64), new Animation("ui/checkbox.png", new Vector2f(2, 1), 0, 0));
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        this.setter.accept(!this.getter.get());
    }

    @Override
    public void update(double frametime) {
        this.animation.setFrame(this.getter.get() ? 1 : 0);
    }
}
