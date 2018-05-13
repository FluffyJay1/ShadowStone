package client.ui;

import java.util.ArrayList;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import server.card.BoardObject;
import server.card.Card;
import server.card.Minion;
import server.event.EventMinionAttack;
import utils.DefaultMouseListener;

public class UI { // lets do this right this time
	ArrayList<UIElement> parentList = new ArrayList<UIElement>();
	ArrayList<UIElement> parentListAddBuffer = new ArrayList<UIElement>();
	ArrayList<UIElement> parentListRemoveBuffer = new ArrayList<UIElement>();
	UIElement pressedElement = null;

	public UI() {

	}

	public void update(double frametime) {
		for (UIElement u : this.parentList) {
			u.update(frametime);
			if (u.remove) {
				this.parentListRemoveBuffer.add(u);
			}
		}
		this.parentList.addAll(this.parentListAddBuffer);
		this.parentList.removeAll(this.parentListRemoveBuffer);
		this.parentListAddBuffer.clear();
		this.parentListRemoveBuffer.clear();
	}

	public void draw(Graphics g) {
		for (UIElement u : this.parentList) {
			u.draw(g);
		}
	}

	public void addUIElementParent(UIElement u) {
		this.parentListAddBuffer.add(u);
	}

	public UIElement getTopUIElement(Vector2f pos) {
		UIElement ret = null;
		for (UIElement u : this.parentList) {
			UIElement test = u.topChildAtPos(pos);
			if (test != null) {
				ret = test;
			}
		}
		return ret;
	}

	public boolean mousePressed(int button, int x, int y) {
		UIElement top = this.getTopUIElement(new Vector2f(x, y));
		if (top != null) {
			this.pressedElement = top;
			top.mousePressed(button, x, y);
			return true;
		}
		return false;
	}

	public void mouseReleased(int button, int x, int y) {
		if (this.pressedElement != null) {
			this.pressedElement.mouseReleased(button, x, y);
			this.pressedElement = null;
		}

	}

	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		LinkedList<UIElement> temp = new LinkedList<UIElement>();
		temp.addAll(this.parentList);
		while (!temp.isEmpty()) { // who needs recursion
			temp.getFirst().mouseMoved(oldx, oldy, newx, newy);
			temp.addAll(temp.getFirst().getChildren());
			temp.removeFirst();
		}
	}

	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		if (this.pressedElement != null) {
			if (this.pressedElement.draggable) {
				this.pressedElement
						.setPos(this.pressedElement.getPos().copy().add(new Vector2f(newx - oldx, newy - oldy)), 1);
			}
			this.pressedElement.mouseDragged(oldx, oldy, newx, newy);

		}
	}
}
