package server.event;

import server.event.misc.*;

public class EventIDLinker {
	public static Class<? extends Event> getClass(int id) {
		// next id is 32
		switch (id) {
		case EventAddEffect.ID:
			return EventAddEffect.class;
		case EventBanish.ID:
			return EventBanish.class;
		case EventBattlecry.ID:
			return EventBattlecry.class;
		case EventSetEffectStats.ID:
			return EventSetEffectStats.class;
		case EventClash.ID:
			return EventClash.class;
		case EventCreateCard.ID:
			return EventCreateCard.class;
		case EventDamage.ID:
			return EventDamage.class;
		case EventDestroy.ID:
			return EventDestroy.class;
		case EventDraw.ID:
			return EventDraw.class;
		case EventEffectDamage.ID:
			return EventEffectDamage.class;
		case EventEnterPlay.ID:
			return EventEnterPlay.class;
		case EventFlag.ID:
			return EventFlag.class;
		case EventGameEnd.ID:
			return EventGameEnd.class;
		case EventLastWords.ID:
			return EventLastWords.class;
		case EventLeavePlay.ID:
			return EventLeavePlay.class;
		case EventManaChange.ID:
			return EventManaChange.class;
		case EventMill.ID:
			return EventMill.class;
		case EventMinionAttack.ID:
			return EventMinionAttack.class;
		case EventMinionAttackDamage.ID:
			return EventMinionAttackDamage.class;
		case EventCardDamage.ID:
			return EventCardDamage.class;
		case EventMuteEffect.ID:
			return EventMuteEffect.class;
		case EventOnAttack.ID:
			return EventOnAttack.class;
		case EventOnAttacked.ID:
			return EventOnAttacked.class;
		case EventPlayCard.ID:
			return EventPlayCard.class;
		case EventPutCard.ID:
			return EventPutCard.class;
		case EventRemoveEffect.ID:
			return EventRemoveEffect.class;
		case EventRestore.ID:
			return EventRestore.class;
		case EventTurnEnd.ID:
			return EventTurnEnd.class;
		case EventTurnStart.ID:
			return EventTurnStart.class;
		case EventUnleash.ID:
			return EventUnleash.class;
		// start of misc
		case EventBlast.ID:
			return EventBlast.class;
		default:
			return null;
		}
	}
}
