package client.ui;

import org.newdawn.slick.geom.Vector2f;

import client.VisualBoard;
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
		this.text.setFont("Verdana", (int) (this.timey * 15) * 15);
		this.text.lineHeight = (int) (this.timey * 15) * 15;
		super.update(frametime);
		this.timey += frametime;
		timey %= 0.5;
		if (this.b.selectedCard != null && this.b.selectedCard instanceof Minion
				&& this.b.selectedCard.status == CardStatus.BOARD) {
			this.setPos(new Vector2f(250, 400), 1);
			this.hide = false;
		} else {
			this.hide = true;
		}

	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if (this.pointIsInHitbox(new Vector2f(x, y))) {
			this.b.unleashingMinion = (Minion) b.selectedCard;
		}
	}
}
