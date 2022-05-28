package client.ui;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import utils.*;

public class UIElement implements DefaultInputListener, UIEventListener, Comparable<UIElement> {
    public static final float EPSILON = 0.0001f;

    protected final UI ui;
    UIElement parent = null;
    final List<UIElement> children = new ArrayList<>();
    final List<UIElement> childrenAddBuffer = new ArrayList<>();
    final List<UIElement> childrenRemoveBuffer = new ArrayList<>();
    // alignment: how the element is placed relative to its pos, takes values (-1, 0, 1)
    // e.g. alignh of -1 means the element is shifted to the right such that the left edge is aligned with the pos
    public int alignh = 0, alignv = 0;
    private int z = 0; // z order is int based because optimization probably
    private boolean visible = true, updateZOrder = false;
    public boolean draggable = false, ignorehitbox = false, hitcircle = false, clip = false, scrollable = false,
            hasFocus = false, relpos = false;
    // relpos: represent position in terms of proportion of total width/height (considering margin), with (0, 0) being pos
    public Vector2f childoffset = new Vector2f(), margins = new Vector2f();
    private Vector2f targetpos, pos;
    private float scale = 1, angle = 0, alpha = 1;
    private double speed = 1;
    private UIElement followTarget;
    Animation animation;
    Image finalImage;
    private Color color;

    public UIElement(UI ui, Vector2f pos) {
        this.ui = ui;
        this.pos = pos.copy();
        this.targetpos = pos.copy();
        this.color = Color.white;
    }

    public UIElement(UI ui, Vector2f pos, String imagepath) {
        this(ui, pos);
        this.setImage(imagepath);
    }

    public UIElement(UI ui, Vector2f pos, Animation animation) {
        this(ui, pos);
        this.setAnimation(animation);
    }

    public UI getUI() {
        return this.ui;
    }

    public void setImage(String imagepath) {
        this.setAnimation(imagepath, new Vector2f(1, 1), 0, 0);
    }

    public void setImage(Image image) {
        this.setAnimation(new Animation(image));
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
        this.finalImage = animation.getCurrentFrame().getScaledCopy(this.scale);
    }

