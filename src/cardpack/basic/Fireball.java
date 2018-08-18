package cardpack.basic;

import java.util.ArrayList;
import java.util.LinkedList;

import server.Board;
import server.card.*;
import server.card.effect.Effect;
import server.event.*;

public class Fireball extends Spell {
	public static final int ID = 3;
	Effect e;

	public Fireball(Board b, int team) {
		super(b, CardStatus.DECK, 3, "Fireball",
				"Choose 2 enemy minions. Deal 2 damage to them and 1 damage to their adjacent minions.",
				"res/card/basic/fireball.png", team, ID);

		this.e = new Effect(0, "") {
			@Override
			public LinkedList<Event> battlecry() {
				LinkedList<Event> list = new LinkedList<Event>();

				for (int i = 0; i < 2; i++) {
					ArrayList<Target> m = new ArrayList<Target>();
					ArrayList<Integer> d = new ArrayList<Integer>();
					int pos = ((BoardObject) this.battlecryTargets.get(0).getTargets().get(i)).cardpos;

					m.add(new Target(this.battlecryTargets.get(0).getTargets().get(i))); // spaghett
					d.add(2);
					for (int j = -1; j <= 1; j += 2) {
						BoardObject b = this.owner.board.getBoardObject(this.owner.team * -1, pos + j);
						if (b != null && this.battlecryTargets.get(0).canTarget(b)) {
							m.add(new Target(b));
							d.add(1);
						}
					}
					list.add(new EventDamage(m, d));
				}

				return list;
			}

		};
		Target t = new Target(e, 2, "Deal 2 damage to an enemy minion and 1 damage to adjacent minions.") {
			@Override
			public boolean canTarget(Card c) {
				return c.status == CardStatus.BOARD && c instanceof Minion && !(c instanceof Leader)
						&& ((Minion) c).team != this.getCreator().owner.team;
			}
		};

		LinkedList<Target> list = new LinkedList<Target>();
		list.add(t);
		e.setBattlecryTargets(list);
		this.addBasicEffect(e);
	}

	@Override
	public boolean conditions() {
		return this.board.getTargetableCards(this.e.battlecryTargets.get(0)).size() > 0;
	}

}
