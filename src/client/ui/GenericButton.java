package client.ui;

import org.newdawn.slick.geom.Vector2f;

public class GenericButton extends UIBox {
	Text text;
	Vector2f originalDim;
	// in case you want to create a buttload of genericbuttons
	public int index;

	public GenericButton(UI ui, Vector2f pos, Vector2f dim, String message, int index) {
		super(ui, pos, dim, "res/ui/uiboxborder.png");
		this.index = index;
		this.originalDim = dim.copy();
		this.text = new Text(ui, new Vector2f(0, 0), message, dim.x * 0.8, 20, "Verdana", 24, 0, 0);
		text.setParent(this);
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO replace this with an actual animation
		this.setDim(this.originalDim.copy().scale(0.9f));
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		this.setDim(this.originalDim);
	}

}
