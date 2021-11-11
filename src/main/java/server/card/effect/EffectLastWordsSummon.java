package server.card.effect;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.*;

public class EffectLastWordsSummon extends Effect {
    int team;
    List<Class<? extends Card>> cardClasses;

    public EffectLastWordsSummon(String description) {
        super(description);
    }

    public EffectLastWordsSummon(String description, List<Class<? extends Card>> cardClasses, int team) {
        super(description);
        this.cardClasses = cardClasses;
        this.team = team;
    }

    public EffectLastWordsSummon(String description, Class<? extends Card> cardClass, int team) {
        this(description, List.of(cardClass), team);
    }

    @Override
    public Resolver lastWords() {
        EffectLastWordsSummon effect = this;
        return new Resolver(false) {
            @Override
            public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                List<Card> newCards = new LinkedList<>();
                List<Integer> cardpos = new LinkedList<>();
                int pos = ((BoardObject) effect.owner).lastBoardPos; // startpos
                for (Class<? extends Card> cardClass : effect.cardClasses) {
                    Minion m = (Minion) Card.createFromConstructor(effect.owner.board, cardClass);
                    newCards.add(m);
                    cardpos.add(pos);
                    pos++;
                }
                this.resolve(b, rl, el, new CreateCardResolver(newCards, effect.team, CardStatus.BOARD, cardpos));
            }

        };
    }

    @Override
    public String extraStateString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.cardClasses.size()).append(" ");
        for (Class<? extends Card> cardClass : this.cardClasses) {
            builder.append(cardClass.getName()).append(" ");
        }
        builder.append(this.team).append(" ");
        return builder.toString();
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        int numCards = Integer.parseInt(st.nextToken());
        this.cardClasses = new LinkedList<>();
        try {
            for (int i = 0; i < numCards; i++) {
                this.cardClasses.add((Class<? extends Card>) Class.forName(st.nextToken()));
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.team = Integer.parseInt(st.nextToken());
    }
}