    public void setAnimation(String imagepath, Vector2f framedim, int spacing, int margin) {
        if (imagepath != null && !imagepath.isEmpty()) {
            this.animation = new Animation(imagepath, framedim, spacing, margin);
            this.finalImage = this.animation.getCurrentFrame().getScaledCopy(this.scale);
        }
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public void setPos(Vector2f pos, double speed) {
        this.targetpos.set(pos);
        this.speed = speed;
        if (speed == 1) {
            this.pos.set(pos);
        }
    }

    public void setAbsPos(Vector2f abspos, double speed) {
        Vector2f pos = this.getPosOfAbs(abspos);
        if (this.relpos) {
            pos = this.getRelOfPos(pos);
        }
        this.setPos(pos, speed);
    }

    public void changePos(Vector2f pos, double speed) {
        this.setPos(this.targetpos.copy().add(pos), speed);
    }

    public void changeAbsPos(Vector2f pos, double speed) {
        if (this.relpos) {
            if (this.parent == null) {
                this.changePos(new Vector2f(pos.x / Config.WINDOW_WIDTH, pos.y / Config.WINDOW_HEIGHT), speed);
            } else {
                this.changePos(new Vector2f(pos.x / this.parent.getWidth(true),
                        pos.y / this.parent.getHeight(true)), speed);
            }
        } else {
            this.changePos(pos, speed);
        }
    }

    // get the normal pos vector, i.e. not relpos
    public Vector2f getPos() {
        if (this.relpos) {
            return this.getPosOfRel(this.pos);
        }
        return this.pos.copy();
    }

    public Vector2f getCenterPos() {
        return this.getPos().add(new Vector2f(this.getHAlignOffset(), this.getVAlignOffset()));
    }

    // absolute position on the screen more or less
    public Vector2f getAbsPos() {
        return this.getAbsOfPos(this.getPos());
    }

    public Vector2f getCenterAbsPos() {
        return this.getAbsOfPos(this.getCenterPos());
    }

    public Vector2f getRelPos() {
        if (!this.relpos) {
            return this.getRelOfPos(this.getPos());
        }
        return this.pos.copy();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return this.scale;
    }

    public Vector2f getDim(boolean margin) {
        return new Vector2f(this.getWidth(margin), this.getHeight(margin));
    }

    public float getWidth(boolean margin) {
        if (this.finalImage == null) {
            return 0;
        }
        return this.finalImage.getWidth() - (margin ? this.margins.x * 2 : 0);
    }

    public float getHeight(boolean margin) {
        if (this.finalImage == null) {
            return 0;
        }
        return this.finalImage.getHeight() - (margin ? this.margins.y * 2 : 0);
    }

    // defined as distance from pos to left edge
    public float getHOff() {
        return this.getWidth(false) * (this.alignh + 1) / 2;
    }

    // defined as distance from pos to top edge
    public float getVOff() {
        return this.getHeight(false) * (this.alignv + 1) / 2;
    }

    // defined as offset from the pos to get to the center
    public float getHAlignOffset() {
        return this.getWidth(false) * -this.alignh / 2;
    }

    // defined as offset from the pos to get to the center
    public float getVAlignOffset() {
        return this.getHeight(false) * -this.alignv / 2;
    }

    public float getLeft(boolean abs, boolean margin) {
        return (abs ? this.getAbsPos().x : this.getPos().x) - this.getHOff() + (margin ? this.margins.x : 0);
    }

    public float getRight(boolean abs, boolean margin) {
        return (abs ? this.getAbsPos().x : this.getPos().x) - this.getHOff() + this.getWidth(false)
                - (margin ? this.margins.x : 0);
    }

    public float getTop(boolean abs, boolean margin) {
        return (abs ? this.getAbsPos().y : this.getPos().y) - this.getVOff() + (margin ? this.margins.y : 0);
    }

    public float getBottom(boolean abs, boolean margin) {
        return (abs ? this.getAbsPos().y : this.getPos().y) - this.getVOff() + this.getHeight(false)
                - (margin ? this.margins.y : 0);
    }

    // topmost point relative to parent before childoffset
    public float getChildLocalTop(float offset) {
        float y = -this.getVOff() + offset;
        for (UIElement u : this.getChildren()) {
            if (u.isVisible()) {
                float childTop = u.getChildLocalTop(offset + this.getVAlignOffset() + u.getPos().y);
                if (u.clip) {
                    childTop = -u.getVOff() + u.getPos().y + offset;
                }
                y = Math.min(y, childTop);
            }
        }
        return y;
    }

    public float getChildLocalBottom(float offset) {
        float y = this.getHeight(false) - this.getVOff() + offset;
        for (UIElement u : this.getChildren()) {
            if (u.isVisible()) {
                float childBottom = u.getChildLocalBottom(offset + this.getVAlignOffset() + u.getPos().y);
                if (u.clip) {
                    childBottom = u.getHeight(false) - u.getVOff() + u.getPos().y + offset;
                }
                y = Math.max(y, childBottom);
            }
        }
        return y;
    }

    // get a width value in terms of relpos coordinates
    public float getWidthInRel(float absWidth) {
        return absWidth / this.getWidth(true);
    }

    public float getHeightInRel(float absHeight) {
        return absHeight / this.getHeight(true);
    }

    public Vector2f getPosOfRel(Vector2f relpos) {
        if (this.parent == null) {
            return new Vector2f(relpos.x * Config.WINDOW_WIDTH, relpos.y * Config.WINDOW_HEIGHT);
        } else {
            return this.parent.getLocalPosOfRel(relpos);
        }
    }

    public Vector2f getRelOfPos(Vector2f pos) {
        if (this.parent == null) {
            return new Vector2f(pos.x / Config.WINDOW_WIDTH, pos.y / Config.WINDOW_HEIGHT);
        } else {
            return new Vector2f(pos.x / this.parent.getWidth(true),
                    pos.y / this.parent.getHeight(true));
        }
    }

    public Vector2f getPosOfAbs(Vector2f abs) {
        if (this.parent == null) {
            return new Vector2f(abs.x - Config.WINDOW_WIDTH / 2, abs.y - Config.WINDOW_HEIGHT / 2);
        }
        return this.parent.getLocalPosOfAbs(abs);
    }

    public Vector2f getAbsOfPos(Vector2f pos) {
        if (this.parent != null) {
            return this.parent.getAbsPosOfLocal(pos);
        }
        return new Vector2f(pos.x + Config.WINDOW_WIDTH / 2, pos.y + Config.WINDOW_HEIGHT / 2);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible && this.hasFocus) {
            this.ui.focusElement(this.parent);
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return this.alpha;
    }

    // do not override this shit
    public final void alert(String strarg, int... intarg) {
        if (this.parent != null) {
            this.parent.onAlert(strarg, intarg);
        } else {
            this.ui.alertListeners(strarg, intarg);
        }
    }

    // override this shit tho
    @Override
    public void onAlert(String strarg, int... intarg) {
        this.alert(strarg, intarg);
    }

    // normalize position relative to position and scale of this ui element
    public Vector2f getLocalPosOfAbs(Vector2f absPos) {
        return new Vector2f(absPos.x - this.getAbsPos().x - this.getHAlignOffset(),
                absPos.y - this.getAbsPos().y - this.getVAlignOffset());
    }

    public Vector2f getLocalPosOfRel(Vector2f relPos) {
        return new Vector2f(relPos.x * this.getWidth(true),
                relPos.y * this.getHeight(true));
    }

    public Vector2f getAbsPosOfLocal(Vector2f localPos) {
        return this.getAbsPos().add(this.childoffset).add(localPos)
                .add(new Vector2f(this.getHAlignOffset(), this.getVAlignOffset()));
    }

    public boolean pointIsInHitbox(float x, float y) {
        if (this.hitcircle) {
            return (new Vector2f(
                    (x - this.getAbsPos().x + this.getWidth(false) / 2 - this.getHOff())
                            / this.getWidth(false),
                    (y - this.getAbsPos().y + this.getHeight(false) / 2 - this.getVOff())
                            / this.getHeight(false)).lengthSquared()) < 0.25;
        }
        return x >= this.getLeft(true, false) && x <= this.getRight(true, false)
                && y >= this.getTop(true, false) && y <= this.getBottom(true, false);
    }

    public void fitInParent() {
        float x = this.getPos().getX(), y = this.getPos().getY();
        if (this.parent == null) { // is parent
            x = Math.max(x, -Config.WINDOW_WIDTH / 2 + this.getHOff());
            x = Math.min(x, Config.WINDOW_WIDTH / 2 - this.getHOff() + this.getWidth(false));
            y = Math.max(y, -Config.WINDOW_HEIGHT / 2 + this.getVOff());
            y = Math.min(y, Config.WINDOW_HEIGHT / 2 - this.getVOff() + this.getHeight(false));
        } else { // has parent
            x = Math.max(x, -this.parent.getWidth(true) / 2 + this.getHOff());
            x = Math.min(x, this.parent.getWidth(true) / 2 - this.getHOff() + this.getWidth(false));
            y = Math.max(y, -this.parent.getWidth(true) / 2 + this.getVOff());
            y = Math.min(y, this.parent.getWidth(true) / 2 - this.getVOff() + this.getHeight(false));
        }
        this.setPos(new Vector2f(x, y), 1);
    }

    public void update(double frametime) {
        if (this.animation != null) {
            this.animation.update(frametime);
        }
        if (this.followTarget != null) {
            this.setAbsPos(this.followTarget.getAbsPos(), this.speed);
        }
        Vector2f delta = this.targetpos.copy().sub(this.pos);
        if (delta.length() > EPSILON) {
            float ratio = 1 - (float) Math.pow(1 - this.speed, frametime);
            this.pos.add(delta.scale(ratio));
        } else {
            this.pos.set(this.targetpos);
        }

        this.updateRelationships();
        for (UIElement u : this.getChildren()) {
            u.update(frametime);
        }
        if (this.updateZOrder) {
            Collections.sort(this.children);
            this.updateZOrder = false;
        }
    }

    public void followElement(UIElement target, double speed) {
        this.followTarget = target;
        this.speed = speed;
    }

    public void stopFollowing() {
        this.followTarget = null;
    }

    public void debugPrint(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("|");
        }
        System.out.println(this.debug());
        for (UIElement u : this.getChildren()) {
            u.debugPrint(depth + 1);
        }
    }

