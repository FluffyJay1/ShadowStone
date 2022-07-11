package client.ui.game;

import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import org.newdawn.slick.geom.Vector2f;

public class EmoteDisplayUnit extends UIBox {
    private static final double DISPLAY_DURATION = 0.5;
    private static final double FADE_DURATION = 2;
    private static final Interpolation<Double> FADE_INTERPOLATION = new LinearInterpolation(1, 0);

    private final Text text;
    private double timer;

    public EmoteDisplayUnit(UI ui, Vector2f pos, String message) {
        super(ui, pos, new Vector2f(), "");
        this.text = new Text(ui, new Vector2f(), message, 600, 40, 42, 0, 0);
        this.addChild(text);
        this.timer = 0;
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.timer += frametime;
        if (this.timer > DISPLAY_DURATION + FADE_DURATION) {
            this.removeParent();
        } else if (this.timer > DISPLAY_DURATION) {
            float alpha = FADE_INTERPOLATION.get((this.timer - DISPLAY_DURATION) / FADE_DURATION).floatValue();
            this.text.setAlpha(alpha);
        }
    }
}
