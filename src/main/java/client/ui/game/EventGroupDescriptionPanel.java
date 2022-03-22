package client.ui.game;

import client.Game;
import client.ui.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.event.eventgroup.EventGroup;

public class EventGroupDescriptionPanel extends UIBox {
    private static final Color LOCAL_COLOR = new Color(0.7f, 0.8f, 1.0f);
    private static final Color ENEMY_COLOR = new Color(1.0f, 0.6f, 0.6f);
    private static final int ICON_SIZE = 64;
    private static final float TEXT_SPACING = 10;
    public EventGroup eg;
    private double time;
    final private UIElement loading, check;
    public EventGroupDescriptionPanel(UI ui, Vector2f pos, float width, EventGroup eg) {
        super(ui, pos, new Vector2f(width, 0), "res/ui/uiboxborder.png");
        this.margins.set(20, 20);
        this.eg = eg;
        Image iconImage = null;
        float iconWidth = 0;
        switch (eg.type) {
            case CLASH -> iconImage = Game.getImage("res/game/clash.png");
            case ONATTACK -> iconImage = Game.getImage("res/game/attack.png");
            case ONATTACKED -> iconImage = Game.getImage("res/game/defend.png");
            case FLAG -> iconImage = Game.getImage("res/game/flag.png");
            case LASTWORDS -> iconImage = Game.getImage("res/game/lastwords.png");
            case UNLEASH -> iconImage = Game.getImage("res/game/unleash.png");
        }
        UIElement icon = null;
        if (iconImage != null) {
            iconImage = iconImage.getScaledCopy(ICON_SIZE, ICON_SIZE);
            iconWidth = ICON_SIZE;
            icon = new UIElement(ui, new Vector2f(this.getWidth(true), 0), new Animation(iconImage));
            icon.alignh = 1;
            icon.alignv = -1;
            this.addChild(icon);
        }
        Text title = null;
        if (eg.cards.size() > 0) {
            Card c = eg.cards.get(0);
            title = new Text(ui, new Vector2f(), "<b>" + c.getTooltip().name, this.getWidth(true) - iconWidth,
                    40, 48, -1, -1);
            // if e.g. it has a single big word that causes it to exceed the expected width
            float excessRatio = title.getWidth(false) / (this.getWidth(true) - iconWidth);
            if (excessRatio > 1.02) {
                // adjust the font size so the offending word fits within the width
                title.setFont((int) (48 / excessRatio));
            }
            this.addChild(title);
            if (c.team == c.board.localteam) {
                this.setColor(LOCAL_COLOR);
            } else {
                this.setColor(ENEMY_COLOR);
            }
        }
        Text text = new Text(ui, new Vector2f(), eg.description, this.getWidth(true),
                30, 32, -1, -1);
        this.addChild(text);
        if (title != null) {
            this.setDim(new Vector2f(this.getWidth(false), title.getHeight(false) + text.getHeight(false) + TEXT_SPACING + this.margins.y * 2));
            if (icon != null) {
                icon.setPos(new Vector2f(this.getWidth(true)/2, -this.getHeight(true)/2), 1);
            }
            title.setPos(new Vector2f(-this.getWidth(true)/2, -this.getHeight(true)/2), 1);
            text.setPos(new Vector2f(-this.getWidth(true)/2, title.getBottom(false, false) + TEXT_SPACING), 1);
        } else {
            this.setDim(new Vector2f(this.getWidth(false), text.getHeight(false) + this.margins.y * 2));
            text.setPos(new Vector2f(-this.getWidth(true)/2, -this.getHeight(true)/2), 1);
        }
        this.check = new UIElement(ui, new Vector2f(this.getWidth(false)/2, 0), "res/ui/check.png");
        this.check.alignh = -1;
        this.check.setVisible(false);
        this.addChild(check);
        Animation loadingAnimation = new Animation("res/ui/loading.png", new Vector2f(4, 2), 0, 0);
        loadingAnimation.play = true;
        loadingAnimation.loop = true;
        loadingAnimation.setFrameInterval(0.1);
        this.loading = new UIElement(ui, new Vector2f(this.getWidth(false)/2, 0), loadingAnimation);
        this.loading.alignh = -1;
        this.loading.setVisible(true);
        this.addChild(loading);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.time += frametime;
    }

    public void markDone() {
        this.check.setVisible(true);
        this.loading.setVisible(false);
    }

    public double getTime() {
        return this.time;
    }
}
