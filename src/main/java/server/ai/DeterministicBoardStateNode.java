package server.ai;

import utils.WeightedSampler;

import java.util.*;

/**
 * We know what the possible actions are and we can determine a best action.
 * This is opposed to not having a defined next action, if it is e.g. reached
 * from a player action that results in RNG, has too high of a depth for the AI
 * to bother to evaluate, or the game ends, basically a leaf node.
 */
public class DeterministicBoardStateNode extends BoardStateNode {
    boolean lethal; // if guaranteed lethal can be achieved from this node
    // The best possible outcome after traversing this node, only accurate if fully
    // evaluated
    private Decision cachedMax;
    private boolean dirtyMax;
    final Map<String, BoardStateNode> branches; // map PlayerAction to board state, the set of already evaluated branches
    private final WeightedSampler<String> branchSampler;
    private List<String> branchEvaluationQueue; // list of unevaluated branches, in the order that they should be evaluated in
    int evaluatedBranches; // different than branches.size(), if we need to re-evaluate then this goes to 0
    int totalBranches;
    // The quality of evaluation for this node, with 1 being full sample rate
    // usually corresponds to the depth at which this was evaluated
    double sampleRate;

    public DeterministicBoardStateNode(int team, double currScore, WeightedSampler<String> branchSampler) {
        super(team, currScore);
        this.lethal = false;
        this.dirtyMax = true;
        this.branches = new HashMap<>();
        this.branchSampler = branchSampler;
        if (branchSampler != null) {
            this.branchEvaluationQueue = new LinkedList<>(branchSampler.sample());
            this.totalBranches = branchSampler.size();
        } else {
            this.branchEvaluationQueue = new LinkedList<>();
        }
    }

    /**
     * Gets the next unevaluated action for this node, like an iterator.
     *
     * @return The action we should evaluate next
     */
    public String nextBranchToEvaluate() {
        return this.branchEvaluationQueue.remove(0);
    }

    /**
     * Reset the "choose without replacement" sampling of branches. Used when we
     * need to re-evaluate existing branches at a new detail level.
     */
    public void resetEvaluationOrder() {
        if (this.evaluatedBranches > 0) {
            this.branchEvaluationQueue = new LinkedList<>(this.branchSampler.sample());
            this.evaluatedBranches = 0;
        }
    }

    /**
     * Find the maximizing action, and the value from executing it. The value is
     * passed through alpha-factor calculations. Note that old values may change
     * if the node gets revisited and new branches are evaluated, as per the
     * alpha factor. Uses cached results if none of the children change.
     *
     * @return The maximizing action and the value from doing it
     */
    public Decision getMax() {
        if (this.dirtyMax) {
            if (this.branches.isEmpty()) {
                this.cachedMax = new Decision(null, this.currScore);
            } else {
                Optional<Decision> maxDecision = this.branches.entrySet().stream()
                        .map(e -> new Decision(e.getKey(), e.getValue().team * this.team * e.getValue().getScore()))
                        .max(Comparator.comparingDouble(d -> d.score));
                this.cachedMax = maxDecision.orElse(new Decision(null, this.currScore));
            }
            this.dirtyMax = false;
        }
        return this.cachedMax;
    }

    @Override
    public double getScore() {
        return this.getMax().score;
    }

    /**
     * Helper method to recursively invalidate the values of nodes that depend
     * on this node. Assumes no cycles in the graph.
     */
    private void markDirtyMax() {
        // if this is already dirty, we can assume the ancestors are also already dirty
        if (!this.dirtyMax) {
            for (DeterministicBoardStateNode ancestor : this.ancestors) {
                ancestor.markDirtyMax();
            }
            this.dirtyMax = true;
        }
    }

    /**
     * Helper method to record a traversal of the tree. Basically assumes that
     * the AI has already traversed the current unevaluated branch, and we just
     * record the result. Updates the max score and max action.
     *
     * @param node  The state after evaluating this branch
     */
    public void logEvaluation(String action, BoardStateNode node) {
        this.branches.put(action, node);
        this.evaluatedBranches++;
        node.ancestors.add(this);
        this.markDirtyMax();
    }

    /**
     * Whether all branches are properly evaluated and a best action can be
     * decided, and also for clearer naming. This is different from having all
     * branches merely evaluated, as they themselves may not have been evaluated
     * at our required detail.
     *
     * @return Whether all branches are evaluated
     */
    public boolean isFullyEvaluated() {
        return this.branchEvaluationQueue.isEmpty();
    }

    /**
     * Find out what proportion of importance a branch holds, according to the
     * weight we assigned the branch. Should sum to 1 over all actions on this
     * branch.
     * @param action The action to query
     * @return The weighted proportion this action occupies
     */
    public double getWeightedProportionOfAction(String action) {
        return this.branchSampler.getWeightOfItem(action) / this.branchSampler.getTotalWeight();
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(this.debugString()).append("\n");
        for (Map.Entry<String, BoardStateNode> e : this.branches.entrySet()) {
            ret.append(e.getKey().equals(this.getMax().action) ? "->[" : "- [").append(e.getValue().debugString()).append("] ").append(e.getKey());
        }
        return ret.toString();
    }

    @Override
    public String debugString() {
        Decision max = this.getMax();
        return String.format("DBSN: %.2f score, %d team, %.2f currScore, %d branches, %d eval, %d seen",
                max.score, this.team, this.currScore, this.totalBranches, this.evaluatedBranches, this.branches.size());
    }

    public static class Decision {
        String action;
        double score;
        public Decision(String action, double score) {
            this.action = action;
            this.score = score;
        }
    }

}
