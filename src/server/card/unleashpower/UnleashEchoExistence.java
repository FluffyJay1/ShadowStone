package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashEchoExistence extends UnleashPower {
	public static final int ID = -16;
	public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Echo Existence",
			"<b> Unleash </b> an allied minion. If it has already attacked this turn, add a copy of it to your deck and subtract 2 from its cost.",
			"res/unleashpower/echoexistence.png", CRAFT, 2, ID, Tooltip.UNLEASH);

	public UnleashEchoExistence(Board b) {
		super(b, TOOLTIP);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		// TODO ADD SHUFFLE ANIMATION
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		if (m.attacksThisTurn > 0) {
			Card c = Card.createFromConstructor(this.p.board, m.id);
			list.add(new EventCreateCard(this.p.board, c, this.p.team, CardStatus.DECK,
					(int) (this.p.deck.cards.size() * Math.random())));
			EffectStatChange esc = new EffectStatChange("Cost reduced by 2 from <b> Echo Existence. </b>");
			esc.change.setStat(EffectStats.COST, -2);
			list.add(new EventAddEffect(c, esc));
		}
		return list;
	}
}
