package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
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
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new QuadraticInterpolationB(2, 1, 1)
                    )
            ))
    );
    private static final Image SLASH_PROJECTILE = Game.getImage("res/particle/attack/slashprojectile.png").getScaledCopy(1.5f);

    public EventAnimationDamageSlash() {
        super(0.2, true);
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
            this.drawProjectile(g, SLASH_PROJECTILE);
        } else {
            this.drawDamageNumber(g);
        }
    }
}
