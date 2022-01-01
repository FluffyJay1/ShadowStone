package client.ui.game;

import org.newdawn.slick.geom.*;

import client.ui.*;

public class UnleashButton extends UIElement {
    final UIBoard uib;
    final Text text;
    double timey = 0;

    public UnleashButton(UI ui, UIBoard b) {
        super(ui, new Vector2f(0, 0), "res/ui/unleashbutton.png");
        this.text = new Text(ui, new Vector2f(0, 0), "<b> UNLEASH", 128, 24, "Verdana", 30, 0, 0);
        text.setParent(this);
        this.uib = b;
        this.setVisible(false);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);

        this.setVisible(!this.uib.b.disableInput && this.uib.selectedCard != null
                && this.uib.b.getPlayer(1).canUnleashCard(this.uib.selectedCard.getCard()));

    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        if (this.pointIsInHitbox(new Vector2f(x, y))) {
            if (!this.uib.b.disableInput) {
                this.uib.selectUnleashingMinion(this.uib.selectedCard);
            }
        }
    }
}
