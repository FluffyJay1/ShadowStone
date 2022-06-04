package server.event;

import server.Board;

import java.util.StringTokenizer;

public class EventFactory {
    public static Event fromString(Board b, StringTokenizer st) {
        int id = Integer.parseInt(st.nextToken());
        return switch (id) {
            case EventAddEffect.ID -> EventAddEffect.fromString(b, st);
            case EventBanish.ID -> EventBanish.fromString(b, st);
            case EventCreateCard.ID -> EventCreateCard.fromString(b, st);
            case EventDamage.ID -> EventDamage.fromString(b, st);
            case EventDestroy.ID -> EventDestroy.fromString(b, st);
            case EventDiscard.ID -> EventDiscard.fromString(b, st);
            case EventGameEnd.ID -> EventGameEnd.fromString(b, st);
            case EventManaChange.ID -> EventManaChange.fromString(b, st);
            case EventMinionAttack.ID -> EventMinionAttack.fromString(b, st);
            case EventMulligan.ID -> EventMulligan.fromString(b, st);
            case EventMulliganPhaseEnd.ID -> EventMulliganPhaseEnd.fromString(b, st);
            case EventUpdateEffectState.ID -> EventUpdateEffectState.fromString(b, st);
            case EventMuteEffect.ID -> EventMuteEffect.fromString(b, st);
            case EventNecromancy.ID -> EventNecromancy.fromString(b, st);
            case EventPlayCard.ID -> EventPlayCard.fromString(b, st);
            case EventPutCard.ID -> EventPutCard.fromString(b, st);
            case EventRemoveEffect.ID -> EventRemoveEffect.fromString(b, st);
            case EventRestore.ID -> EventRestore.fromString(b, st);
            case EventSetEffectStats.ID -> EventSetEffectStats.fromString(b, st);
            case EventSpellboost.ID -> EventSpellboost.fromString(b, st);
            case EventSpend.ID -> EventSpend.fromString(b, st);
            case EventTransform.ID -> EventTransform.fromString(b, st);
            case EventTurnEnd.ID -> EventTurnEnd.fromString(b, st);
            case EventTurnStart.ID -> EventTurnStart.fromString(b, st);
            case EventUnleash.ID -> EventUnleash.fromString(b, st);
            default -> null;
        };
    }
}
