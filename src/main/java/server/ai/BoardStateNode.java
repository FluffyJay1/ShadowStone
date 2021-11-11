package server.ai;

import java.util.*;
import java.util.Map.*;

/**
 * A node in the AI action traversal tree, mainly to cache results of previous
 * traversals. The AI class still handles traversal logic, with the manipulation
 * of board states and everything. This is pretty much just a data class.
 * 
 * @author Michael
 *
 */
public class BoardStateNode {
    /**
     * Defined Next: we know what the possible actions are and we can determine a
     * best action. A node can have an undefined next if it is e.g. reached from a
     * player action that results in RNG, has too high of a depth for the AI to
     * bother to evaluate, or the game ends, basically a leaf node.
     */
    boolean definedNext;

    // The best possible outcome after traversing this node, only accurate if fully
    // evaluated
    double maxScore;
    String maxAction;
    boolean lethal; // if guaranteed lethal can be achieved from this node

    int team; // just to be clear which team we're evaluating for
    Map<String, BoardStateNode> branches; // map PlayerAction to board state
    List<String> unevaluatedBranches;
    int totalBranches; // used to calculate for proportion of branches we need evaluated
    double currScore;

    /**
     * Default constructor, sets up everything required for the node itself but not
     * the branches, can't be used as a leaf node quite yet, as it still needs a
     * maxScore
     * 
     * @param team The team being evaluated for, i.e. the team that can make a move
     * @param poss A list of possible branches
     */
    public BoardStateNode(int team, List<String> poss) {
        this.branches = new HashMap<>();
        this.maxScore = Double.NEGATIVE_INFINITY;
        this.lethal = false;
        this.definedNext = true;

        this.team = team;
        this.unevaluatedBranches = poss;
        if (poss != null) {
            this.totalBranches = poss.size();
        }
    }

    /**
     * Constructor for a node reached from a non-deterministic action. Must be a
     * leaf node, but score is determined from traversing further.
     * 
     * @param team  The team being evaluated for, i.e. the team that can make a move
     * @param score An average score from performing the action
     */
    public BoardStateNode(int team, double score) {
        this(team, null);
        this.maxScore = score;
        this.definedNext = false;
    }

    /**
     * Helper method to record a traversal of the tree. Basically assumes that the
     * AI has already traversed the indexed branch from the list of unevaluated
     * branches, and we just record the result. Updates the max score and max
     * action.
     * 
     * @param index The index in unevaluatedBranches that we just evaluated
     * @param node  The state after evaluating that branch
     * @return Whether the action logged is the new best action
     */
    public boolean logEvaluation(int index, BoardStateNode node) {
        if (!this.definedNext) {
            // bruh what are you doing
            System.out.println("BSN: tried to log evaluation of a node with no defined next");
            return false;
        }
        String action = this.unevaluatedBranches.remove(index);
        this.branches.put(action, node);

        int flip = this.team * node.team; // if same team
        double score = node.maxScore * flip;
        if (this.maxAction == null || score > this.maxScore) {
            this.maxScore = score;
            this.maxAction = action;
            return true;
        }
        return false;
    }

    /**
     * Whether all branches are evaluated and a best action can be decided, and also
     * for clearer naming
     * 
     * @return Whether all branches are evaluated
     */
    public boolean isFullyEvaluated() {
        return this.unevaluatedBranches.isEmpty();
    }

    @Override
    public String toString() {
        String ret = "BSN: " + this.totalBranches + " branches, "
                + (this.definedNext ? this.unevaluatedBranches.size() + " unevaluated, " : "") + this.definedNext
                + " definedNext, " + this.maxScore + " maxScore"
                + (this.definedNext ? ", " + this.currScore + " currScore, " + this.maxAction : "");
        for (Entry<String, BoardStateNode> e : this.branches.entrySet()) {
            ret += e.getValue().maxScore * this.team * e.getValue().team + " " + e.getKey();
        }
        return ret;
    }
}
