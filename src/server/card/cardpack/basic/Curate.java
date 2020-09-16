package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class Curate extends Minion {
	public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Curate",
			"<b> Battlecry: </b> Restore 5 health to an ally.", "res/card/basic/curate.png", CRAFT, 7, 5, 3, 5, true,
			Curate.class, new Vector2f(169, 143), 1.4, Tooltip.BATTLECRY);

	public Curate(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(TOOLTIP.description, false) {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry eb = new EventBattlecry(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						List<Card> targets = this.effect.battlecryTargets.get(0).getTargets();
						if (!targets.isEmpty()) {
							Minion target = (Minion) targets.get(0);
							eventlist.add(new EventRestore(target, 5));
						}
					}
				};
				return eb;
			}
		};
		Target t = new Target(e, 1, "Restore 5 health to an ally.") {
			@Override
			public boolean canTarget(Card c) {
				return c instanceof Minion && ((Minion) c).isInPlay() && c.team == this.getCreator().owner.team;
			}
		};
		List<Target> list = new LinkedList<>();
		list.add(t);
		e.setBattlecryTargets(list);
		this.addEffect(true, e);
	}
}
