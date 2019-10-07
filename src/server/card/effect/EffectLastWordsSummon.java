package server.card.effect;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.event.*;

public class EffectLastWordsSummon extends Effect {
	public static final int ID = 2;

	int cardid, team;

	public EffectLastWordsSummon(String description, int cardid, int team) {
		super(ID, description);
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
				eventlist.add(new EventCreateCard(this.effect.owner.board, m, team, CardStatus.BOARD, recentcardpos));
				// why is an anonymous class allowed to use variables in the
				// method scope
			}
		};
		return elw;
	}

	@Override
	public String toString() {
		return this.id + " " + this.description + Game.STRING_END + " " + this.cardid + " " + this.team + " ";
	}

	public static EffectLastWordsSummon fromString(Board b, StringTokenizer st) {
		String description = st.nextToken(Game.STRING_END).trim();
		st.nextToken(" \n"); // THANKS STRING TOKENIZER
		int cardid = Integer.parseInt(st.nextToken());
		int team = Integer.parseInt(st.nextToken());
		return new EffectLastWordsSummon(description, cardid, team);
	}
}
