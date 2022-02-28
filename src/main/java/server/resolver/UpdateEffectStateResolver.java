package server.resolver;

import server.ServerBoard;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventUpdateEffectState;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class UpdateEffectStateResolver extends Resolver {
    private final Effect effect;
    private final Runnable updateStep;
    public UpdateEffectStateResolver(Effect effect, Runnable updateStep) {
        super(false);
        this.effect = effect;
        this.updateStep = updateStep;
        this.essential = true;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        String oldState = effect.extraStateString();
        updateStep.run();
        String newState = effect.extraStateString();
        b.processEvent(rq, el, new EventUpdateEffectState(this.effect, oldState, newState, true));
    }
}
