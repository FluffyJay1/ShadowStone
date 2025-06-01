package client.ui.game.visualboardanimation.eventanimation.damage;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import client.Config;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UICard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.color.LinearColorInterpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.ComposedListInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ClampedInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.interpolation.vector.LinearVectorInterpolation;
import server.card.Minion;
import server.event.EventDamage;

public class EventAnimationDamageGasterBlaster extends EventAnimationDamage {
    private List<GasterBlaster> blasters; 

    public EventAnimationDamageGasterBlaster() {
        super(0.5, true);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.blasters = new ArrayList<>(event.m.size());
    }

    @Override
    public void onStart() {
        float y = this.event.cardSource.team == this.visualBoard.getLocalteam() ? Config.WINDOW_HEIGHT + 150 : -150;
        float targetY = Config.WINDOW_HEIGHT * (0.5f + 0.3f * this.event.cardSource.team * this.visualBoard.getLocalteam() + (float) (Math.random() - 0.5) * 0.1f);
        for (Minion m : this.event.m) {
            float targetX = (float) ((Math.random() - 0.5) * 0.75 + 0.5) * Config.WINDOW_WIDTH;
            float x = targetX + (float) (Math.random() - 0.5) * Config.WINDOW_WIDTH * 0.5f;
            Vector2f targetPos = new Vector2f(targetX, targetY);
            double expectedAngle = m.uiCard.getAbsPos().copy().sub(targetPos).getTheta();
            double startAngle = expectedAngle + (float) (Math.random() - 0.5) * 180;
            GasterBlaster blaster = new GasterBlaster(new Vector2f(x, y), targetPos, startAngle, m.uiCard);
            this.blasters.add(blaster);
        }
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        for (GasterBlaster gb : this.blasters) {
            gb.update(frametime);
        }
    }

    @Override
    public void draw(Graphics g) {
        for (GasterBlaster gb : this.blasters) {
            gb.drawBeamThing(g);
        }
        for (GasterBlaster gb : this.blasters) {
            gb.drawBlaster(g);
        }
        if (!this.isPre()) {
            this.drawDamageNumber(g);
        }
    }

    private class GasterBlaster {
        private static final double BEAM_WIDTH_JITTER_INTERVAL = 0.03;
        private static final double BLASTER_FRAME_INTERVAL = 0.03;
        private static final float IMAGE_SCALE = 2.5f;
        private static final Interpolation<Double> BEAM_WIDTH = new SequentialInterpolation<>(List.of(
                new QuadraticInterpolationB(50, 200, 100),
                new ConstantInterpolation(200),
                new QuadraticInterpolationB(200, 0, -100)
        ), List.of(0.1, 0.5, 0.4));
        private static final Interpolation<Color> BEAM_COLOR = new ComposedInterpolation<>(new ClampedInterpolation(0.5, 1), new LinearColorInterpolation(
                new Color(1.0f, 1.0f, 1.0f, 1.0f),
                new Color(1.0f, 1.0f, 1.0f, 0f)
        ));
        Interpolation<Vector2f> posPreInterpolation, posPostInterpolation;
        Interpolation<Double> angleInterpolation;
        double startAngle, beamWidthJitterTimer, beamWidthJitterValue, animationTimer;
        UICard targetMinion;
        Vector2f targetPos, beamTarget;
        Animation animation;

        public GasterBlaster(Vector2f startPos, Vector2f targetPos, double startAngle, UICard targetMinion) {
            this.posPreInterpolation = new ComposedInterpolation<>(new ComposedListInterpolation(List.of(new ClampedInterpolation(0, 0.5), new QuadraticInterpolationA(0, 1, -1))), new LinearVectorInterpolation(startPos, targetPos));
            this.targetPos = targetPos;
            this.angleInterpolation = new ClampedInterpolation(0, 0.5);
            this.startAngle = startAngle;
            this.animation = new Animation("animation/gasterblaster.png", new Vector2f(6, 1), 0, 0, Image.FILTER_NEAREST);
            this.targetMinion = targetMinion;
            this.beamWidthJitterTimer = 0;
            this.beamWidthJitterValue = 1;
            this.animationTimer = 0;
        }

        public void update(double frametime) {
            if (!isPre()) {
                this.beamWidthJitterTimer += frametime;
                if (this.beamWidthJitterTimer >= BEAM_WIDTH_JITTER_INTERVAL) {
                    this.beamWidthJitterValue = Math.random() * 0.4 + 0.8;
                    this.beamWidthJitterTimer %= BEAM_WIDTH_JITTER_INTERVAL;
                }
                this.animationTimer += frametime;
                int frame = (int) (this.animationTimer / BLASTER_FRAME_INTERVAL) + 1;
                if (frame >= 4) {
                    frame = 4 + frame % 2;
                }
                this.animation.setFrame(frame);
            }
        }

        public void drawBeamThing(Graphics g) {
            if (!isPre()) {
                if (this.beamTarget == null) {
                    // lock in a target angle
                    Vector2f minionPos = targetMinion.getAbsPos();
                    double targetAngle = minionPos.copy().sub(this.targetPos).getTheta();
                    Vector2f beamDiff = new Vector2f(targetAngle).scale(Config.WINDOW_WIDTH + Config.WINDOW_HEIGHT); // far enough lol
                    this.beamTarget = minionPos.copy().add(beamDiff);
                    this.posPostInterpolation = new ComposedInterpolation<>(
                        new SequentialInterpolation<>(List.of(
                            new LinearInterpolation(0, 0.05),
                            new QuadraticInterpolationB(0.05, 1, 0.1)
                        ), List.of(0.4, 0.6)),
                        new LinearVectorInterpolation(this.targetPos, this.targetPos.copy().sub(beamDiff.copy().scale(0.25f)))
                    );
                }
                Vector2f pos = this.posPostInterpolation.get(normalizedPost());
                double angle = this.beamTarget.copy().sub(pos).getTheta();
                double width = BEAM_WIDTH.get(normalizedPost()) * this.beamWidthJitterValue;
                Vector2f beamStartPos = pos.copy().add(new Vector2f(angle).scale((float) width));
                Color color = BEAM_COLOR.get(normalizedPost());
                drawBeam(g, width, color, beamStartPos, this.beamTarget);
            }
        }

        public void drawBlaster(Graphics g) {
            if (isPre()) {
                Vector2f pos = this.posPreInterpolation.get(normalizedPre());
                double targetAngle = targetMinion.getAbsPos().copy().sub(pos).getTheta();
                double angle = this.startAngle + this.angleInterpolation.get(normalizedPre()) * (targetAngle - this.startAngle);
                Image image = this.animation.getCurrentFrame();
                drawCenteredAndScaled(g, image, pos, IMAGE_SCALE, 1, (float) angle - 90);
            } else {
                Vector2f pos = this.posPostInterpolation.get(normalizedPost());
                double angle = this.beamTarget.copy().sub(pos).getTheta();
                Image image = this.animation.getCurrentFrame();
                drawCenteredAndScaled(g, image, pos, IMAGE_SCALE, 1, (float) angle - 90);
            }
        }
    }
}
