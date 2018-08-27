package client.ui.game;

import org.newdawn.slick.geom.Vector2f;

import client.VisualBoard;
import client.ui.*;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

public class CardSelectPanel extends UIBox {
	VisualBoard b;
	UnleashButton ub;
	ScrollingContext scroll;
	Text name, description, effects;

	public CardSelectPanel(UI ui, VisualBoard b) {
		super(ui, new Vector2f(200, 400), new Vector2f(300, 400), "src/res/ui/uiboxborder.png");
		this.b = b;
		this.clip = true;
		this.margins.set(10, 10);
		this.scroll = new ScrollingContext(ui, new Vector2f(), this.getDim(true));
		this.addChild(this.scroll);
		this.ub = new UnleashButton(ui, b);

		this.scroll.addChild(ub);

		this.name = new Text(ui, new Vector2f(-130, (float) this.scroll.getLocalTop(true)), "name", 260, 32,
				"Arial Black", 32, -1, -1);
		this.description = new Text(ui, new Vector2f((float) this.scroll.getLocalLeft(true), -200), "description", 260,
				18, "Arial Black", 20, -1, -1);
		this.effects = new Text(ui, new Vector2f((float) this.scroll.getLocalLeft(true), 200), "effects", 260, 20,
				"Arial Black", 18, -1, -1);

		this.scroll.addChild(this.name);
		this.scroll.addChild(this.description);
		this.scroll.addChild(this.effects);
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		if (this.b.selectedCard != null) {
			// this.setPos(new Vector2f(200, 400), 1);
			this.hide = false;
			this.name.setText(this.b.selectedCard.name);
			String description = "C: " + this.b.selectedCard.finalBasicStatEffects.getStat(EffectStats.COST);
			if (this.b.selectedCard instanceof Minion) {
				description += ", A: " + this.b.selectedCard.finalBasicStatEffects.getStat(EffectStats.ATTACK) + ", M: "
						+ this.b.selectedCard.finalBasicStatEffects.getStat(EffectStats.MAGIC) + ", H: "
						+ this.b.selectedCard.finalBasicStatEffects.getStat(EffectStats.HEALTH);
			}
			description += "\n \n" + this.b.selectedCard.text;
			this.description.setText(description);
			String effectstext = "Effects:\n";
			for (Effect e : this.b.selectedCard.getAdditionalEffects()) {
				if (!e.description.isEmpty()) {
					effectstext += "- " + e.description + "\n";
				}
			}
			this.effects.setText(effectstext);
			if (!this.ub.hide) {
				this.ub.setPos(new Vector2f(0, (float) this.description.getBottom(false, false) + 32), 1);
				this.effects.setPos(
						new Vector2f((float) this.getLocalLeft(true), (float) this.ub.getBottom(false, false) + 10),
						0.99);
			} else {
				this.effects.setPos(new Vector2f((float) this.getLocalLeft(true),
						(float) this.description.getBottom(false, false) + 10), 0.99);
			}
		} else {
			this.hide = true;
		}
		this.description.setPos(
				new Vector2f((float) this.getLocalLeft(true), (float) this.name.getBottom(false, false) + 10), 1);

	}
}
