package client.ui.game.visualboardanimation.eventanimation.damage;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.Minion;
import server.event.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public abstract class EventAnimationDamage extends EventAnimation<EventDamage> {
    /*
     * If subclasses have no additional parameters, all they need to implement
     * the () constructor, nothing else is needed. If the animation requires
     * some parameters, they will need to implement a
     * extraParamString() and fromExtraParams(StringTokenizer) method.
     */
    // precompute
    List<Vector2f> dirs;
    List<Double> anglesRad;
    private final boolean requireNonEmpty;

    public EventAnimationDamage(double preTime, boolean requireNonEmpty) {
        this(preTime, 0.5, requireNonEmpty);
    }

    public EventAnimationDamage(double preTime, double postTime, boolean requireNonEmpty) {
        super(preTime, postTime);
        this.requireNonEmpty = requireNonEmpty;
    }

    @Override
    public boolean shouldAnimate() {
        return !requireNonEmpty || !event.m.isEmpty();
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.dirs = new ArrayList<>(event.m.size());
        this.anglesRad = new ArrayList<>(event.m.size());
        for (Minion m : event.m) {
            Vector2f diff = m.uiCard.getAbsPos().copy().sub(event.cardSource.uiCard.getAbsPos());
            double rad = Math.atan2(diff.y, diff.x);
            this.dirs.add(diff);
            this.anglesRad.add(rad);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!this.isPre()) {
            this.drawDamageNumber(g);
        }
    }

    public void drawDamageNumber(Graphics g) {
        // show the damage number thing
        g.setColor(Color.red);
        UnicodeFont font = Game.getFont(96, true, false);
        g.setFont(font);
        float yoff = (float) (Math.min(Math.pow(0.5 - 2 * this.normalizedPost(), 2), 0.25) * 100);
        for (int i = 0; i < this.event.m.size(); i++) {
            String dstring = this.event.actualDamage.get(i) + "";
            g.drawString(dstring, this.event.m.get(i).uiCard.getAbsPos().x - font.getWidth(dstring) / 2,
                    this.event.m.get(i).uiCard.getAbsPos().y - font.getHeight(dstring) + yoff);
        }
        g.setColor(Color.white);
    }

    public Vector2f interpProjectile(Vector2f targetPos, float time) {
        return targetPos.sub(this.event.cardSource.uiCard.getPos()).scale(time)
                .add(this.event.cardSource.uiCard.getPos());
    }

    public void drawProjectile(Graphics g, Image projectile, float time) {
        for (int i = 0; i < this.event.m.size(); i++) {
            projectile.setRotation((float) (this.anglesRad.get(i) * 180 / Math.PI));
            Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                    .sub(this.event.cardSource.uiCard.getAbsPos()).scale(time)
                    .add(this.event.cardSource.uiCard.getAbsPos());
            g.drawImage(projectile, pos.x - projectile.getWidth()/2, pos.y - projectile.getHeight()/2);
        }
    }

    public void drawBeam(Graphics g, double width, Color color) {
        g.setDrawMode(Graphics.MODE_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        for (int i = 0; i < this.event.m.size(); i++) {
            Minion m = this.event.m.get(i);
            Vector2f start = this.event.cardSource.uiCard.getAbsPos();
            Vector2f end = m.uiCard.getAbsPos();
            float distance = start.distance(end);
            Shape r = new Rectangle(-distance/2, -(float) width/2, distance, (float) width); //rounded rectangle is buggy af
            // macgyver our own rounded rectangle
            r = r.union(new Circle(-distance/2, 0, (float) width/2))[0];
            r = r.union(new Circle(distance/2, 0, (float) width/2))[0];
            Transform t = Transform.createTranslateTransform((start.x + end.x) / 2, (start.y + end.y) / 2)
                    .concatenate(Transform.createRotateTransform(this.anglesRad.get(i).floatValue()));
            g.setColor(color);
            g.fill(r.transform(t));
        }
        g.setDrawMode(Graphics.MODE_NORMAL);
    }

    static void drawCenteredAndScaled(Graphics g, Image image, Vector2f pos, float scale, float alpha) {
        Image scaledCopy = image.getScaledCopy(scale);
        scaledCopy.setAlpha(alpha);
        g.drawImage(scaledCopy, pos.x - scaledCopy.getWidth() / 2, pos.y - scaledCopy.getHeight() / 2);
    }
}
