package client.ui.game;

import org.newdawn.slick.geom.Vector2f;

import client.VisualBoard;
import client.tooltip.Tooltip;
import client.ui.*;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

public class CardSelectPanel extends UIBox {
	VisualBoard b;
	UnleashButton ub;
	ScrollingContext scroll;
	Tooltip currTooltip;
	Card lastCardSelected;
	TooltipDisplayPanel tooltipPanel;
	Text effects;
	CardSelectTooltipPanel tooltipReferencePanel;

	public CardSelectPanel(UI ui, VisualBoard b) {
		super(ui, new Vector2f(200, 400), new Vector2f(300, 400), "src/res/ui/uiboxborder.png");
		this.b = b;
		this.margins.set(10, 10);
		this.tooltipReferencePanel = new CardSelectTooltipPanel(ui, 3);
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
		this.ub = new UnleashButton(ui, b);
		this.scroll.addChild(this.ub);
		this.effects = new Text(ui, new Vector2f((float) this.scroll.getLocalLeft(true), 200), "effects", 260, 20,
				"Univers Condensed", 24, -1, -1);
		this.scroll.addChild(this.effects);
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		if (this.lastCardSelected != this.b.selectedCard) {
			this.lastCardSelected = this.b.selectedCard;
			this.tooltipReferencePanel.setReferenceTooltip(null);
		}
		if (this.b.selectedCard != null) {
			// this.setPos(new Vector2f(200, 400), 1);
			this.setHide(false);
			if (this.currTooltip != this.b.selectedCard.tooltip) {
				this.currTooltip = this.b.selectedCard.tooltip;
				this.tooltipPanel.setTooltip(this.currTooltip);
				this.scroll.childoffset.y = 0;
			}
			String effectstext = "Effects:\n";
			for (Effect e : this.b.selectedCard.getAdditionalEffects()) {
				if (!e.description.isEmpty()) {
					effectstext += "- " + e.description + "\n";
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
			this.tooltipReferencePanel.setReferenceTooltip(this.b.selectedCard.tooltip);
		} else {
			this.tooltipReferencePanel.setReferenceTooltip(null);
		}
	}
}
