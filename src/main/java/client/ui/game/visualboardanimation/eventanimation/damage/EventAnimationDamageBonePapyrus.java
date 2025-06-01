package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Config;
import client.Game;
import client.ui.game.UICard;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

public class EventAnimationDamageBonePapyrus extends EventAnimationDamage {
    private static final double CLIP_WIDTH = 200;

    public EventAnimationDamageBonePapyrus() {
        super(0.25, true);
    }

    @Override
    public void draw(Graphics g) {
        Image boneImage = Game.getImage("particle/attack/bonepapyrus.png");
        boneImage.setFilter(Image.FILTER_NEAREST);
        for (Minion m : this.event.m) {
            UICard uic = m.uiCard;
            Vector2f cardPos = uic.getAbsPos();
            Vector2f drawPos = cardPos.copy();
            drawPos.x += (CLIP_WIDTH + boneImage.getWidth() / 2) * (this.normalizedWhole() - 0.5) * uic.getScale();
            Rectangle prevClip = g.getClip(); // did u know that the rectangle returned is mutable by future calls to setClip
            Rectangle prevClipCloned = null;
            if (prevClip != null) {
                prevClipCloned = new Rectangle(prevClip.getX(), prevClip.getY(), prevClip.getWidth(), prevClip.getHeight());
            }
            g.setClip((int) (cardPos.x - CLIP_WIDTH / 2 * uic.getScale()), 0, (int) (CLIP_WIDTH * uic.getScale()), Config.WINDOW_HEIGHT);
            drawCenteredAndScaled(g, boneImage, drawPos, uic.getScale(), 1, 0);
            if (prevClipCloned != null) {
                g.setClip(prevClipCloned);
            } else {
                g.setClip(prevClip);
            }
        }
        super.draw(g);
    }
}
