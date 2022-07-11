package client.ui.game;

import client.ui.Animation;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIElement;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.vector.CircleInterpolation;
import network.Emote;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.function.Consumer;

public class EmoteSelectPanel extends UIElement {
    private static final double RADIUS = 150;
    private static final Interpolation<Vector2f> POSITION_INTERPOLATION = new ComposedInterpolation<>(new LinearInterpolation(-0.6, -0.2), new CircleInterpolation(RADIUS));
    private Text previewText;
    UIBoard board;

    List<EmoteSelectButton> buttons;

    public EmoteSelectPanel(UI ui, Vector2f pos, UIBoard board, Consumer<Emote> onSelectEmote) {
        super(ui, pos, "");
        this.buttons = List.of(
                new EmoteSelectButton(ui, new Vector2f(), Emote.GREETINGS, "res/ui/emotegreetings.png", onSelectEmote),
                new EmoteSelectButton(ui, new Vector2f(), Emote.THANKS, "res/ui/emotethanks.png", onSelectEmote),
                new EmoteSelectButton(ui, new Vector2f(), Emote.SORRY, "res/ui/emotesorry.png", onSelectEmote),
                new EmoteSelectButton(ui, new Vector2f(), Emote.WELLPLAYED, "res/ui/emotewellplayed.png", onSelectEmote),
                new EmoteSelectButton(ui, new Vector2f(), Emote.SHOCKED, "res/ui/emoteshocked.png", onSelectEmote),
                new EmoteSelectButton(ui, new Vector2f(), Emote.THINKING, "res/ui/emotethinking.png", onSelectEmote),
                new EmoteSelectButton(ui, new Vector2f(), Emote.THREATEN, "res/ui/emotethreaten.png", onSelectEmote)
        );
        for (int i = 0; i < this.buttons.size(); i++) {
            EmoteSelectButton button = this.buttons.get(i);
            button.setPos(POSITION_INTERPOLATION.get((double) i / (this.buttons.size() - 1)), 1);
            this.addChild(button);
        }
        this.previewText = new Text(ui, new Vector2f(), "", 150, 25, 25, 0, 0);
        this.addChild(this.previewText);
        this.board = board;
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.previewText.setVisible(false);
        for (EmoteSelectButton esb : this.buttons) {
            if (esb.hovering) {
                this.board.b.getPlayer(this.board.b.getLocalteam()).getLeader().ifPresent(l -> {
                    this.previewText.setText(l.getTooltip().emoteSet.getLine(esb.emote));
                    this.previewText.setVisible(true);
                });
                break;
            }
        }
    }

    private static class EmoteSelectButton extends UIElement {
        public boolean hovering;
        Consumer<Emote> onClick;
        Emote emote;

        public EmoteSelectButton(UI ui, Vector2f pos, Emote emote, String iconPath, Consumer<Emote> onClick) {
            super(ui, pos, new Animation("res/ui/emotebutton.png", new Vector2f(2, 1), 0, 0));
            this.hitcircle = true;
            this.emote = emote;
            UIElement icon = new UIElement(ui, new Vector2f(), iconPath);
            icon.ignorehitbox = true;
            this.addChild(icon);
            this.onClick = onClick;
        }

        @Override
        public void update(double frametime) {
            super.update(frametime);
            if (this.pointIsInHitbox(this.ui.lastmousepos.x, this.ui.lastmousepos.y)) {
                this.getAnimation().setFrame(1);
                this.hovering = true;
            } else {
                this.getAnimation().setFrame(0);
                this.hovering = false;
            }
        }

        @Override
        public void mouseClicked(int button, int x, int y, int clickCount) {
            this.getParent().setVisible(false);
            this.onClick.accept(this.emote);
        }
    }
}
