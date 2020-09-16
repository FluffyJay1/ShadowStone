package server.card.effect;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class EffectLastWordsSummon extends Effect {
	int team;
	Class<? extends Card> cardClass;

	public EffectLastWordsSummon(String description, boolean listener) {
		super(description, listener);
	}

	public EffectLastWordsSummon(String description, Class<? extends Card> cardClass, int team) {
		super(description, false);
		this.cardClass = cardClass;
		this.team = team;
	}

	@Override
	public EventLastWords lastWords() {
		Minion m = (Minion) Card.createFromConstructor(this.owner.board, this.cardClass);
		int recentcardpos = this.owner.cardpos;
		EventLastWords elw = new EventLastWords(this, false) {
			@Override
			public void resolve(List<Event> eventlist, boolean loopprotection) {
				eventlist.add(new EventCreateCard(m, team, CardStatus.BOARD, recentcardpos));
				// why is an anonymous class allowed to use variables in the
				// method scope
			}
		};
		return elw;
	}

	@Override
	public String extraStateString() {
		return this.cardClass.getName() + " " + this.team + " ";
	}

	@Override
	public Effect loadExtraState(Board b, StringTokenizer st) {
		try {
			this.cardClass = (Class<? extends Card>) Class.forName(st.nextToken());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.team = Integer.parseInt(st.nextToken());
		return this;
	}
}