    public String debug() {
        return this.getClass().toString().substring("class client.ui.".length());
    }

    public void draw(Graphics g) {
        if (this.animation != null) {
            this.finalImage = this.animation.getCurrentFrame().getScaledCopy(this.scale);
            this.finalImage.rotate(this.angle);
            this.finalImage.setAlpha(this.alpha);
            if (this.visible) {
                g.drawImage(this.finalImage, this.getLeft(true, false), this.getTop(true, false), this.getColor());
            }
        }
        if (this.visible) {
            this.drawChildren(g);
        }
    }

    public void drawChildren(Graphics g) {
        Rectangle prevClip = g.getClip(); // did u know that the rectangle returned is mutable by future calls to setClip
        Rectangle prevClipCloned = null;
        if (this.clip) {
            if (prevClip != null) {
                prevClipCloned = new Rectangle(prevClip.getX(), prevClip.getY(), prevClip.getWidth(), prevClip.getHeight());
                int left = (int) Math.max(prevClip.getMinX(), this.getLeft(true, true));
                int top = (int) Math.max(prevClip.getMinY(), this.getTop(true, true));
                int right = (int) Math.min(prevClip.getMaxX(), this.getRight(true, true));
                int bot = (int) Math.min(prevClip.getMaxY(), this.getBottom(true, true));
                if (bot < top || right < left) {
                    return;
                }
                g.setClip(left, top, right - left, bot - top);
            } else {
                g.setClip((int) (this.getLeft(true, true)),
                        (int) (this.getTop(true, true)),
                        (int) this.getWidth(true),
                        (int) this.getHeight(true));
            }
        }
        for (UIElement u : this.getChildren()) {
            u.draw(g);

        }
        if (prevClipCloned != null) {
            g.setClip(prevClipCloned);
        } else {
            g.setClip(prevClip);
        }
    }

