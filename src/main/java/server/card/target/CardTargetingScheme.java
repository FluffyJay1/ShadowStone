package server.card.target;

import java.util.*;
import java.util.stream.Collectors;

import server.card.Card;
import server.card.effect.*;
import utils.SelectRandom;

public abstract class CardTargetingScheme implements TargetingScheme<Card> {
    private final Effect creator;
    private final int minTargets;
    private final int maxtargets;
    private final String description;

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
    protected abstract boolean criteria(Card c);

    public final boolean canTarget(Card c) {
        // stealth or elusive
        return !(c.team != this.getCreator().owner.team && c.isInPlay() && (c.finalStats.get(Stat.STEALTH) > 0 || c.finalStats.get(Stat.ELUSIVE) > 0))
                && this.criteria(c);
    }

    @Override
    public TargetList<Card> generateRandomTargets() {
        List<Card> targetable = this.creator.owner.board.getTargetableCards(this).toList();
        TargetList<Card> ret = this.makeList();
        ret.targeted = SelectRandom.from(targetable, this.maxtargets);
        return ret;
    }

    @Override
    public List<Card> getPossibleChoices() {
        return this.creator.owner.board.getTargetableCards(this).collect(Collectors.toList());
    }

    @Override
    public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
        return this.getPossibleChoices().size() > 0;
    }

    @Override
    public boolean conditions() {
        return this.creator.owner.board.getTargetableCards(this).count() >= this.minTargets;
    }

    // if the maximum number of targets is selected, or all targetable cards
    // have been targeted
    @Override
    public boolean isFullyTargeted(TargetList<Card> targets) {
        return targets.targeted.size() == this.maxtargets || targets.targeted.size() == this.creator.owner.board.getTargetableCards(this).count();
    }

    @Override
    public boolean isValid(TargetList<Card> targets) {
        if (targets.targeted.stream().anyMatch(c -> !this.canTarget(c))) {
            // can't target
            return false;
        }
        if (targets.targeted.size() != new HashSet<>(targets.targeted).size()) {
            // has duplicates
            return false;
        }
        return targets.targeted.size() >= this.minTargets && targets.targeted.size() <= this.maxtargets;
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
        StringBuilder builder = new StringBuilder(targets.targeted.size() + " ");
        for (Card c : targets.targeted) {
            builder.append(c.toReference());
        }
        return builder.toString();
    }

    @Override
    public TargetList<Card> makeList() {
        return new CardTargetList(new ArrayList<>());
    }
}
