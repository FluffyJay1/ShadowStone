package client.ui.game;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import client.ui.*;
import server.card.effect.*;

public class CardSelectPanel extends UIBox {
	UIBoard uib;
	UnleashButton ub;
	ScrollingContext scroll;
	Tooltip currTooltip;
	UICard lastCardSelected;
	TooltipDisplayPanel tooltipPanel;
	Text effects;
	CardSelectTooltipPanel tooltipReferencePanel;

	public CardSelectPanel(UI ui, UIBoard b) {
		super(ui, new Vector2f(-0.35f, -0.14f), new Vector2f(400, 500), "src/res/ui/uiboxborder.png");
		this.relpos = true;
		this.uib = b;
		this.margins.set(10, 10);
		this.tooltipReferencePanel = new CardSelectTooltipPanel(ui, new Vector2f(400, 0), 3);
		this.tooltipReferencePanel.setReferenceTooltip(null);
		this.addChild(tooltipReferencePanel);
		this.scroll = new ScrollingContext(ui, new Vector2f(), this.getDim(true));
		this.scroll.clip = true;
		this.addChild(this.scroll);
		this.tooltipPanel = new TooltipDisplayPanel(ui) {
			@Override
			public void mouseClicked(int button, int x, int y, int clickCount) {
				if (this.pointIsInHitbox(new Vector2f(x, y))) {
					((CardSelectPanel) this.getParent().getParent()).tooltipReferencePanel
							.setReferenceTooltip(this.tooltip);
				} else {
					((CardSelectPanel) this.getParent().getParent()).tooltipReferencePanel.setReferenceTooltip(null);
				}
			}
		};
		this.tooltipPanel.setPos(new Vector2f(0, (float) this.getLocalTop(true)), 1);
		this.scroll.addChild(this.tooltipPanel);
		this.ub = new UnleashButton(ui, uib);
		this.scroll.addChild(this.ub);
		this.effects = new Text(ui, new Vector2f((float) this.scroll.getLocalLeft(true), 200), "effects", 260, 20,
				"Lucida Console", 24, -1, -1);
		this.scroll.addChild(this.effects);
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		if (this.lastCardSelected != this.uib.selectedCard) {
			this.lastCardSelected = this.uib.selectedCard;
			this.tooltipReferencePanel.setReferenceTooltip(null);
		}
		if (this.uib.selectedCard != null) {
			// this.setPos(new Vector2f(200, 400), 1);
			this.setHide(false);
			if (this.currTooltip != this.uib.selectedCard.getCard().tooltip) {
				this.currTooltip = this.uib.selectedCard.getCard().tooltip;
				this.tooltipPanel.setTooltip(this.currTooltip);
				this.scroll.childoffset.y = 0;
			}
			String effectstext = "Effects:\n";
			for (Effect e : this.uib.selectedCard.getCard().getEffects(false)) {
				if (!e.description.isEmpty()) {
					effectstext += "- " + e.description + (e.mute ? " <b> MUTED </b> " : "") + "\n";
				}
			}
			this.effects.setText(effectstext);
			if (!this.ub.getHide()) {
				this.ub.setPos(new Vector2f(0, (float) this.tooltipPanel.getBottom(false, false) + 32), 1);
				this.effects.setPos(
						new Vector2f((float) this.getLocalLeft(true), (float) this.ub.getBottom(false, false) + 10),
						0.99);
			} else {
				this.effects.setPos(new Vector2f((float) this.getLocalLeft(true),
						(float) this.tooltipPanel.getBottom(false, false) + 10), 0.99);
			}
		} else {
			this.setHide(true);
		}

	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		if (this.tooltipPanel.pointIsInHitbox(new Vector2f(x, y))) {
			this.tooltipReferencePanel.setReferenceTooltip(this.uib.selectedCard.getCard().tooltip);
		} else {
			this.tooltipReferencePanel.setReferenceTooltip(null);
		}
	}
}
