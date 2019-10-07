package server.card;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.event.*;

public class BoardObject extends Card {

	public BoardObject(Board b, TooltipCard tooltip) {
		super(b, tooltip);
	}

	public boolean isInPlay() {
		return this.alive && this.status.equals(CardStatus.BOARD);
	}

	public List<EventLastWords> lastWords() {
		List<EventLastWords> list = new LinkedList<EventLastWords>();
		for (Effect e : this.getFinalEffects()) {
			EventLastWords temp = e.lastWords();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onTurnStart() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onTurnStart();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onTurnEnd() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onTurnEnd();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onEnterPlay() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onEnterPlay();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onLeavePlay() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onLeavePlay();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}
}
