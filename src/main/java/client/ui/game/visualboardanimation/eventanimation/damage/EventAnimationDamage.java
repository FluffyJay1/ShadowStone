package client.ui.game.visualboardanimation.eventanimation.damage;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.Minion;
import server.event.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
    public void init(VisualBoard b, EventDamage event) {
        this.visualBoard = b;
        this.event = event;
        if (this.requireNonEmpty && event.m.isEmpty()) {
            this.preTime = 0;
            this.postTime = 0;
            return;
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

    static void drawCenteredAndScaled(Graphics g, Image image, Vector2f pos, float scale, float alpha) {
        Image scaledCopy = image.getScaledCopy(scale);
        scaledCopy.setAlpha(alpha);
        g.drawImage(scaledCopy, pos.x - scaledCopy.getWidth() / 2, pos.y - scaledCopy.getHeight() / 2);
    }

    // so serverboard have no use for animations, so they should not reflect
    // so in this string we encode the number of tokens that they should skip
    public static String stringOrNull(EventAnimationDamage ed) {
        if (ed == null) {
            return "null ";
        }
        return ed.toString();
    }

    public String toString() {
        String s = this.extraParamString();
        int numTokens = new StringTokenizer(s).countTokens() + 1;
        return numTokens + " " + this.getClass().getName() + " " + s;
    }

    // override this
    public String extraParamString() {
        return "";
    }

    // if animation info is stored in a string, and that string gets serialized
    // as part of a larger object, when deserializing that object this will
    // retrieve the animation string and not actually do reflection to create
    // the animation
    public static String extractAnimationString(StringTokenizer st) {
        String firstToken = st.nextToken();
        if (firstToken.equals("null")) {
            return "null ";
        }
        int numTokens = Integer.parseInt(firstToken);
        StringBuilder sb = new StringBuilder(firstToken).append(" ");
        for (int i = 0; i < numTokens; i++) {
            sb.append(st.nextToken()).append(" ");
        }
        return sb.toString();
    }

    // so much catch
    public static EventAnimationDamage fromString(StringTokenizer st) {
        String firstToken = st.nextToken();
        if (firstToken.equals("null")) {
            return new EventAnimationDamageDefault();
        }
        String className = st.nextToken();
        try {
            Class<? extends EventAnimationDamage> edclass = Class.forName(className).asSubclass(EventAnimationDamage.class);
            try {
                return (EventAnimationDamage) edclass.getMethod("fromExtraParams", StringTokenizer.class).invoke(null, st);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // assume it has the default constructor then
                return edclass.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
