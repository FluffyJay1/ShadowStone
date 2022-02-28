package client.ui.game.visualboardanimation.eventanimation.attack;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
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
import java.util.function.Supplier;

public class EventAnimationDamageSlash extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/slash.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0, new Vector2f(0, 0),
                            new QuadraticInterpolationB(1, 0, 0),
                            new QuadraticInterpolationB(2, 1, 1)
                    )
            ))
    );
    private static final Image SLASH_PROJECTILE = Game.getImage("res/particle/attack/slashprojectile.png").getScaledCopy(1.5f);

    // precompute
    private List<Double> angles;

    public EventAnimationDamageSlash() {
        super(0.2);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.angles = new ArrayList<>(event.m.size());
        for (Minion m : event.m) {
            Vector2f diff = m.uiCard.getAbsPos().copy().sub(event.cardSource.uiCard.getAbsPos());
            double rad = Math.atan2(diff.y, diff.x);
            this.angles.add(rad * 180 / Math.PI);
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
        if (this.isPre()) {
            // do the shooting
            for (int i = 0; i < this.event.m.size(); i++) {
                SLASH_PROJECTILE.setRotation(this.angles.get(i).floatValue());
                Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                        .sub(this.event.cardSource.uiCard.getAbsPos()).scale((float) (this.normalizedPre()))
                        .add(this.event.cardSource.uiCard.getAbsPos());
                g.drawImage(SLASH_PROJECTILE, pos.x - SLASH_PROJECTILE.getWidth()/2, pos.y - SLASH_PROJECTILE.getHeight()/2);
            }
        } else {
            this.drawDamageNumber(g);
        }
    }
}
