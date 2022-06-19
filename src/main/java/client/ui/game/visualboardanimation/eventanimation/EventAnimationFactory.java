package client.ui.game.visualboardanimation.eventanimation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import client.ui.game.visualboardanimation.eventanimation.basic.*;
import client.ui.game.visualboardanimation.eventanimation.board.*;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.damage.*;
import server.event.*;

/**
 * Handles giving the right animations for the right events
 */
public class EventAnimationFactory {
    final VisualBoard b;
    public EventAnimationFactory(VisualBoard b) {
        this.b = b;
    }

    /**
     * Retrieve a new animation for the event. If the returned event is not null,
     * the animation is guaranteed to resolve the event in the animation itself.
     * @param <T> The event type if known
     * @param event The event to animate
     * @return An animation for the event
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> EventAnimation<T> newAnimation(T event) {
        // TODO: check to see if we want to use a special animation or not
        EventAnimation<T> anim = null;
        if (event instanceof EventDamage) {
            // lol
            anim = (EventAnimation<T>) EventAnimationDamage.fromString(new StringTokenizer(((EventDamage) event).animationString));
        } else {
            // by the grace of allah this cast is safe
            Class<? extends EventAnimation<T>> animClass = (Class<? extends EventAnimation<T>>) eventToAnimationMap.get(event.getClass());
            if (animClass == null) {
                return null;
            } else {
                try {
                    anim = animClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
        assert anim != null;
        anim.init(this.b, event);
        return anim;
    }

    private static final Map<Class<? extends Event>, Class<? extends EventAnimation<? extends Event>>> eventToAnimationMap = new HashMap<>() {{
        put(EventAddEffect.class, EventAnimationAddEffect.class);
        put(EventDamage.class, EventAnimationDamage.class);
        put(EventRemoveEffect.class, EventAnimationRemoveEffect.class);
        put(EventRestore.class, EventAnimationRestore.class);
        put(EventTurnStart.class, EventAnimationTurnStart.class);
        put(EventUnleash.class, EventAnimationUnleash.class);
        put(EventGameEnd.class, EventAnimationGameEnd.class);
        put(EventPlayCard.class, EventAnimationPlayCard.class);
        put(EventPutCard.class, EventAnimationPutCard.class);
        put(EventMinionAttack.class, EventAnimationMinionAttack.class);
        put(EventSetEffectStats.class, EventAnimationSetEffectStats.class);
        put(EventCreateCard.class, EventAnimationCreateCard.class);
        put(EventDestroy.class, EventAnimationDestroy.class);
        put(EventBanish.class, EventAnimationBanish.class);
        put(EventTransform.class, EventAnimationTransform.class);
        put(EventMulliganPhaseEnd.class, EventAnimationMulliganPhaseEnd.class);
        put(EventNecromancy.class, EventAnimationNecromancy.class);
        put(EventSpellboost.class, EventAnimationSpellboost.class);
        put(EventSpend.class, EventAnimationSpend.class);
        put(EventDiscard.class, EventAnimationDiscard.class);
    }};
}
