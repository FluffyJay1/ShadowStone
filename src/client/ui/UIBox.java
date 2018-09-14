package client.ui;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class UIBox extends UIElement {

	private Vector2f dim = new Vector2f();

	public UIBox(UI ui, Vector2f pos, Vector2f dim, String imagepath) {
		super(ui, pos, imagepath);
		this.dim.set(dim);
	}

	@Override
	public void draw(Graphics g) {
		if (this.image != null) {
			this.finalImage = this.image.getScaledCopy((int) this.getWidth(false), (int) this.getHeight(false));
			// this.finalImage.rotate((float) this.angle);
			if (!this.hide) {
				g.drawImage(this.finalImage, (float) this.getLeft(true, false), (float) this.getTop(true, false));
			}
		}
		if (!this.hide) {
			this.drawChildren(g);
		}
	}

	public void setDim(Vector2f dim) {
		this.dim.set(dim);
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
