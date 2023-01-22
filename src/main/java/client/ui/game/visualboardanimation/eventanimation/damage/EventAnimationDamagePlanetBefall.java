package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.realvalue.ClampedInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.interpolation.vector.LinearVectorInterpolation;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.event.EventDamage;

import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Supplier;

public class EventAnimationDamagePlanetBefall extends EventAnimationDamage {
    private static final Supplier<Image> ROCK_IMAGE = () -> Game.getImage("animation/planetbefall.png");
    private static final Supplier<Image> SHADOW_IMAGE = () -> Game.getImage("animation/shadow.png");

    private static final Interpolation<Vector2f> ROCK_OFFSET_REL = new ComposedInterpolation<>(
            new QuadraticInterpolationA(0, 1, 0.8),
            new LinearVectorInterpolation(new Vector2f(-0.75f, -1.5f), new Vector2f(0, -0.1f))
    );

    private static final float ROCK_SCALE_FINAL = 3;
    private static final Interpolation<Double> ROCK_SCALE = new QuadraticInterpolationB(10, ROCK_SCALE_FINAL, -0.5);
    private static final Interpolation<Double> ROCK_ALPHA = new ComposedInterpolation<>(
            new ClampedInterpolation(0, 0.5),
            new QuadraticInterpolationB(0, 1, 2)
    );
    private static final Interpolation<Double> SHADOW_ALPHA = new QuadraticInterpolationB(0, 1, 0);
    private static final Interpolation<Double> SHADOW_SCALE = new QuadraticInterpolationA(7, 4, -3);
    private static final Interpolation<Double> ROCK_ANIMATION_RANGE_POST = new ClampedInterpolation(0, 0.25);
    private static final Interpolation<Double> ROCK_SCALE_POST = new QuadraticInterpolationA(ROCK_SCALE_FINAL, 10, -0.2);
    private static final Interpolation<Double> ROCK_ALPHA_POST = new QuadraticInterpolationB(1, 0, 0);
    private static final float Y_REL_ENEMY = -0.25f;
    private static final float Y_REL_ALLIED = 0.3f;

    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(100),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.6, 1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.15, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(4 + Math.random() * 2, 6 + Math.random() * 3, 4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(300),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(500, 5000, 1000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    int team;
    float y;

    public EventAnimationDamagePlanetBefall(int team) {
        super(0.7, false);
        this.team = team;
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.y = this.team == b.getLocalteam() ? Y_REL_ALLIED : Y_REL_ENEMY;
    }

    @Override
    public void onProcess() {
        Vector2f explosionPos = this.visualBoard.uiBoard.getPosOfRel(new Vector2f(0, this.y));
        this.visualBoard.uiBoard.addParticleSystem(explosionPos, UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            Vector2f shadowPos = this.visualBoard.uiBoard.getAbsPosOfLocal(this.visualBoard.uiBoard.getPosOfRel(new Vector2f(0, this.y)));
            drawCenteredAndScaled(g, SHADOW_IMAGE.get(), shadowPos, SHADOW_SCALE.get(this.normalizedPre()).floatValue(), SHADOW_ALPHA.get(this.normalizedPre()).floatValue());
            Vector2f rockPos = this.visualBoard.uiBoard.getAbsPosOfLocal(this.visualBoard.uiBoard.getPosOfRel(ROCK_OFFSET_REL.get(this.normalizedPre()).add(new Vector2f(0, this.y))));
            drawCenteredAndScaled(g, ROCK_IMAGE.get(), rockPos, ROCK_SCALE.get(this.normalizedPre()).floatValue(), ROCK_ALPHA.get(this.normalizedPre()).floatValue());
        } else {
            double rockTime = ROCK_ANIMATION_RANGE_POST.get(this.normalizedPost());
            Vector2f rockPos = this.visualBoard.uiBoard.getAbsPosOfLocal(this.visualBoard.uiBoard.getPosOfRel(ROCK_OFFSET_REL.get(1).add(new Vector2f(0, this.y))));
            drawCenteredAndScaled(g, ROCK_IMAGE.get(), rockPos, ROCK_SCALE_POST.get(rockTime).floatValue(), ROCK_ALPHA_POST.get(rockTime).floatValue());
            this.drawDamageNumber(g);
        }
    }

    @Override
    public String extraParamString() {
        return this.team + " ";
    }

    public static EventAnimationDamagePlanetBefall fromExtraParams(StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        return new EventAnimationDamagePlanetBefall(team);
    }
}