    public void setZ(int z) {
        if (z != this.z) {
            this.z = z;
            if (this.parent != null) {
                this.parent.updateZOrder = true;
            } else {
                this.ui.updateZOrder = true;
            }
        }
    }

    public int getZ() {
        return this.z;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    // returns the uielement that is top (prioritizes children)
    public UIElement topChildAtPos(float x, float y, boolean requirehitbox, boolean requirescrollable,
            boolean requiredraggable) {
        if (!this.visible) {
            return null;
        }
        UIElement u = null;

        if ((!this.ignorehitbox || !requirehitbox) && (this.scrollable || !requirescrollable)
                && (this.draggable || !requiredraggable)) {
            if (this.pointIsInHitbox(x, y)) {
                u = this;
            }
        }
        // not in hitbox and we are clipping
        if (!this.pointIsInHitbox(x, y) && this.clip) {
            return null;
        }
        for (UIElement child : this.children) {
            UIElement thing = child.topChildAtPos(x, y, requirehitbox, requirescrollable, requiredraggable);
            if (thing != null) {
                u = thing;
            }
        }
        return u;
    }

    // PARENTING //////////////////////////
    // massive copy paste fiesta down below
    public List<UIElement> getChildren() {
        List<UIElement> allchildren = new LinkedList<>();
        allchildren.addAll(this.children);
        allchildren.addAll(this.childrenAddBuffer);
        allchildren.removeAll(this.childrenRemoveBuffer);
        return allchildren;

    }

    public void setParent(UIElement parent) { // should only be run once per
                                                // thing
        this.parent = parent;
        if (!parent.children.contains(this) && !parent.childrenAddBuffer.contains(this)) {
            parent.childrenAddBuffer.add(this);
            parent.updateZOrder = true;
        }
    }

    public UIElement getParent() {
        return this.parent;
    }

    public void addChild(UIElement child) {
        child.setParent(this);
    }

    /**
     * Removes the parent of the UIElement, handles the removing of the connection
     * between parent and child by removing itself from the parent's children and by
     * removing its parent object
     */
    public void removeParent() {
        if (this.parent != null) {
            // this.setPos(this.parent.getFinalPos().add(this.pos), 1);
            this.parent.childrenRemoveBuffer.add(this);
        } else {
            this.ui.removeUIElementParent(this);
        }
        this.parent = null;
    }

    public void updateRelationships() {
        this.children.addAll(this.childrenAddBuffer);
        this.children.removeAll(this.childrenRemoveBuffer);
        this.childrenAddBuffer.clear();
        this.childrenRemoveBuffer.clear();
    }

    /**
     * Removes a child of an UIElement
     * 
     * @param child The child to remove
     */
    public void removeChild(UIElement child) {
        if (this.getChildren().contains(child)) {
            child.removeParent();
        }
    }

    public void removeChildren(List<? extends UIElement> children) {
        for (UIElement child : children) {
            this.removeChild(child);
        }
    }

    public void removeChildren() {
        for (UIElement u : this.getChildren()) {
            u.removeParent();
        }
    }

    public boolean isChildOf(UIElement other) {
        if (this.parent == null) {
            return false;
        }
        if (this.parent == other) {
            return true;
        }
        return this.parent.isChildOf(other);
    }

    @Override
    public int compareTo(UIElement uie) {
        return this.z - uie.z;
    }

}
