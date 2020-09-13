package server.card.effect;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.event.*;

public class EffectLastWordsAlliedBlast extends Effect {
	int damage = 0;

	public EffectLastWordsAlliedBlast(String description, boolean listener) {
		super(description, listener);
	}

	public EffectLastWordsAlliedBlast(int damage) {
		this.damage = damage;
	}

	@Override
	public EventLastWords lastWords() {
		EventLastWords lw = new EventLastWords(this, true) {
			@Override
			public void resolve(List<Event> eventlist, boolean loopprotection) {
				Board b = this.effect.owner.board;
				List<Minion> minions = b.getMinions(this.effect.owner.team, false, true);
				if (!minions.isEmpty()) {
					Minion victim = Game.selectRandom(minions);
					eventlist.add(new EventEffectDamage(this.effect, victim, damage));
				}
			}
		};
		return lw;
	}

	@Override
	public String extraStateString() {
		return this.damage + " ";
	}

	@Override
	public Effect loadExtraState(Board b, StringTokenizer st) {
		this.damage = Integer.parseInt(st.nextToken());
		this.description = "<b> Last Words: </b> Deal " + this.damage + " damage to a random allied minion.";
		return this;
	}
}
