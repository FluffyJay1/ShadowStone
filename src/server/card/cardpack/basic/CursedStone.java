package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.*;
import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.misc.*;

public class CursedStone extends Minion {
	public static final int ID = 12;
	public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Cursed Stone",
			"<b> Unleash: Blast(X) </b> and gain <b> Last Words: </b> Deal X damage to a random allied minion. X equals the amount of health your leader is missing.",
			"res/card/basic/cursedstone.png", CRAFT, 5, 1, 5, 5, false, ID, new Vector2f(), -1, Tooltip.UNLEASH,
			Tooltip.BLAST, Tooltip.LASTWORDS);
	public static final AnonymousEffectList EFFECTS = new AnonymousEffectList(ID,
			new AnonymousEffect("<b> Last Words: </b> Deal X damage to a random allied minion.", false) {
				int missing = 0;

				@Override
				public EventLastWords lastWords() {
					EventLastWords lw = new EventLastWords(this, true) {
						@Override
						public void resolve(List<Event> eventlist, boolean loopprotection) {
							Board b = this.effect.owner.board;
							List<Minion> minions = b.getMinions(this.effect.owner.team, false, true);
							if (!minions.isEmpty()) {
								Minion victim = Game.selectRandom(minions);
								eventlist.add(new EventEffectDamage(this.effect, victim, missing));
							}
						}
					};
					return lw;
				}

				@Override
				public Effect reconstruct(StringTokenizer st) throws CloneNotSupportedException {
					this.missing = Integer.parseInt(st.nextToken());
					Effect cloned = this.clone();
					cloned.description = "<b> Last Words: </b> Deal " + this.missing
							+ " damage to a random allied minion.";
					return cloned;
				}

				@Override
				public String toConstructorString() {
					return this.missing + " ";
				}
			});

	public CursedStone(Board b) {
		super(b, TOOLTIP);
		/*
		 * it's called cursed stone not because of the stone itself, but because of the
		 * anonymous classes
		 */
		Effect e = new Effect(0, TOOLTIP.description) {
			@Override
			public EventFlag unleash() {
				EventFlag ef = new EventFlag(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Player player = this.effect.owner.board.getPlayer(this.effect.owner.team);
						int missing = player.leader.finalStatEffects.getStat(EffectStats.HEALTH) - player.leader.health;
						eventlist.add(new EventBlast(this.effect, missing));
						Effect lw = null;
						try {
							lw = EFFECTS.get(0).reconstruct(new StringTokenizer(missing + " "));
						} catch (CloneNotSupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						eventlist.add(new EventAddEffect(this.effect.owner, lw));
					}
				};
				return ef;
			}
		};
		this.addEffect(true, e);
	}

}
