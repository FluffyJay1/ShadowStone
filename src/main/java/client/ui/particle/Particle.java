package client.ui.particle;

import client.ui.Animation;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public class Particle {
    // basically a bean
    public double time, maxTime;
    public double angle, angleVel;
    public double velscale; // how much velocity gets scaled per second (like drag)
    public Interpolation<Double> opacityInterpolation;
    public Interpolation<Double> scaleInterpolation;
    public Vector2f pos, vel, accel;
    public Animation animation;
    public int drawMode;

    public Particle() {
        this.time = 0;
        this.angle = 0;
        this.angleVel = 0;
        this.opacityInterpolation = new ConstantInterpolation(1);
        this.scaleInterpolation = new ConstantInterpolation(1);
        this.pos = new Vector2f();
        this.vel = new Vector2f();
        this.accel = new Vector2f();
        this.drawMode = Graphics.MODE_NORMAL;
    }

    public void update(double frametime) {
        this.time += frametime;
        this.pos.add(this.vel.copy().scale((float) frametime));
        this.vel.add(this.accel.copy().scale((float) frametime));
        this.vel.scale((float) Math.pow(this.velscale, frametime));
        this.angle = (this.angle + this.angleVel * frametime) % 360;
        this.animation.update(frametime);
    }

    public double normalizedTime() {
        return this.time / this.maxTime;
    }

    public void draw(Graphics g, Vector2f parentAbsPos) {
        Image image = this.animation.getCurrentFrame();
        image.setAlpha(this.opacityInterpolation.get(this.normalizedTime()).floatValue());
        float scale = this.scaleInterpolation.get(this.normalizedTime()).floatValue();
        float scaledWidth = scale * image.getWidth();
        float scaledHeight = scale * image.getHeight();
        image.setCenterOfRotation(scaledWidth/2, scaledHeight/2);
        image.rotate((float) this.angle);
        Vector2f topleft = new Vector2f(-scaledWidth / 2, -scaledHeight / 2).add(pos).add(parentAbsPos);
        g.setDrawMode(this.drawMode);
        // the boys over at slick got the blending equations wrong, they don't factor in alpha correctly
        if (this.drawMode == Graphics.MODE_NORMAL) {
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE_MINUS_DST_ALPHA, GL_ONE);
        } else if (this.drawMode == Graphics.MODE_ADD) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        }
        g.drawImage(image, topleft.x, topleft.y, topleft.x + scaledWidth, topleft.y + scaledHeight, 0, 0, image.getWidth(), image.getHeight());
        g.setDrawMode(Graphics.MODE_NORMAL);
    }
}
