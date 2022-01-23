package server.card.target;

import java.util.*;
import java.util.stream.Collectors;

import server.card.Card;
import server.card.effect.*;

public abstract class CardTargetingScheme implements TargetingScheme<Card> {
    private final Effect creator;
    private final int minTargets;
    private final int maxtargets;
    public String description;

    public CardTargetingScheme(Effect creator, int minTargets, int maxtargets, String description) {
        this.creator = creator;
        this.minTargets = minTargets;
        this.maxtargets = maxtargets;
        this.description = description;
    }

    @Override
    public Effect getCreator() {
        return this.creator;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int getMaxTargets() {
        return this.maxtargets;
    }

    // override this shit with anonymous functions
    public boolean canTarget(Card c) {
        return true;
    }

    @Override
    public void fillRandom(TargetList<Card> targetsToFill) {
        List<Card> cards = this.creator.owner.board.getTargetableCards(this)
                .filter(c -> this.canTarget(c) && !targetsToFill.list.contains(c))
                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = targetsToFill.list.size(); i < this.maxtargets && cards.size() > 0; i++) {
            targetsToFill.list.add(cards.remove((int) (Math.random() * cards.size())));
        }
    }

    @Override
    public List<Card> getPossibleChoices() {
        return this.creator.owner.board.getTargetableCards(this).collect(Collectors.toList());
    }

    @Override
    public boolean conditions() {
        return this.creator.owner.board.getTargetableCards(this).count() >= this.minTargets;
    }

    // if the maximum number of targets is selected, or all targetable cards
    // have been targeted
    @Override
    public boolean isFullyTargeted(TargetList<Card> targets) {
        return targets.list.size() == this.maxtargets || targets.list.size() == this.creator.owner.board.getTargetableCards(this).count();
    }

    @Override
    public boolean isValid(TargetList<Card> targets) {
        if (targets.list.stream().anyMatch(c -> !this.canTarget(c))) {
            // can't target
            return false;
        }
        if (targets.list.size() != new HashSet<>(targets.list).size()) {
            // has duplicates
            return false;
        }
        return targets.list.size() >= this.minTargets && targets.list.size() <= this.maxtargets;
    }

    @Override
    public CardTargetList parseToList(StringTokenizer st) {
        int num = Integer.parseInt(st.nextToken());
        List<Card> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            ret.add(Card.fromReference(this.creator.owner.board, st));
        }
        return new CardTargetList(ret);
    }

    @Override
    public String listToString(TargetList<Card> targets) {
        StringBuilder builder = new StringBuilder(targets.list.size() + " ");
        for (Card c : targets.list) {
            builder.append(c.toReference());
        }
        return builder.toString();
    }

    @Override
    public TargetList<Card> makeList() {
        return new CardTargetList(new ArrayList<>());
    }
}
