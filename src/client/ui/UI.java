package client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Vector2f;

import server.card.BoardObject;
import server.card.Card;
import server.card.Minion;
import server.event.EventMinionAttack;
import utils.DefaultKeyListener;
import utils.DefaultMouseListener;

public class UI implements DefaultKeyListener { // lets do this right this time
	public static final boolean DEBUG = false;
	ArrayList<UIElement> parentList = new ArrayList<UIElement>();
	ArrayList<UIElement> parentListAddBuffer = new ArrayList<UIElement>();
	ArrayList<UIElement> parentListRemoveBuffer = new ArrayList<UIElement>();
	ArrayList<UIEventListener> listeners = new ArrayList<UIEventListener>();
	UIElement pressedElement = null, draggingElement = null, focusedElement = null;
	public Vector2f lastmousepos = new Vector2f();
	public boolean[] pressedKeys = new boolean[255];

	public UI() {

	}

	public void update(double frametime) {
		for (UIElement u : this.parentList) {
			u.update(frametime);
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
		if (DEBUG && this.focusedElement != null) {
			g.drawRect((float) this.focusedElement.getLeft(true, false),
					(float) this.focusedElement.getTop(true, false), (float) this.focusedElement.getWidth(false),
					(float) this.focusedElement.getHeight(false));
		}
	}

	public void addUIElementParent(UIElement u) {
		this.parentListAddBuffer.add(u);
	}

	public void removeUIElementParent(UIElement u) {
		this.parentListRemoveBuffer.add(u);
	}

	public UIElement getTopUIElement(Vector2f pos, boolean requirehitbox, boolean requirescrollable,
			boolean requiredraggable) {
		UIElement ret = null;
		for (UIElement u : this.parentList) {
			UIElement test = u.topChildAtPos(pos, requirehitbox, requirescrollable, requiredraggable);
			if (test != null) {
				ret = test;
			}
		}
		return ret;
	}

	// somewhat useful i guess
	public UIElement getCommonParent(UIElement first, UIElement second) {
		if (first == second) {
			return first;
		}
		Set<UIElement> trace = new HashSet<UIElement>();
		for (UIElement e = first; e != null; e = e.getParent()) {
			trace.add(e);
		}
		for (UIElement e = second; e != null; e = e.getParent()) {
			if (trace.contains(e)) {
				return e;
			}
		}
		return null;
	}

	public void focusElement(UIElement element) {
		UIElement commonParent = this.getCommonParent(element, this.focusedElement);
		for (UIElement e = this.focusedElement; e != commonParent; e = e.getParent()) {
			e.hasFocus = false;
			// transfer key press
			for (int i = 0; i < this.pressedKeys.length; i++) {
				if (this.pressedKeys[i]) {
					e.keyReleased(i, Input.getKeyName(i).length() == 1 ? Input.getKeyName(i).charAt(0) : 0);
				}
			}
		}
		this.focusedElement = element;
		// who needs recursion
		for (UIElement e = element; e != commonParent; e = e.getParent()) {
			e.hasFocus = true;
			// transfer key press
			for (int i = 0; i < this.pressedKeys.length; i++) {
				if (this.pressedKeys[i]) {
					e.keyPressed(i, Input.getKeyName(i).length() == 1 ? Input.getKeyName(i).charAt(0) : 0);
				}
			}
		}
	}

	public void addListener(UIEventListener uel) {
		// i cannot be assed to make buffers for these
		this.listeners.add(uel);
	}

	public void removeListener(UIEventListener uel) {
		this.listeners.remove(uel);
	}

	public void alertListeners(String strarg, int... intarg) {
		for (UIEventListener uel : this.listeners) {
			uel.onAlert(strarg, intarg);
		}
	}

	public void mouseClicked(int button, int x, int y, int clickCount) {
		if (DEBUG) {
			for (UIElement u : this.parentList) {
				u.debugPrint(0);
			}
		}
		UIElement top = this.getTopUIElement(new Vector2f(x, y), true, false, false);
		if (top != null) {
			top.mouseClicked(button, x, y, clickCount);
		}
	}

	public boolean mousePressed(int button, int x, int y) {
		UIElement top = this.getTopUIElement(new Vector2f(x, y), true, false, false);
		this.pressedElement = top;
		this.focusElement(top);
		if (top != null) {
			top.mousePressed(button, x, y);
		}
		UIElement dragging = this.getTopUIElement(new Vector2f(x, y), true, false, true);
		if (dragging != null) {
			this.draggingElement = dragging;
		}
		return top != null;
	}

	public void mouseReleased(int button, int x, int y) {

		if (this.pressedElement != null) {
			this.pressedElement.mouseReleased(button, x, y);
			this.pressedElement = null;
		}
		this.draggingElement = null;

	}

	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		LinkedList<UIElement> temp = new LinkedList<UIElement>();
		temp.addAll(this.parentList);
		while (!temp.isEmpty()) { // who needs recursion
			temp.getFirst().mouseMoved(oldx, oldy, newx, newy);
			temp.addAll(temp.getFirst().getChildren());
			temp.removeFirst();
		}
		this.lastmousepos.set(newx, newy);
	}

	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		if (this.pressedElement != null) {
			this.pressedElement.mouseDragged(oldx, oldy, newx, newy);
		}
		if (this.draggingElement != null) {
			if (this.draggingElement.draggable) {
				this.draggingElement
						.setPos(this.draggingElement.getPos().copy().add(new Vector2f(newx - oldx, newy - oldy)), 1);
			}
			this.draggingElement.mouseDragged(oldx, oldy, newx, newy);

		}
		this.lastmousepos.set(newx, newy);
	}

	public void mouseWheelMoved(int change) {
		UIElement top = this.getTopUIElement(this.lastmousepos, false, true, false);
		if (top != null) {
			top.mouseWheelMoved(change);
		}
	}

	@Override
	public void keyPressed(int key, char c) {
		this.pressedKeys[key] = true;
		for (UIElement e = this.focusedElement; e != null; e = e.getParent()) {
			e.keyPressed(key, c);
		}
	}

	@Override
	public void keyReleased(int key, char c) {
		this.pressedKeys[key] = false;
		for (UIElement e = this.focusedElement; e != null; e = e.getParent()) {
			e.keyReleased(key, c);
		}
	}
}
