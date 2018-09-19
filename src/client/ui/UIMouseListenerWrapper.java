package client.ui;

import utils.DefaultMouseListener;

//this class exists because UI can't implement defaultmouselistener because the visual board needs to know if anything in the ui was pressed and in short fuck you
public class UIMouseListenerWrapper implements DefaultMouseListener {
	UI ui;

	public UIMouseListenerWrapper(UI ui) {
		this.ui = ui;
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		this.ui.mouseClicked(button, x, y, clickCount);
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		this.ui.mousePressed(button, x, y);
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		this.ui.mouseReleased(button, x, y);
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		this.ui.mouseMoved(oldx, oldy, newx, newy);
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		this.ui.mouseDragged(oldx, oldy, newx, newy);
	}

	@Override
	public void mouseWheelMoved(int change) {
		this.ui.mouseWheelMoved(change);
	}
}
