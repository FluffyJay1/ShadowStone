package cardpack.basic;

import java.util.LinkedList;

import server.Board;
import server.card.*;
import server.event.*;

public class Fireball extends Spell {
	public static final int ID = 3;

	public Fireball(Board b, int team) {
		super(b, CardStatus.DECK, 3, "Fireball", "Deal 2 damage to an enemy minion and 1 damage to adjacent minions",
				"res/card/basic/fireball.png", team, ID);
		Target t = new Target(this, "Target an enemy minion.") {
			@Override
			public boolean canTarget(Card c) {
				return c.status == CardStatus.BOARD && c instanceof Minion && !(c instanceof Leader)
						&& ((Minion) c).team != this.creator.team;
			}
		};
		this.battlecryTargets.add(t);
	}

	@Override
	public LinkedList<Event> battlecry() {
		LinkedList<Event> list = new LinkedList<Event>();
		int pos = ((BoardObject) this.battlecryTargets.get(0).target).boardpos;
		list.add(new EventDamage((Minion) this.battlecryTargets.get(0).target, 2));
		for (int i = -1; i <= 1; i += 2) {
			BoardObject b = this.board.getBoardObject(pos + i);
			if (b != null && this.battlecryTargets.get(0).canTarget(b)) {
				list.add(new EventDamage((Minion) b, 1));
			}
		}
		return list;
	}

}
