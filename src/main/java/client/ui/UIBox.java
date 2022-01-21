package client.ui;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

public class UIBox extends UIElement {

    private Vector2f dim = new Vector2f(), originalDim = new Vector2f();

    public UIBox(UI ui, Vector2f pos, Vector2f dim) {
        super(ui, pos);
        this.dim.set(dim);
        this.originalDim.set(dim);
    }

    public UIBox(UI ui, Vector2f pos, Vector2f dim, String imagepath) {
        super(ui, pos, imagepath);
        this.dim.set(dim);
        this.originalDim.set(dim);
    }

    public UIBox(UI ui, Vector2f pos, Vector2f dim, Animation animation) {
        super(ui, pos, animation);
        this.dim.set(dim);
        this.originalDim.set(dim);
    }

    @Override
    public void setScale(double scale) {
        super.setScale(scale);
        this.dim.set(this.originalDim.copy().scale((float) scale));
    }

    @Override
    public void draw(Graphics g) {
        if (this.isVisible()) {
            this.drawSelf(g);
            this.drawChildren(g);
        }
    }

    public void drawSelf(Graphics g) {
        if (this.animation != null) {
            this.finalImage = this.animation.getCurrentFrame().getScaledCopy((int) this.getWidth(false),
                    (int) this.getHeight(false));
            // this.finalImage.rotate((float) this.angle);
            this.finalImage.setAlpha((float) this.getAlpha());
            g.drawImage(this.finalImage, (float) this.getLeft(true, false), (float) this.getTop(true, false));
        }
    }

    public void setDim(Vector2f dim) {
        this.originalDim.set(dim);
        this.setScale(1);
    }

    public Vector2f getOriginalDim() {
        return this.originalDim;
    }

    @Override
    public double getWidth(boolean margin) {
        return this.dim.x - (margin ? this.margins.x * 2 : 0);
    }

    @Override
    public double getHeight(boolean margin) {
        return this.dim.y - (margin ? this.margins.y * 2 : 0);
    }
}
