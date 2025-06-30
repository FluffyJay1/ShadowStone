package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Config;
import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.color.ConstantColorInterpolation;
import client.ui.interpolation.color.LinearColorInterpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ClampedInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import server.event.EventDamage;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

// explosion
public class EventAnimationDamageAOEMegumin extends EventAnimationDamage {
    private static final int NUM_RINGS = 7;
    private static final int RING_OFFSET = 150;
    private static final int RING_OFFSET_VARIATION = 100;
    private static final double RING_SPAWN_TIME = 0.78;
    private static final double RING_COLLAPSE_TIMESTAMP = 0.8;
    private static final double RING_COLLAPSE_DIFF = 0.01;
    private static final double RING_MIN_SCALE = 1;
    private static final double RING_SCALE_VARIATION = 1;
    private static final double RING_Y_SCALE = 0.5;
    private static final Interpolation<Double> RING_SCALE_INTERPOLATION = new ComposedInterpolation<>(new ClampedInterpolation(0, 0.1), new QuadraticInterpolationB(0, 1, 2));
    private static final Interpolation<Double> RING_COLLAPSE_INTERPOLATION = new ComposedInterpolation<>(new ClampedInterpolation(0, 0.5), new QuadraticInterpolationB(1, 0, 1.5));
    private static final Interpolation<Double> RING_ROTATION_INTERPOLATION = new SequentialInterpolation<>(List.of(
            new QuadraticInterpolationB(0, 240, 460),
            new LinearInterpolation(240, 300)
    ), List.of(0.1, 0.9));
    private static final Interpolation<Double> FADE_ALPHA = new SequentialInterpolation<>(List.of(
        new QuadraticInterpolationB(0, 0.4, 1),
        new ConstantInterpolation(0.4),
        new QuadraticInterpolationB(0.4, 0.93, 0),
        new ConstantInterpolation(0.93)
    ), List.of(0.4, 0.52, 0.01, 0.07));
    private static final Interpolation<Double> FLASHBANG_ALPHA = new ComposedInterpolation<>(new ClampedInterpolation(0, 0.8), new QuadraticInterpolationB(0.8, 0, 0));
    private static final Interpolation<Color> BEAM_COLOR = new SequentialInterpolation<>(List.of(
            new ConstantColorInterpolation(new Color(1f, 0.4f, 0.0f, 0f)),
            new LinearColorInterpolation(
                    new Color(1f, 0.4f, 0.0f, 0.0f),
                    new Color(1f, 0.8f, 0.5f, 0.5f)),
            new LinearColorInterpolation(
                    new Color(1f, 0.8f, 0.5f, 0.5f),
                    new Color(1f, 1f, 1f, 20.0f)),
            new ConstantColorInterpolation(new Color(1f, 1f, 1f, 1f))
    ), List.of(0.4, 0.52, 0.01, 0.07));
    private static final Interpolation<Double> BEAM_WIDTH = new SequentialInterpolation<>(List.of(
            new ConstantInterpolation(150),
            new LinearInterpolation(150, 200),
            new QuadraticInterpolationB(200, 5, -390),
            new ConstantInterpolation(5),
            new QuadraticInterpolationB(5, 1000, 0)
    ), List.of(0.4, 0.52, 0.01, 0.055, 0.015));
    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(600),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 5.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.5, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(10 + Math.random() * 5, 5 + Math.random(), 8)
                    ),
                    new CirclePositionEmissionPropertyStrategy(500),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 1000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );

    private List<Ring> rings;
    private Vector2f drawPos;
    private Vector2f localDrawPos;
    private int team;

    public EventAnimationDamageAOEMegumin(int team) {
        super(6, 1.5, false);
        this.team = team;
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.rings = new ArrayList<>(NUM_RINGS);
        for (int i = 0; i < NUM_RINGS; i++) {
            double ratio = (double) i / NUM_RINGS;
            this.rings.add(new Ring(ratio * RING_SPAWN_TIME, i * RING_OFFSET + RING_OFFSET_VARIATION * ratio, RING_MIN_SCALE + RING_SCALE_VARIATION * Math.random(), Math.random() * 360));
        }
        this.localDrawPos = b.uiBoard.getLocalPosOfRel(new Vector2f(0, this.visualBoard.uiBoard.getBoardPosY(this.team)));
        this.drawPos = b.uiBoard.getAbsPosOfLocal(this.localDrawPos);
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            g.setColor(new Color(0, 0, 0, FADE_ALPHA.get(this.normalizedPre()).floatValue()));
            g.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
            Image image = Game.getImage("particle/attack/megumincircle.png").getScaledCopy(1);
            g.setDrawMode(Graphics.MODE_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
            g.pushTransform();
            g.scale(1, (float) RING_Y_SCALE);
            for (int i = 0; i < NUM_RINGS; i++) {
                Ring ring = this.rings.get(i);
                if (this.normalizedPre() >= ring.delay) {
                    float scale;
                    double collapseTime = RING_COLLAPSE_TIMESTAMP + RING_COLLAPSE_DIFF * (NUM_RINGS - i - 1);
                    if (this.normalizedPre() >= collapseTime) {
                        scale = (float) (ring.finalScale * RING_COLLAPSE_INTERPOLATION.get((this.normalizedPre() - collapseTime) / (1 - collapseTime)));
                    } else {
                        scale = (float) (ring.finalScale * RING_SCALE_INTERPOLATION.get(this.normalizedPre() - ring.delay));
                    }
                    drawCenteredAndScaled(g, image,
                            new Vector2f(drawPos.x, (float) ((drawPos.y - ring.yOffset) / RING_Y_SCALE)),
                            scale,
                            1, 
                            (float) (RING_ROTATION_INTERPOLATION.get(this.normalizedPre() - ring.delay) + ring.rotationOffset));
                }
            }
            drawBeam(g, BEAM_WIDTH.get(this.normalizedPre()), BEAM_COLOR.get(this.normalizedPre()), new Vector2f(drawPos.x, -100), new Vector2f(drawPos.x, (float) (drawPos.y / RING_Y_SCALE)));
            g.popTransform();
            g.setDrawMode(Graphics.MODE_NORMAL);
        } else {
            g.setDrawMode(Graphics.MODE_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
            g.setColor(new Color(1, 0.6f, 0f, FLASHBANG_ALPHA.get(this.normalizedPost()).floatValue()));
            g.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
            g.setDrawMode(Graphics.MODE_NORMAL);
        }
        super.draw(g);
    }

    @Override
    public void onProcess() {
        this.visualBoard.uiBoard.addParticleSystem(this.localDrawPos, UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
    }

    @Override
    public String extraParamString() {
        return this.team + " ";
    }

    public static EventAnimationDamageAOEMegumin fromExtraParams(StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        return new EventAnimationDamageAOEMegumin(team);
    }

    private static record Ring(double delay, double yOffset, double finalScale, double rotationOffset) { }

}
