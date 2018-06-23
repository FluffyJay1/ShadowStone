package server.event;

public class EventIDLinker {
	public static Class<? extends Event> getClass(int id) {
		switch (id) {
		case EventAddEffect.ID:
			return EventAddEffect.class;
		case EventCreateCard.ID:
			return EventCreateCard.class;
		case EventDamage.ID:
			return EventDamage.class;
		case EventDestroy.ID:
			return EventDestroy.class;
		case EventDraw.ID:
			return EventDraw.class;
		case EventManaChange.ID:
			return EventManaChange.class;
		case EventMill.ID:
			return EventMill.class;
		case EventMinionAttack.ID:
			return EventMinionAttack.class;
		case EventMinionAttackDamage.ID:
			return EventMinionAttackDamage.class;
		case EventMinionDamage.ID:
			return EventMinionDamage.class;
		case EventPlayCard.ID:
			return EventPlayCard.class;
		case EventRestore.ID:
			return EventRestore.class;
		case EventTurnEnd.ID:
			return EventTurnEnd.class;
		case EventTurnStart.ID:
			return EventTurnStart.class;
		case EventUnleash.ID:
			return EventUnleash.class;
		default:
			return null;
		}
	}
}
