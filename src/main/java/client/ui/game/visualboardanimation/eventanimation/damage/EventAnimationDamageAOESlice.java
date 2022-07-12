package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;
import server.event.EventDamage;

import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public class EventAnimationDamageAOESlice extends EventAnimationDamage {
    private static final Supplier<Image> SLICE_IMAGE = () -> Game.getImage("particle/attack/slice.png");
    private static final Supplier<Image> SLICE_WAKE_IMAGE = () -> Game.getImage("particle/attack/slicewake.png");
    private static final float SLICE_SCALE = 1;
    private static final float SLICE_WAKE_SCALE = 4;

    private static final Interpolation<Double> SLICE_X = new LinearInterpolation(-0.5, 3.5);
    private static final Interpolation<Double> SLICE_WAKE_X = new LinearInterpolation(-3.5, 1.5);

    private static final Supplier<EmissionStrategy> GLOW_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(3),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation(Game.getImage("particle/misc/glow.png"))),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.4)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0.04, new Vector2f(),
                            () -> new QuadraticInterpolationA(0.3, 0, -0.3),
                            () -> new LinearInterpolation(1, 3)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 1500))
            ))
    );

    private static final double INCLUDE_LEADER_OFFSET = 0.1;
    private static final float INCLUDE_LEADER_SCALE_MULTIPLIER = 1.5f;
    final int team;
    final boolean includeLeader;
    float drawY; // idk

    public EventAnimationDamageAOESlice(int team, boolean includeLeader) {
        super(0.35, false);
        this.team = team;
        this.includeLeader = includeLeader;
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.drawY = (float) this.visualBoard.uiBoard.getBoardPosY(this.team);
        if (this.includeLeader) {
            this.drawY += INCLUDE_LEADER_OFFSET * this.team * this.visualBoard.getLocalteam();
        }
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(GLOW_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            Vector2f sliceLocalPos = this.visualBoard.uiBoard.getPosOfRel(new Vector2f(SLICE_X.get(this.normalizedPre()).floatValue(), this.drawY));
            Vector2f slicePos = this.visualBoard.uiBoard.getAbsPosOfLocal(sliceLocalPos);
            Vector2f sliceWakeLocalPos = this.visualBoard.uiBoard.getPosOfRel(new Vector2f(SLICE_WAKE_X.get(this.normalizedPre()).floatValue(), this.drawY));
            Vector2f sliceWakePos = this.visualBoard.uiBoard.getAbsPosOfLocal(sliceWakeLocalPos);
            g.setDrawMode(Graphics.MODE_ADD);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
            drawCenteredAndScaled(g, SLICE_IMAGE.get(), slicePos, SLICE_SCALE * (this.includeLeader ? INCLUDE_LEADER_SCALE_MULTIPLIER : 1), 1);
            drawCenteredAndScaled(g, SLICE_WAKE_IMAGE.get(), sliceWakePos, SLICE_WAKE_SCALE * (this.includeLeader ? INCLUDE_LEADER_SCALE_MULTIPLIER : 1), 1);
            g.setDrawMode(Graphics.MODE_NORMAL);
        } else {
            this.drawDamageNumber(g);
        }
    }

    @Override
    public String extraParamString() {
        return this.team + " " + this.includeLeader + " ";
    }

    public static EventAnimationDamageAOESlice fromExtraParams(StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        boolean includeLeader = Boolean.parseBoolean(st.nextToken());
        return new EventAnimationDamageAOESlice(team, includeLeader);
    }
}
