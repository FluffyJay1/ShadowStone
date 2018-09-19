package server.card.cardpack.basic;

import java.util.ArrayList;
import java.util.LinkedList;

import client.tooltip.TooltipSpell;
import server.Board;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.*;

public class Fireball extends Spell {
	public static final int ID = 3;
	public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
	public static final TooltipSpell TOOLTIP = new TooltipSpell("Fireball",
			"Choose 2 enemy minions. Deal 2 damage to them and 1 damage to their adjacent minions.",
			"res/card/basic/fireball.png", CRAFT, 3, ID);
	Effect e;

	public Fireball(Board b, int team) {
		super(b, team, TOOLTIP);
		// anonymous classes within anonymous classes

		this.e = new Effect(0, "") {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry bc = new EventBattlecry(this) {
					@Override
					public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
						for (int i = 0; i < this.effect.battlecryTargets.get(0).getTargets().size(); i++) {
							ArrayList<Target> m = new ArrayList<Target>();
							ArrayList<Integer> d = new ArrayList<Integer>();
							ArrayList<Boolean> p = new ArrayList<Boolean>();
							int pos = ((BoardObject) this.effect.battlecryTargets.get(0).getTargets().get(i)).cardpos;

							m.add(new Target(this.effect.battlecryTargets.get(0).getTargets().get(i))); // spaghett
							d.add(2);
							p.add(this.effect.owner.finalStatEffects.getStat(EffectStats.POISONOUS) > 0);
							for (int j = -1; j <= 1; j += 2) {
								BoardObject b = this.effect.owner.board.getBoardObject(this.effect.owner.team * -1,
										pos + j);
								if (b != null && this.effect.battlecryTargets.get(0).canTarget(b)) {
									m.add(new Target(b));
									d.add(1);
									p.add(this.effect.owner.finalStatEffects.getStat(EffectStats.POISONOUS) > 0);
								}
							}
							eventlist.add(new EventDamage(m, d, p));
						}

					}
				};
				return bc;
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
