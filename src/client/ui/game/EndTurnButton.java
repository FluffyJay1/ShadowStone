package client.ui.game;

import org.newdawn.slick.geom.*;

import client.*;
import client.ui.*;

public class EndTurnButton extends UIBox {
	VisualBoard b;
	Text text;

	public EndTurnButton(UI ui, VisualBoard b) {
		super(ui, new Vector2f(1600, 540), new Vector2f(128, 128), "res/ui/border.png");
		this.text = new Text(ui, new Vector2f(0, 0), "<b> END TURN", 128, 24, "Verdana", 30, 0, 0);
		text.setParent(this);
		this.b = b;
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		this.setHide(this.b.disableInput);
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if (this.pointIsInHitbox(new Vector2f(x, y))) {
			if (!this.b.disableInput) {
				this.b.realBoard.playerEndTurn(this.b.localteam);
				this.b.handleTargeting(null);
				this.b.disableInput = true;
			}
		}
	}
}
