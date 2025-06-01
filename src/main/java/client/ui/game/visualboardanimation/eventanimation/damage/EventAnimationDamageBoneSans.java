package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Config;
import client.Game;
import client.ui.game.UICard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;

import java.util.List;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

public class EventAnimationDamageBoneSans extends EventAnimationDamage {
    private static final float SCALE = 3;
    private static final float WIDTH = 200;
    private static final float CLIP_Y_OFFSET = 100;
    private static final float ATTACK_HEIGHT = 100;
    private static final List<Double> WEIGHTS = List.of(0.2, 0.6, 0.2);
    private static final Interpolation<Double> BONE_X_INTERPOLATION = new SequentialInterpolation<>(List.of(
                new ConstantInterpolation(-WIDTH/2),
                new LinearInterpolation(-WIDTH/2, WIDTH/2),
                new ConstantInterpolation(WIDTH/2)
    ), WEIGHTS);
    private static final Interpolation<Double> BONE_HEIGHT_INTERPOLATION = new SequentialInterpolation<>(List.of(
                new LinearInterpolation(0, ATTACK_HEIGHT),
                new ConstantInterpolation(ATTACK_HEIGHT),
                new LinearInterpolation(ATTACK_HEIGHT, 0)
    ), WEIGHTS);

    public EventAnimationDamageBoneSans() {
        super(0.25, true);
    }

    @Override
    public void draw(Graphics g) {
        Image boneImage = Game.getImage("particle/attack/bonesans.png").getScaledCopy(SCALE);
        boneImage.setFilter(Image.FILTER_NEAREST);
        for (Minion m : this.event.m) {
            UICard uic = m.uiCard;
            Image scaledImage = boneImage.getScaledCopy(uic.getScale());
            Vector2f cardPos = uic.getAbsPos();
            Vector2f drawPos = cardPos.copy();
            drawPos.x += BONE_X_INTERPOLATION.get(this.normalizedWhole()) * uic.getScale() - scaledImage.getWidth() / 2;
            drawPos.y += (CLIP_Y_OFFSET - BONE_HEIGHT_INTERPOLATION.get(this.normalizedWhole())) * uic.getScale();
            Rectangle prevClip = g.getClip(); // did u know that the rectangle returned is mutable by future calls to setClip
            Rectangle prevClipCloned = null;
            if (prevClip != null) {
                prevClipCloned = new Rectangle(prevClip.getX(), prevClip.getY(), prevClip.getWidth(), prevClip.getHeight());
            }
            g.setClip(0, 0, Config.WINDOW_WIDTH, (int) (cardPos.y + CLIP_Y_OFFSET * uic.getScale()));
            g.drawImage(scaledImage, drawPos.x, drawPos.y);
            if (prevClipCloned != null) {
                g.setClip(prevClipCloned);
            } else {
                g.setClip(prevClip);
            }
        }
        super.draw(g);
    }
}
