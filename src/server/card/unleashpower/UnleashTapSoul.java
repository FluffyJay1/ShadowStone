package server.card.unleashpower;

import java.util.LinkedList;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.*;
import server.Board;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashTapSoul extends UnleashPower {
	public static final int ID = -14;
	public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Tap Soul",
			"<b> Unleash </b> an allied minion. Deal 3 damage to your leader. If <b> Vengeance </b> is active for you, this can be used once more per turn.",
			"res/unleashpower/tapsoul.png", CRAFT, 1, ID, Tooltip.UNLEASH, Tooltip.VENGEANCE);

	Effect vengeanceBonus;

	public UnleashTapSoul(Board b) {
		super(b, TOOLTIP, new Vector2f(430, 535), 0.12);
		this.vengeanceBonus = new Effect(0, "When Vengeance is active, this can be used once more per turn.") {
			boolean vengeance;

			@Override
			public EventFlag onEvent(Event e) {
				if (e instanceof EventDamage || e instanceof EventRestore || e instanceof EventAddEffect) {
					if (!vengeance && p.vengeance()) {
						vengeance = true;
						EventFlag ef = new EventFlag(this) {
							@Override
							public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
								EffectStatChange esc = new EffectStatChange("");
								esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 1);
								eventlist.add(new EventSetEffectStats(vengeanceBonus, esc));
							}
						};
						return ef;
					}
					if (vengeance && !p.vengeance()) {
						vengeance = false;
						EventFlag ef = new EventFlag(this) {
							@Override
							public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
								EffectStatChange esc = new EffectStatChange("");
								esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 0);
								eventlist.add(new EventSetEffectStats(vengeanceBonus, esc));
							}
						};
						return ef;
					}
				}
				return null;
			}
		};
		this.addBasicEffect(this.vengeanceBonus);

	}

	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		list.add(new EventDamage((Leader) this.p.board.getBoardObject(this.p.team, 0), 3, false));
		return list;
	}
}
