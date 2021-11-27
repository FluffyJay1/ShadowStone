package client.ui.particle;

import client.ui.Animation;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

public class Particle {
    // basically a bean
    public double time, maxTime;
    public double angle, angleVel;
    public double velscale; // how much velocity gets scaled per second (like drag)
    public Interpolation<Double> opacityInterpolation;
    public Interpolation<Double> scaleInterpolation;
    public Vector2f pos, vel, accel;
    public Animation animation;

    public Particle() {
        this.time = 0;
        this.angle = 0;
        this.angleVel = 0;
        this.opacityInterpolation = new ConstantInterpolation(1);
        this.scaleInterpolation = new ConstantInterpolation(1);
        this.pos = new Vector2f();
        this.vel = new Vector2f();
        this.accel = new Vector2f();
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
        image.rotate((float) this.angle);
        image.setAlpha(this.opacityInterpolation.get(this.normalizedTime()).floatValue());
        float scale = this.scaleInterpolation.get(this.normalizedTime()).floatValue();
        float scaledWidth = scale * image.getWidth();
        float scaledHeight = scale * image.getHeight();
        Vector2f topleft = new Vector2f(-scaledWidth / 2, -scaledHeight / 2).add(pos).add(parentAbsPos);
        /*
        This looks dumb but I will explain:
        To draw a rotated image, I have to set the rotation in the Image, fair enough
        To draw a scaled image, I can get a scaled copy of the image, but the dimensions get rounded,
        which looks jittery when scale is being interpolated
        So alternatively I scale it in the draw call, which makes interpolation smooth like we want
        HOWEVER when it tries to draw the rotated image this way, it considers the center of rotation as if
        it wasn't scaled, so rotation + scaling causes the center of rotation to not align
        here we do some dumb compensation to adjust for the fucked center of rotation
         */
        double rad = this.angle * Math.PI / 180;
        double widthDiff = image.getWidth() - scaledWidth;
        double heightDiff = image.getHeight() - scaledHeight;
        Vector2f dumbCompensation = new Vector2f((float) (widthDiff / 2 * (1 - Math.cos(rad)) + heightDiff / 2 * Math.sin(rad)),
                (float) (widthDiff / 2 * -Math.sin(rad) + heightDiff / 2 * (float) (1 - Math.cos(rad))));
        Vector2f actual = topleft.copy().sub(dumbCompensation);
        g.drawImage(image, actual.x, actual.y, actual.x + scaledWidth, actual.y + scaledHeight, 0, 0, image.getWidth(), image.getHeight());
    }
}
