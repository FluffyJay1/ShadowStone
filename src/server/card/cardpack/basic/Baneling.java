package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.misc.*;

public class Baneling extends Minion {
	public static final int ID = 11;
	public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Baneling", "<b> Last Words: Blast(5). </b>",
			"res/card/basic/baneling.png", CRAFT, 3, 1, 0, 1, false, ID, new Vector2f(253, 271), 1.5, Tooltip.LASTWORDS,
			Tooltip.BLAST);

	public Baneling(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(0, "<b> Last Words: Blast(5). </b>") {
			@Override
			public EventLastWords lastWords() {
				EventLastWords lw = new EventLastWords(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						eventlist.add(new EventBlast(this.effect, 5));
					}
				};
				return lw;
			}
		};
		this.addEffect(true, e);
	}
}
