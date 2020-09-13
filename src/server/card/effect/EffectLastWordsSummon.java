package server.card.effect;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class EffectLastWordsSummon extends Effect {
	int cardid, team;

	public EffectLastWordsSummon(String description, boolean listener) {
		super(description, listener);
	}

	public EffectLastWordsSummon(String description, int cardid, int team) {
		super(description, false);
		this.cardid = cardid;
		this.team = team;
	}

	@Override
	public EventLastWords lastWords() {
		Minion m = (Minion) Card.createFromConstructor(this.owner.board, this.cardid);
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
		return this.cardid + " " + this.team + " ";
	}

	@Override
	public Effect loadExtraState(Board b, StringTokenizer st) {
		this.cardid = Integer.parseInt(st.nextToken());
		this.team = Integer.parseInt(st.nextToken());
		return this;
	}
}
