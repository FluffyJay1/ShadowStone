package server.card.effect.common;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class EffectLastWordsSummon extends Effect {
    int teamMultiplier;
    List<BoardObjectText> boardObjectTexts;
    private List<Card> cachedInstances; // for getPresenceValue, preview the value of the created cards

    // required for reflection
    public EffectLastWordsSummon() { }

    public EffectLastWordsSummon(String description, List<? extends BoardObjectText> boardObjectTexts, int teamMultiplier) {
        super(description);
        this.boardObjectTexts = new ArrayList<>(boardObjectTexts);
        this.teamMultiplier = teamMultiplier;
    }

    public EffectLastWordsSummon(String description, BoardObjectText boardObjectText, int teamMultiplier) {
        this(description, List.of(boardObjectText), teamMultiplier);
    }

    @Override
    public ResolverWithDescription lastWords() {
        EffectLastWordsSummon effect = this;
        return new ResolverWithDescription(this.description, new Resolver(false) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                List<Integer> cardpos = new LinkedList<>();
                int pos = ((BoardObject) effect.owner).getRelevantBoardPos(); // startpos
                for (BoardObjectText cardText : effect.boardObjectTexts) {
                    cardpos.add(pos);
                    pos++;
                }
                this.resolve(b, rq, el, new CreateCardResolver(boardObjectTexts, effect.owner.team * effect.teamMultiplier, CardStatus.BOARD, cardpos));
            }
        });
    }

    @Override
    public double getLastWordsValue(int refs) {
        if (this.cachedInstances == null) {
            this.cachedInstances = this.boardObjectTexts.stream()
                    .map(bot -> bot.constructInstance(this.owner.board))
                    .collect(Collectors.toList());
        }
        return this.teamMultiplier * AI.valueForSummoning(this.cachedInstances, refs);
    }

    @Override
    public String extraStateString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.boardObjectTexts.size()).append(" ");
        for (BoardObjectText bot : this.boardObjectTexts) {
            builder.append(bot.toString());
        }
        builder.append(this.teamMultiplier).append(" ");
        return builder.toString();
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        int numCards = Integer.parseInt(st.nextToken());
        this.boardObjectTexts = new LinkedList<>();
        for (int i = 0; i < numCards; i++) {
            this.boardObjectTexts.add(BoardObjectText.fromString(st.nextToken()));
        }
        this.teamMultiplier = Integer.parseInt(st.nextToken());
    }
}
