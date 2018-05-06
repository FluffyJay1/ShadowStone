package cardpack.basic;

import java.util.LinkedList;

import server.Board;
import server.card.*;
import server.event.*;

public class Fireball extends Spell {
	public static final int ID = 3;

	public Fireball(Board b) {
		super(b, CardStatus.DECK, 3, "Fireball", "Deal 2 damage to target minion and 1 damage to adjacent minions",
				"res/card/basic/fireball.png", ID);
		Target t = new Target("Target a minion.") {
			@Override
			public boolean canTarget(Card c) {
				return c instanceof Minion && !(c instanceof Leader);
			}
		};
		this.targets.add(t);
	}

	@Override
	public LinkedList<Event> battlecry() {
		LinkedList<Event> list = new LinkedList<Event>();
		int pos = ((BoardObject) this.targets.get(0).target).boardpos;
		list.add(new EventDamage((Minion) this.targets.get(0).target, 2));
		for (int i = -1; i <= 1; i += 2) {
			BoardObject b = this.board.getBoardObject(i);
			if (b != null && b instanceof Minion) {
				list.add(new EventDamage((Minion) b, 1));
			}
		}
		return list;
	}

}
