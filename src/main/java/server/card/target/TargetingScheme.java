package server.card.target;

import server.card.effect.Effect;

import java.util.List;
import java.util.StringTokenizer;

public interface TargetingScheme<T> {
    Effect getCreator();
    String getDescription();
    int getMaxTargets();
    boolean isFullyTargeted(TargetList<T> targets);
    boolean isValid(TargetList<T> targets);
    void fillRandom(TargetList<T> targetsToFill);
    List<T> getPossibleChoices();
    boolean conditions();
    TargetList<T> parseToList(StringTokenizer st);
    String listToString(TargetList<T> targets);
    TargetList<T> makeList();
}
