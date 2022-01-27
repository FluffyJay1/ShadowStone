package server.card.target;

import server.card.effect.Effect;
import utils.Indexable;

import java.util.function.Function;

public class ModalOption implements Indexable {
    int index;
    String option;
    private Function<Effect, Boolean> conditions;
    public ModalOption(String option, Function<Effect, Boolean> conditions) {
        this.option = option;
        this.conditions = conditions;
    }

    public ModalOption(String option) {
        this(option, null);
    }

    public String getName() {
        return option;
    }

    public boolean conditions(ModalTargetingScheme origin) {
        if (this.conditions == null) {
            return true;
        }
        return this.conditions.apply(origin.getCreator());
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }
}
