package client.ui.game.visualboardanimation.eventanimation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import client.ui.game.visualboardanimation.eventanimation.board.*;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.attack.*;
import client.ui.game.visualboardanimation.eventanimation.basic.EventAnimationAddEffect;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamage;
import client.ui.game.visualboardanimation.eventanimation.basic.EventAnimationRemoveEffect;
import client.ui.game.visualboardanimation.eventanimation.basic.EventAnimationRestore;
import client.ui.game.visualboardanimation.eventanimation.basic.EventAnimationSetEffectStats;
import client.ui.game.visualboardanimation.eventanimation.basic.EventAnimationTurnStart;
import client.ui.game.visualboardanimation.eventanimation.basic.EventAnimationUnleash;
import server.card.Minion;
import server.event.*;

/**
 * Handles giving the right animations for the right events
 */
public class EventAnimationFactory {
    VisualBoard b;
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
    public <T extends Event> EventAnimation<T> newAnimation(T event) {
        // TODO: check to see if we want to use a special animation or not
        EventAnimation<T> anim = null;
        Class<? extends EventAnimation<? extends Event>> animClass = getAnimationClass(event);
        if (animClass == null) {
            return null;
        } else {
            try {
                // TODO make this not bad
                anim = (EventAnimation<T>) animClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        anim.init(this.b, event);
        return anim;
    }

    private Class<? extends EventAnimation<? extends Event>> getAnimationClass(Event event) {
        if (event instanceof EventDamage) {
            EventDamage ed = (EventDamage) event;
            Class<? extends EventAnimation<? extends Event>> animClass = ed.animation;
            if (animClass != null) {
                return animClass;
            }
        }
        return eventToAnimationMap.get(event.getClass());
    }

    private static Map<Class<? extends Event>, Class<? extends EventAnimation<? extends Event>>> eventToAnimationMap = new HashMap<>() {{
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
        put(EventMinionBeginAttackPhase.class, EventAnimationMinionBeginAttackPhase.class);
        put(EventSetEffectStats.class, EventAnimationSetEffectStats.class);
    }};
}
