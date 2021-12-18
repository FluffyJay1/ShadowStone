package server.resolver;

import server.Board;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventUpdateEffectState;

import java.util.List;

public class UpdateEffectStateResolver extends Resolver {
    private Effect effect;
    private Runnable updateStep;
    public UpdateEffectStateResolver(Effect effect, Runnable updateStep) {
        super(false);
        this.effect = effect;
        this.updateStep = updateStep;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        String oldState = effect.extraStateString();
        updateStep.run();
        String newState = effect.extraStateString();
        b.processEvent(rl, el, new EventUpdateEffectState(this.effect, oldState, newState, true));
    }
}
