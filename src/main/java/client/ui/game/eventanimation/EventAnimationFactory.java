package client.ui.game.eventanimation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import client.ui.game.eventanimation.board.*;
import client.VisualBoard;
import client.ui.game.eventanimation.attack.*;
import client.ui.game.eventanimation.basic.EventAnimationAddEffect;
import client.ui.game.eventanimation.basic.EventAnimationBattlecry;
import client.ui.game.eventanimation.basic.EventAnimationClash;
import client.ui.game.eventanimation.basic.EventAnimationDamage;
import client.ui.game.eventanimation.basic.EventAnimationFlag;
import client.ui.game.eventanimation.basic.EventAnimationLastWords;
import client.ui.game.eventanimation.basic.EventAnimationOnAttack;
import client.ui.game.eventanimation.basic.EventAnimationOnAttacked;
import client.ui.game.eventanimation.basic.EventAnimationRemoveEffect;
import client.ui.game.eventanimation.basic.EventAnimationRestore;
import client.ui.game.eventanimation.basic.EventAnimationSetEffectStats;
import client.ui.game.eventanimation.basic.EventAnimationTurnStart;
import client.ui.game.eventanimation.basic.EventAnimationUnleash;
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
     * Retrieve a new animation for the event. The returned animation is guaranteed to not be null,
     * and the animation is guaranteed to resolve the event in the animation itself.
     * @param <T> The event type if known
     * @param event The event to animate
     * @return An animation for the event
     */
    public <T extends Event> EventAnimation<T> newAnimation(T event) {
        // TODO: check to see if we want to use a special animation or not
        EventAnimation<T> anim = null;
        Class<? extends EventAnimation<? extends Event>> animClass = eventToAnimationMap.get(event.getClass());
        if (animClass == null) {
            anim = new EventAnimation<T>(0, 0);
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

    private static Map<Class<? extends Event>, Class<? extends EventAnimation<? extends Event>>> eventToAnimationMap = new HashMap<>() {{
        put(EventAddEffect.class, EventAnimationAddEffect.class);
        put(EventBattlecry.class, EventAnimationBattlecry.class);
        put(EventClash.class, EventAnimationClash.class);
        put(EventDamage.class, EventAnimationDamage.class);
        put(EventFlag.class, EventAnimationFlag.class);
        put(EventLastWords.class, EventAnimationLastWords.class);
        put(EventOnAttack.class, EventAnimationOnAttack.class);
        put(EventOnAttacked.class, EventAnimationOnAttacked.class);
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
