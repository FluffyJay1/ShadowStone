package client.ui.game;

import org.newdawn.slick.geom.Vector2f;

import client.VisualBoard;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIElement;
import server.card.CardStatus;
import server.card.Minion;

public class UnleashButton extends UIElement {
	VisualBoard b;
	Text text;
	double timey = 0;

	public UnleashButton(UI ui, VisualBoard b) {
		super(ui, new Vector2f(0, 0), "res/ui/unleashbutton.png");
		this.text = new Text(ui, new Vector2f(0, 0), "<b> UNLEASH", 128, 24, "Verdana", 30, 0, 0);
		text.setParent(this);
		this.b = b;
		this.hide = true;
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		if (this.b.selectedCard != null && this.b.selectedCard instanceof Minion
				&& this.b.selectedCard.status == CardStatus.BOARD && this.b.selectedCard.team == 1) {
			this.hide = false;
		} else {
			this.hide = true;
		}

	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if (this.pointIsInHitbox(new Vector2f(x, y))) {
			this.b.unleashingMinion = (Minion) b.selectedCard;
			this.b.resolveNoUnleashTarget();
			this.b.animateUnleashTargets(true);
		}
	}
}
