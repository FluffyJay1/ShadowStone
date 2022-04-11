package client.ui.game.visualboardanimation.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.Minion;
import server.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public class EventAnimationDamage extends EventAnimation<EventDamage> {
    /*
     * So this is weird. If the EventDamage has a source, we want to draw the source
     * effect shooting something at the victims. However, if the source is null,
     * there is nothing to do the shooting so we just straight up damage it. If the
     * EventDamage had a source, then the VisualBoard should've used that effect's
     * special animation for damage, and that special animation doesn't have to
     * consider the null source case. Since this animation serves as a default, it
     * is the only class that has to handle both cases.
     */
    public EventAnimationDamage() {
        this(-1, 0.5);
    }

    public EventAnimationDamage(double preTime) {
        this(preTime, 0.5);
    }

    public EventAnimationDamage(double preTime, double postTime) {
        super(preTime, postTime);
    }

    // precompute
    List<Vector2f> dirs;
    List<Double> anglesRad;

    @Override
    public void init(VisualBoard b, EventDamage event) {
        this.visualBoard = b;
        this.event = event;
        if (this.preTime == -1) {
            this.preTime = 0.25;
        }
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
        if (this.isPre()) {
            // do the shooting
            g.setColor(Color.red);
            for (int i = 0; i < this.event.m.size(); i++) {
                Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                        .sub(this.event.cardSource.uiCard.getAbsPos()).scale((float) (this.normalizedPre()))
                        .add(this.event.cardSource.uiCard.getAbsPos());
                g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
            }
            g.setColor(Color.white);
        } else {
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
            String dstring = this.event.damage.get(i) + "";
            g.drawString(dstring, this.event.m.get(i).uiCard.getAbsPos().x - font.getWidth(dstring) / 2,
                    this.event.m.get(i).uiCard.getAbsPos().y - font.getHeight(dstring) + yoff);
        }
        g.setColor(Color.white);
    }

    public void drawProjectile(Graphics g, Image projectile) {
        for (int i = 0; i < this.event.m.size(); i++) {
            projectile.setRotation((float) (this.anglesRad.get(i) * 180 / Math.PI));
            Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                    .sub(this.event.cardSource.uiCard.getAbsPos()).scale((float) (this.normalizedPre()))
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

    public static String nameOrNull(Class<? extends EventAnimationDamage> animation) {
        return animation == null ? "null " : animation.getName() + " ";
    }

    public static Class<? extends EventAnimationDamage> fromString(String name) {
        if (name.equals("null")) {
            return null;
        }
        try {
            return Class.forName(name).asSubclass(EventAnimationDamage.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
