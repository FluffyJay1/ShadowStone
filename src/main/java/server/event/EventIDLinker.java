package server.event;

public class EventIDLinker {
    public static Class<? extends Event> getClass(int id) {
        // next id is 31
        return switch (id) {
            case EventAddEffect.ID -> EventAddEffect.class;
            case EventBanish.ID -> EventBanish.class;
            case EventCreateCard.ID -> EventCreateCard.class;
            case EventDamage.ID -> EventDamage.class;
            case EventDestroy.ID -> EventDestroy.class;
            case EventGameEnd.ID -> EventGameEnd.class;
            case EventManaChange.ID -> EventManaChange.class;
            case EventMinionAttack.ID -> EventMinionAttack.class;
            case EventMulligan.ID -> EventMulligan.class;
            case EventMulliganPhaseEnd.ID -> EventMulliganPhaseEnd.class;
            case EventUpdateEffectState.ID -> EventUpdateEffectState.class;
            case EventMuteEffect.ID -> EventMuteEffect.class;
            case EventNecromancy.ID -> EventNecromancy.class;
            case EventPlayCard.ID -> EventPlayCard.class;
            case EventPutCard.ID -> EventPutCard.class;
            case EventRemoveEffect.ID -> EventRemoveEffect.class;
            case EventRestore.ID -> EventRestore.class;
            case EventSetEffectStats.ID -> EventSetEffectStats.class;
            case EventSpellboost.ID -> EventSpellboost.class;
            case EventTransform.ID -> EventTransform.class;
            case EventTurnEnd.ID -> EventTurnEnd.class;
            case EventTurnStart.ID -> EventTurnStart.class;
            case EventUnleash.ID -> EventUnleash.class;
            default -> null;
        };
    }
}
