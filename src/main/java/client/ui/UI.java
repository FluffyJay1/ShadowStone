package client.ui;

import java.util.*;
import java.util.function.Consumer;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import utils.*;

public class UI implements DefaultInputListener { // lets do this right this time
    public static final boolean DEBUG = false;
    final List<UIElement> parentList = new ArrayList<>();
    final List<UIElement> parentListAddBuffer = new ArrayList<>();
    final List<UIElement> parentListRemoveBuffer = new ArrayList<>();
    final List<UIEventListener> listeners = new ArrayList<>();
    UIElement pressedElement = null, draggingElement = null, focusedElement = null;
    public Vector2f lastmousepos = new Vector2f();
    private double scale = 1;
    public final boolean[] pressedKeys = new boolean[255];
    public boolean updateZOrder = false;

    Consumer<UIElement> onClick, onPress, onRelease;

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
        if (this.updateZOrder) {
            Collections.sort(this.parentList);
            this.updateZOrder = false;
        }
    }

    public void draw(Graphics g) {
        for (UIElement u : this.parentList) {
            u.draw(g);
        }
        if (DEBUG && this.focusedElement != null) {
            g.drawRect(this.focusedElement.getLeft(true, false),
                    this.focusedElement.getTop(true, false), this.focusedElement.getWidth(false),
                    this.focusedElement.getHeight(false));
        }
    }

    public void addUIElementParent(UIElement u) {
        this.parentListAddBuffer.add(u);
        this.updateZOrder = true;
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
        Set<UIElement> trace = new HashSet<>();
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

    @Override
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
        if (this.onClick != null) {
            this.onClick.accept(top);
        }
    }

    public void setOnClick(Consumer<UIElement> onClick) {
        this.onClick = onClick;
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        UIElement top = this.getTopUIElement(new Vector2f(x, y), true, false, false);
        this.pressedElement = top;
        this.focusElement(top);
        if (top != null) {
            top.mousePressed(button, x, y);
        }
        if (this.onPress != null) {
            this.onPress.accept(top);
        }
        UIElement dragging = this.getTopUIElement(new Vector2f(x, y), true, false, true);
        if (dragging != null) {
            this.draggingElement = dragging;
        }
    }

    public void setOnPress(Consumer<UIElement> onPress) {
        this.onPress = onPress;
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        if (this.onRelease != null) {
            this.onRelease.accept(this.pressedElement);
        }
        if (this.pressedElement != null) {
            this.pressedElement.mouseReleased(button, x, y);
            this.pressedElement = null;
        }
        this.draggingElement = null;

    }

    public void setOnRelease(Consumer<UIElement> onRelease) {
        this.onRelease = onRelease;
    }

    @Override
    public void mouseMoved(int oldx, int oldy, int newx, int newy) {
        LinkedList<UIElement> temp = new LinkedList<>(this.parentList);
        while (!temp.isEmpty()) { // who needs recursion
            temp.getFirst().mouseMoved(oldx, oldy, newx, newy);
            temp.addAll(temp.getFirst().getChildren());
            temp.removeFirst();
        }
        this.lastmousepos.set(newx, newy);
    }

    @Override
    public void mouseDragged(int oldx, int oldy, int newx, int newy) {
        if (this.pressedElement != null) {
            this.pressedElement.mouseDragged(oldx, oldy, newx, newy);
        }
        if (this.draggingElement != null) {
            if (this.draggingElement.draggable) {
                this.draggingElement.changeAbsPos(new Vector2f(newx - oldx, newy - oldy), 1);
            }
            this.draggingElement.mouseDragged(oldx, oldy, newx, newy);

        }
        this.lastmousepos.set(newx, newy);
    }

    @Override
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
