package client.ui.game.visualboardanimation.eventanimation.attack;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;
import server.event.EventDamage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EventAnimationDamageShoot extends EventAnimationDamage {
    private static final Function<Vector2f, EmissionStrategy> SHOOT_DUST_STRATEGY = (dir) -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(15),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/dust.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 0.8)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.1, new Vector2f(),
                            () -> new LinearInterpolation(1, 0),
                            () -> new QuadraticInterpolationB(1, 4, 6)
                    ),
                    new DirectionalVelocitySpreadEmissionPropertyStrategy(dir, 1.9, new LinearInterpolation(100, 1200)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );

    private static final Function<Vector2f, EmissionStrategy> SHOOT_FIRE_STRATEGY = (dir) -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(6),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.35)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.15, new Vector2f(),
                            () -> new LinearInterpolation(1, 0),
                            () -> new QuadraticInterpolationB(0.25 + Math.random(), 0, 3)
                    ),
                    new DirectionalVelocitySpreadEmissionPropertyStrategy(dir, 0.3, new LinearInterpolation(100, 2500)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );

    private static final Function<Vector2f, EmissionStrategy> SHOOT_SHRAPNEL_STRATEGY = (dir) -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(20),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/shrapnel.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.25, 0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.003, new Vector2f(),
                            () -> new LinearInterpolation(1, 0),
                            () -> new ConstantInterpolation(new LinearInterpolation(2, 5).get(Math.random()))
                    ),
                    new DirectionalVelocitySpreadEmissionPropertyStrategy(dir, 1.5, new LinearInterpolation(0, 4000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-1000, 1000))
            ))
    );
    private static final Image SHOOT_PROJECTILE = Game.getImage("res/particle/attack/round.png");

    // precompute
    private List<Vector2f> dirs;
    private List<Double> angles;

    public EventAnimationDamageShoot() {
        super(0.15);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.dirs = new ArrayList<>(event.m.size());
        this.angles = new ArrayList<>(event.m.size());
        for (Minion m : event.m) {
            Vector2f diff = m.uiCard.getAbsPos().copy().sub(event.cardSource.uiCard.getAbsPos());
            double rad = Math.atan2(diff.y, diff.x);
            this.dirs.add(diff);
            this.angles.add(rad * 180 / Math.PI);
        }
    }

    @Override
    public void onStart() {
        for (Vector2f dir : this.dirs) {
            this.visualBoard.uiBoard.addParticleSystem(this.event.cardSource.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, SHOOT_DUST_STRATEGY.apply(dir));
        }
        // layering
        for (Vector2f dir : this.dirs) {
            this.visualBoard.uiBoard.addParticleSystem(this.event.cardSource.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, SHOOT_FIRE_STRATEGY.apply(dir));
        }
    }

    @Override
    public void onProcess() {
        for (int i = 0; i < this.event.m.size(); i++) {
            Minion m = this.event.m.get(i);
            Vector2f dir = this.dirs.get(i);
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, SHOOT_SHRAPNEL_STRATEGY.apply(dir));
        }
    }

    @Override
    public void draw(Graphics g) {
        float rotvel = 300;
        if (this.isPre()) {
            // do the shooting
            for (int i = 0; i < this.event.m.size(); i++) {
                SHOOT_PROJECTILE.setRotation(this.angles.get(i).floatValue());
                Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                        .sub(this.event.cardSource.uiCard.getAbsPos()).scale((float) (this.normalizedPre()))
                        .add(this.event.cardSource.uiCard.getAbsPos());
                g.drawImage(SHOOT_PROJECTILE, pos.x - SHOOT_PROJECTILE.getWidth()/2, pos.y - SHOOT_PROJECTILE.getHeight()/2);
            }
        } else {
            this.drawDamageNumber(g);
        }
    }
}
