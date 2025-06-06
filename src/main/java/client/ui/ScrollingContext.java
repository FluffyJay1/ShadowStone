package client.ui;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class ScrollingContext extends UIBox {
    public ScrollingContext(UI ui, Vector2f pos, Vector2f dim) {
        super(ui, pos, dim);
        this.scrollable = true;
        this.ignorehitbox = true;
    }

    public void mouseWheelMoved(int change) {
        this.childoffset.y += change * 0.5;
        this.constrainScroll();
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.constrainScroll();
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        double childrenHeight = this.getChildLocalBottom(0) - this.getChildLocalTop(0);
        if (childrenHeight > this.getHeight(false)) {
            g.fillRect(this.getRight(true, false) - 5,
                    (float) (this.getTop(true, false)
                            - (this.getChildLocalTop(0) + this.getVOff() + this.childoffset.y)
                                    * this.getHeight(false) / childrenHeight),
                    5, (float) (this.getHeight(false) * this.getHeight(false) / childrenHeight));

        }
    }

    public void constrainScroll() {
        this.childoffset.y = Math.min(this.childoffset.y,
                -this.getVOff() - this.getChildLocalTop(0));
        this.childoffset.y = Math.max(this.childoffset.y,
                -this.getVOff() + this.getHeight(false) - this.getChildLocalBottom(0));
    }
}
