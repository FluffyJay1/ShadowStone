package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
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
import java.util.function.Supplier;

public class EventAnimationDamageRocks extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(6),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/rock.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 0.9)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.15, new Vector2f(0, 400),
                            () -> new ConstantInterpolation(1),
                            () -> new QuadraticInterpolationA(0.25, 0, -4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(50),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 350)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );
    private static final Image ROCK = Game.getImage("res/particle/attack/rock.png").getScaledCopy(1);

    private List<Double> randomAngles;

    public EventAnimationDamageRocks() {
        super(0.5, true);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.randomAngles = new ArrayList<>(event.m.size());
        for (int i = 0; i < event.m.size(); i++) {
            this.randomAngles.add(Math.random() * 360);
        }
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, EMISSION_STRATEGY.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        float rotvel = 300;
        if (this.isPre()) {
            // do the shooting
            for (int i = 0; i < this.event.m.size(); i++) {
                ROCK.setRotation(randomAngles.get(i).floatValue() + rotvel * (float) this.normalizedPre());
                Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                        .sub(this.event.cardSource.uiCard.getAbsPos()).scale((float) (this.normalizedPre()))
                        .add(this.event.cardSource.uiCard.getAbsPos());
                g.drawImage(ROCK, pos.x - ROCK.getWidth()/2, pos.y - ROCK.getHeight()/2);
            }
        } else {
            this.drawDamageNumber(g);
        }
    }
}
