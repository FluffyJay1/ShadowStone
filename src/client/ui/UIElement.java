package client.ui;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import utils.DefaultMouseListener;

public class UIElement implements DefaultMouseListener {
	public static final double EPSILON = 0.0001;

	UI ui;
	UIElement parent = null;
	ArrayList<UIElement> children = new ArrayList<UIElement>();
	ArrayList<UIElement> childrenAddBuffer = new ArrayList<UIElement>();
	ArrayList<UIElement> childrenRemoveBuffer = new ArrayList<UIElement>();

	public boolean remove = false, hide = false, draggable = false, ignorehitbox = false;
	private Vector2f targetpos = new Vector2f(), pos = new Vector2f(); // private
																		// cuz
																		// fuck
																		// you
	public double scale = 1, speed = 1, angle = 0;
	Image image, finalImage;

	public UIElement(UI ui, Vector2f pos, String imagepath) {
		this.ui = ui;
		this.pos = pos.copy();
		this.targetpos = pos.copy();
		this.setImage(imagepath);
	}

	public void setImage(String imagepath) {
		if (imagepath != null && !imagepath.isEmpty()) {
			this.image = Game.getImage(imagepath);
		}
	}

	public void setRemove(boolean state) {
		this.remove = state;
		for (UIElement u : this.getChildren()) {
			u.setRemove(state);
		}
	}

	public void setPos(Vector2f pos, double speed) {
		this.targetpos.set(pos);
		this.speed = speed;
		if (speed == 1) {
			this.pos.set(pos);
		}
	}

	public Vector2f getPos() {
		return this.pos;
	}

	public Vector2f getFinalPos() {
		if (this.parent != null) {
			return this.parent.getFinalPos().copy().add(this.pos);
		}
		return this.pos;
	}

	public double getWidth() {
		return this.finalImage.getWidth();
	}

	public double getHeight() {
		return this.finalImage.getHeight();
	}

	public boolean pointIsInHitbox(Vector2f pos) {
		return pos.getX() >= this.getFinalPos().getX() - this.getWidth() / 2
				&& pos.getX() <= this.getFinalPos().getX() + this.getWidth() / 2
				&& pos.getY() >= this.getFinalPos().getY() - this.getHeight() / 2
				&& pos.getY() <= this.getFinalPos().getY() + this.getHeight() / 2;
	}

	public void update(double frametime) {
		Vector2f delta = this.targetpos.copy().sub(this.pos);
		if (delta.length() > EPSILON) {
			float ratio = 1 - (float) Math.pow(1 - this.speed, frametime);
			this.pos.add(delta.scale(ratio));
		}
		this.updateRelationships();
		for (UIElement u : this.getChildren()) {
			u.update(frametime);
		}
	}

	public void draw(Graphics g) {
		if (this.image != null) {
			this.finalImage = this.image.getScaledCopy((float) this.scale);
			this.finalImage.rotate((float) this.angle);
			if (!this.hide) {
				g.drawImage(this.finalImage, (float) (this.getFinalPos().x - this.finalImage.getWidth() / 2),
						(float) (this.getFinalPos().y - this.finalImage.getHeight() / 2));
				for (UIElement u : this.getChildren()) {
					u.draw(g);
				}
			}
		}
	}

	// returns the uielement that is top (prioritizes children)
	public UIElement topChildAtPos(Vector2f pos) {
		if (this.hide) {
			return null;
		}
		UIElement u = null;

		if (!this.ignorehitbox && this.pointIsInHitbox(pos)) {
			u = this;
		}
		for (UIElement child : this.children) {
			UIElement thing = child.topChildAtPos(pos);
			if (thing != null) {
				u = thing;
			}
		}
		return u;
	}

	// PARENTING //////////////////////////
	// massive copy paste fiesta down below
	public ArrayList<UIElement> getChildren() {
		ArrayList<UIElement> allchildren = new ArrayList<UIElement>();
		allchildren.addAll(this.children);
		allchildren.addAll(this.childrenAddBuffer);
		allchildren.removeAll(this.childrenRemoveBuffer);
		return allchildren;

	}

	public void setParent(UIElement parent) { // should only be run once per
												// thing
		this.parent = parent;
		if (parent.children.contains(this) == false && parent.childrenAddBuffer.contains(this) == false) {
			parent.childrenAddBuffer.add(this);
		}
	}

	public void addChild(UIElement child) {
		child.setParent(this);
	}

	/**
	 * Removes the parent of the UIElement, handles the removing of the
	 * connection between parent and child by removing itself from the parent's
	 * children and by removing its parent object
	 */
	public void removeParent() {
		if (this.parent != null) {
			this.setPos(this.parent.getFinalPos().add(this.pos), 1);
			this.parent.childrenRemoveBuffer.add(this);
		}
		this.parent = null;
	}

	public void updateRelationships() {
		// this.origin = this.parent.getFinalLoc();
		/*
		 * for(UIElement u : this.children) { if(u.getRemove()) {
		 * this.childrenRemoveBuffer.add(u); } }
		 */
		if (this.parent != null) {
			if (this.parent.remove) {
				this.setRemove(true);
			}
		}
		this.children.addAll(this.childrenAddBuffer);
		this.children.removeAll(this.childrenRemoveBuffer);
		this.childrenAddBuffer.clear();
		this.childrenRemoveBuffer.clear();
	}

	/**
	 * Removes a child of an UIElement
	 * 
	 * @param child
	 *            The child to remove
	 */
	public void removeChild(UIElement child) {
		if (this.getChildren().contains(child)) {
			child.removeParent();
		}
	}

	public void removeChildren() {
		for (UIElement u : this.getChildren()) {
			u.removeParent();
		}
	}
}
