package server.event;

public class EventIDLinker {
    public static Class<? extends Event> getClass(int id) {
        // next id is 31
        return switch (id) {
            case EventAddEffect.ID -> EventAddEffect.class;
            case EventBanish.ID -> EventBanish.class;
            case EventBattlecry.ID -> EventBattlecry.class;
            case EventSetEffectStats.ID -> EventSetEffectStats.class;
            case EventClash.ID -> EventClash.class;
            case EventCreateCard.ID -> EventCreateCard.class;
            case EventDamage.ID -> EventDamage.class;
            case EventDestroy.ID -> EventDestroy.class;
            case EventEnterPlay.ID -> EventEnterPlay.class;
            case EventFlag.ID -> EventFlag.class;
            case EventGameEnd.ID -> EventGameEnd.class;
            case EventLastWords.ID -> EventLastWords.class;
            case EventLeavePlay.ID -> EventLeavePlay.class;
            case EventManaChange.ID -> EventManaChange.class;
            case EventMill.ID -> EventMill.class;
            case EventMinionAttack.ID -> EventMinionAttack.class;
            case EventUpdateEffectState.ID -> EventUpdateEffectState.class;
            case EventMuteEffect.ID -> EventMuteEffect.class;
            case EventOnAttack.ID -> EventOnAttack.class;
            case EventOnAttacked.ID -> EventOnAttacked.class;
            case EventPlayCard.ID -> EventPlayCard.class;
            case EventPutCard.ID -> EventPutCard.class;
            case EventRemoveEffect.ID -> EventRemoveEffect.class;
            case EventRestore.ID -> EventRestore.class;
            case EventTurnEnd.ID -> EventTurnEnd.class;
            case EventTurnStart.ID -> EventTurnStart.class;
            case EventUnleash.ID -> EventUnleash.class;
            default -> null;
        };
    }
}
