package server.card.target;

import server.card.effect.Effect;
import utils.PositionedList;
import utils.SelectRandom;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModalTargetingScheme implements TargetingScheme<Integer> {
    private final Effect creator;
    private final int numTargets;
    private final String description;
    private final List<ModalOption> options;

    public ModalTargetingScheme(Effect creator, int numTargets, String description, List<ModalOption> options) {
        this.creator = creator;
        this.numTargets = numTargets;
        this.description = description;
        this.options = new PositionedList<>(options);
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
        return this.numTargets;
    }

    public List<ModalOption> getOptions() {
        return this.options;
    }

    @Override
    public boolean isFullyTargeted(TargetList<Integer> targets) {
        return targets.targeted.size() == this.numTargets || targets.targeted.size() == this.options.stream().filter(option -> option.conditions(this)).count();
    }

    @Override
    public boolean isValid(TargetList<Integer> targets) {
        if (targets.targeted.size() != new HashSet<>(targets.targeted).size()) {
            // has duplicates
            return false;
        }
        if (targets.targeted.size() != this.numTargets) {
            return false;
        }
        return targets.targeted.stream()
                .map(this.options::get)
                .allMatch(modalOption -> modalOption.conditions(this));
    }

    @Override
    public TargetList<Integer> generateRandomTargets() {
        List<Integer> targetable = this.getPossibleChoices();
        TargetList<Integer> ret = this.makeList();
        ret.targeted = SelectRandom.from(targetable, this.numTargets);
        return ret;
    }

    @Override
    public List<Integer> getPossibleChoices() {
        return this.options.stream()
                .filter(modalOption -> modalOption.conditions(this))
                .map(ModalOption::getIndex)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
        return true;
    }

    @Override
    public boolean conditions() {
        return this.getPossibleChoices().size() >= this.numTargets;
    }

    @Override
    public ModalTargetList parseToList(StringTokenizer st) {
        int num = Integer.parseInt(st.nextToken());
        List<Integer> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            ret.add(Integer.parseInt(st.nextToken()));
        }
        return new ModalTargetList(ret);
    }

    @Override
    public String listToString(TargetList<Integer> targets) {
        StringBuilder builder = new StringBuilder(targets.targeted.size() + " ");
        for (int i : targets.targeted) {
            builder.append(i).append(" ");
        }
        return builder.toString();
    }

    @Override
    public TargetList<Integer> makeList() {
        return new ModalTargetList(new ArrayList<>());
    }
}
