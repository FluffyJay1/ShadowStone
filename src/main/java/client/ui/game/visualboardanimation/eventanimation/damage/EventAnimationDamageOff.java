package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.ComposedListInterpolation;
import client.ui.interpolation.realvalue.*;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageOff extends EventAnimationDamage {
    // these must be suppliers to avoid ExceptionInInitializerError
    private static final Supplier<Image> CIRCLE_IMAGE = () -> {
        Image i = Game.getImage("res/animation/circle.png");
        i.setFilter(Image.FILTER_NEAREST);
        return i;
    };
    private static final Supplier<Image> HORIZONTAL_IMAGE = () -> {
        Image i = Game.getImage("res/animation/horizontalline.png");
        i.setFilter(Image.FILTER_NEAREST);
        return i;
    };
    private static final Supplier<Image> VERTICAL_IMAGE = () -> {
        Image i = Game.getImage("res/animation/verticalline.png");
        i.setFilter(Image.FILTER_NEAREST);
        return i;
    };

    private static final Supplier<EmissionStrategy> BLOOD_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> {
                        Animation anim = new Animation("res/animation/bloodsplatter.png", new Vector2f(3, 1), 0, 0, Image.FILTER_NEAREST);
                        anim.play = true;
                        anim.setFrameInterval(0.025);
                        return anim;
                    }),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.15)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 1, new Vector2f(0, 0),
                            () -> new LinearInterpolation(1, 0),
                            () -> new ConstantInterpolation(3)
                    )
            ))
    );
    private static final Vector2f BLOOD_OFFSET = new Vector2f(-192, 0);

    private static final Interpolation<Double> START_ANIMATION_RANGE_CIRCLE = new ClampedInterpolation(0, 0.55);
    private static final Interpolation<Double> START_SCALE_CIRCLE = new ComposedListInterpolation(
            List.of(new LinearInterpolation(0.1, 1),
                    new SpringInterpolation(2.5),
                    new LinearInterpolation(8, 3)));
    private static final Interpolation<Double> START_ALPHA_CIRCLE = new LinearInterpolation(0, 1);

    private static final Interpolation<Double> START_ANIMATION_RANGE_VERTICAL = new ClampedInterpolation(0.7, 0.8);
    private static final Interpolation<Double> START_SCALE_VERTICAL = new LinearInterpolation(5, 3);
    private static final Interpolation<Double> START_ALPHA_VERTICAL = new ConstantInterpolation(1);

    private static final Interpolation<Double> START_ANIMATION_RANGE_HORIZONTAL = new ClampedInterpolation(0.8, 0.9);
    private static final Interpolation<Double> START_SCALE_HORIZONTAL = new LinearInterpolation(5, 3);
    private static final Interpolation<Double> START_ALPHA_HORIZONTAL = new ConstantInterpolation(1);

    private static final Interpolation<Double> END_ANIMATION_RANGE_CIRCLE = new ClampedInterpolation(0.2, 0.4);
    private static final Interpolation<Double> END_SCALE_CIRCLE = new ConstantInterpolation(3);
    private static final Interpolation<Double> END_ALPHA_CIRCLE = new LinearInterpolation(1, 0);

    private static final Interpolation<Double> END_ANIMATION_RANGE_CIRCLE2 = new ClampedInterpolation(0, 0.1);
    private static final Interpolation<Double> END_SCALE_CIRCLE2 = new LinearInterpolation(3, 4);
    private static final Interpolation<Double> END_ALPHA_CIRCLE2 = new LinearInterpolation(1, 0);

    private static final Interpolation<Double> END_ANIMATION_RANGE_VERTICAL = new ClampedInterpolation(0, 0.15);
    private static final Interpolation<Double> END_SCALE_VERTICAL = new LinearInterpolation(3, 4);
    private static final Interpolation<Double> END_ALPHA_VERTICAL = new LinearInterpolation(1, 0);

    private static final Interpolation<Double> END_ANIMATION_RANGE_HORIZONTAL = new ClampedInterpolation(0, 0.15);
    private static final Interpolation<Double> END_SCALE_HORIZONTAL = new LinearInterpolation(3, 4);
    private static final Interpolation<Double> END_ALPHA_HORIZONTAL = new LinearInterpolation(1, 0);

    public EventAnimationDamageOff() {
        super(0.3, true);
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos().add(BLOOD_OFFSET), UIBoard.PARTICLE_Z_BOARD, BLOOD_EMISSION_STRATEGY.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            for (Minion m : this.event.m) {
                UICard uic = m.uiCard;
                Vector2f drawPos = uic.getAbsPos();
                // circle
                double circleTime = START_ANIMATION_RANGE_CIRCLE.get(this.normalizedPre());
                drawCenteredAndScaled(g, CIRCLE_IMAGE.get(), drawPos,
                        START_SCALE_CIRCLE.get(circleTime).floatValue(),
                        START_ALPHA_CIRCLE.get(circleTime).floatValue());

                double vertTime = START_ANIMATION_RANGE_VERTICAL.get(this.normalizedPre());
                if (vertTime > 0) {
                    drawCenteredAndScaled(g, VERTICAL_IMAGE.get(), drawPos,
                            START_SCALE_VERTICAL.get(vertTime).floatValue(),
                            START_ALPHA_VERTICAL.get(vertTime).floatValue());
                }

                double horTime = START_ANIMATION_RANGE_HORIZONTAL.get(this.normalizedPre());
                if (horTime > 0) {
                    drawCenteredAndScaled(g, HORIZONTAL_IMAGE.get(), drawPos,
                            START_SCALE_HORIZONTAL.get(horTime).floatValue(),
                            START_ALPHA_HORIZONTAL.get(horTime).floatValue());
                }
            }
        } else {
            this.drawDamageNumber(g);
            for (Minion m : this.event.m) {
                UICard uic = m.uiCard;
                Vector2f drawPos = uic.getAbsPos();
                // circle
                double circleTime = END_ANIMATION_RANGE_CIRCLE.get(this.normalizedPost());
                drawCenteredAndScaled(g, CIRCLE_IMAGE.get(), drawPos,
                        END_SCALE_CIRCLE.get(circleTime).floatValue(),
                        END_ALPHA_CIRCLE.get(circleTime).floatValue());

                double circle2Time = END_ANIMATION_RANGE_CIRCLE2.get(this.normalizedPost());
                drawCenteredAndScaled(g, CIRCLE_IMAGE.get(), drawPos,
                        END_SCALE_CIRCLE2.get(circle2Time).floatValue(),
                        END_ALPHA_CIRCLE2.get(circle2Time).floatValue());

                double vertTime = END_ANIMATION_RANGE_VERTICAL.get(this.normalizedPost());
                drawCenteredAndScaled(g, VERTICAL_IMAGE.get(), drawPos,
                        END_SCALE_VERTICAL.get(vertTime).floatValue(),
                        END_ALPHA_VERTICAL.get(vertTime).floatValue());

                double horTime = END_ANIMATION_RANGE_HORIZONTAL.get(this.normalizedPost());
                drawCenteredAndScaled(g, HORIZONTAL_IMAGE.get(), drawPos,
                        END_SCALE_HORIZONTAL.get(horTime).floatValue(),
                        END_ALPHA_HORIZONTAL.get(horTime).floatValue());
            }
        }
    }
}
