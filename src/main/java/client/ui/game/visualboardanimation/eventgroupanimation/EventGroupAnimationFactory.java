package client.ui.game.visualboardanimation.eventgroupanimation;

import client.VisualBoard;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EventGroupAnimationFactory {
    final VisualBoard b;
    public EventGroupAnimationFactory(VisualBoard b) {
        this.b = b;
    }
    /**
     * Retrieve a new animation for the eventgroup. Does not handle pushing/popping the eventgroup stack.
     * @param eventGroup The eventgroup to animate
     * @return An animation for the eventgroup
     */
    public EventGroupAnimation newAnimation(EventGroup eventGroup) {
        EventGroupAnimation anim = null;
        Class<? extends EventGroupAnimation> animClass = eventGroupToAnimationMap.get(eventGroup.type);
        if (animClass == null) {
            return null;
        } else {
            try {
                // TODO make this not bad
                anim = animClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        assert anim != null;
        anim.init(this.b, eventGroup);
        return anim;
    }
    private static final Map<EventGroupType, Class<? extends EventGroupAnimation>> eventGroupToAnimationMap = new HashMap<>() {{
        put(EventGroupType.BATTLECRY, EventGroupAnimationBattlecry.class);
        put(EventGroupType.CLASH, EventGroupAnimationClash.class);
        put(EventGroupType.FLAG, EventGroupAnimationFlag.class);
        put(EventGroupType.LASTWORDS, EventGroupAnimationLastWords.class);
        put(EventGroupType.ONATTACK, EventGroupAnimationOnAttack.class);
        put(EventGroupType.ONATTACKED, EventGroupAnimationOnAttacked.class);
        put(EventGroupType.UNLEASH, EventGroupAnimationUnleash.class);
    }};
}
