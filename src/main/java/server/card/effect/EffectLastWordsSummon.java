package server.card.effect;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.*;

public class EffectLastWordsSummon extends Effect {
    int teamMultiplier;
    List<Class<? extends Card>> cardClasses;
    private List<Card> cachedInstances; // for getPresenceValue, preview the value of the created cards

    // required for reflection
    public EffectLastWordsSummon() { }

    public EffectLastWordsSummon(String description, List<Class<? extends Card>> cardClasses, int teamMultiplier) {
        super(description);
        this.cardClasses = cardClasses;
        this.teamMultiplier = teamMultiplier;
    }

    public EffectLastWordsSummon(String description, Class<? extends Card> cardClass, int teamMultiplier) {
        this(description, List.of(cardClass), teamMultiplier);
    }

    @Override
    public Resolver lastWords() {
        EffectLastWordsSummon effect = this;
        return new Resolver(false) {
            @Override
            public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                List<Card> newCards = new LinkedList<>();
                List<Integer> cardpos = new LinkedList<>();
                int pos = ((BoardObject) effect.owner).getRelevantBoardPos(); // startpos
                for (Class<? extends Card> cardClass : effect.cardClasses) {
                    Minion m = (Minion) Card.createFromConstructor(effect.owner.board, cardClass);
                    newCards.add(m);
                    cardpos.add(pos);
                    pos++;
                }
                this.resolve(b, rl, el, new CreateCardResolver(newCards, effect.owner.team * effect.teamMultiplier, CardStatus.BOARD, cardpos));
            }

        };
    }

    @Override
    public double getLastWordsValue(int refs) {
        if (this.cachedInstances == null) {
            this.cachedInstances = this.cardClasses.stream()
                    .map(cl -> Card.createFromConstructor(this.owner.board, cl))
                    .collect(Collectors.toList());
        }
        // behold magic numbers
        double sum = 0;
        double multiplier = 0.9;
        for (Card c : this.cachedInstances) {
            sum += c.getValue(refs - 1) * multiplier * 0.8;
            multiplier *= multiplier; // each card has lower and lower chance of being able to fit
        }
        return sum;
    }

    @Override
    public String extraStateString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.cardClasses.size()).append(" ");
        for (Class<? extends Card> cardClass : this.cardClasses) {
            builder.append(cardClass.getName()).append(" ");
        }
        builder.append(this.teamMultiplier).append(" ");
        return builder.toString();
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        int numCards = Integer.parseInt(st.nextToken());
        this.cardClasses = new LinkedList<>();
        try {
            for (int i = 0; i < numCards; i++) {
                this.cardClasses.add(Class.forName(st.nextToken()).asSubclass(Card.class));
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.teamMultiplier = Integer.parseInt(st.nextToken());
    }
}
