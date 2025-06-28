package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.event.*;
import server.resolver.util.ResolverQueue;

// when u wanna return a resolver but the only thing u have to do is create card
public class CreateCardResolver extends Resolver {
    public EventCreateCard event;
    private List<? extends CardText> ct;
    private List<Card> cardsToCopy;
    private int team;
    private CardStatus status;
    private List<Integer> cardpos;
    private CardVisibility visibility;
    private List<List<Effect>> effectsToAdd;

    // cards provided should be freshly constructed
    public CreateCardResolver() {
        super(false);
    }

    public static class Builder {
        private final CreateCardResolver built;
        private Integer singlePos;
        private List<Effect> singleSetOfAdditionalEffects;
        Builder() {
            this.built = new CreateCardResolver();
        }

        public Builder withCards(List<? extends CardText> ct) { built.ct = ct; return this; }
        public Builder withCard(CardText ct) { built.ct = List.of(ct); return this; }
        public Builder withCardsToCopy(List<Card> cardsToCopy) { built.cardsToCopy = cardsToCopy; return this; }
        public Builder withCardToCopy(Card cardToCopy) { built.cardsToCopy = List.of(cardToCopy); return this; }
        public Builder withTeam(int team) { built.team = team; return this; }
        public Builder withStatus(CardStatus status) { built.status = status; return this; }
        public Builder withPos(List<Integer> cardpos) { built.cardpos = cardpos; return this; }
        public Builder withPos(int cardpos) { this.singlePos = cardpos; return this; }
        public Builder withVisibility(CardVisibility visibility) { built.visibility = visibility; return this; }
        public Builder withAdditionalEffects(List<List<Effect>> effectsToAdd) { built.effectsToAdd = effectsToAdd; return this; }
        public Builder withAdditionalEffectsForAll(List<Effect> effectsToAdd) { this.singleSetOfAdditionalEffects = effectsToAdd; return this; }
        public Builder withAdditionalEffectForAll(Effect effectToAdd) { this.singleSetOfAdditionalEffects = List.of(effectToAdd); return this; }

        public CreateCardResolver build() {
            assert this.built.ct != null || this.built.cardsToCopy != null;
            if (this.built.ct == null) {
                this.built.ct = List.of();
            }
            if (this.built.cardsToCopy == null) {
                this.built.cardsToCopy = List.of();
            }
            int totalSize = this.built.ct.size() + this.built.cardsToCopy.size();
            assert this.built.team != 0;
            assert this.built.status != null;
            if (this.singlePos != null) {
                this.built.cardpos = Collections.nCopies(totalSize, this.singlePos);
            }
            assert this.built.cardpos != null;
            assert this.built.cardpos.size() == totalSize;
            if (this.built.visibility == null) {
                this.built.visibility = CardVisibility.ALL;
            }
            if (this.singleSetOfAdditionalEffects != null) {
                this.built.effectsToAdd = Collections.nCopies(totalSize, this.singleSetOfAdditionalEffects);
            }
            if (this.built.effectsToAdd == null) {
                this.built.effectsToAdd = Collections.nCopies(totalSize, List.of());
            }
            assert this.built.effectsToAdd.size() == totalSize;
            return this.built;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Card> cards = new ArrayList<>(this.ct.size() + this.cardsToCopy.size());
        for (int i = 0; i < this.ct.size(); i++) {
            Card card = this.ct.get(i).constructInstance(b);
            cards.add(card);
        }
        for (int i = 0; i < this.cardsToCopy.size(); i++) {
            Card card = Card.fromTemplateString(b, new StringTokenizer(this.cardsToCopy.get(i).toTemplateString())); // bootleg clone but it works
            cards.add(card);
        }
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            for (Effect e : this.effectsToAdd.get(i)) {
                try {
                    Effect clonedEffect = e.clone();
                    card.addEffect(false, clonedEffect);
                } catch (CloneNotSupportedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            if (card instanceof Minion) {
                Minion m = (Minion) card;
                EventCommon.enforceMinionMaxHealth(m);
            }
        }
        this.event = b.processEvent(rq, el, new EventCreateCard(cards, this.team, this.status, this.cardpos, this.visibility));
        this.resolve(b, rq, el, new DestroyResolver(event.markedForDeath, EventDestroy.Cause.NATURAL));
    }

}
