package server.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RNGBoardStateNode extends BoardStateNode {
    // Keep track the different results we got, and how often we got them
    private final Map<BoardStateNode, Integer> counts;
    int trials;

    boolean dirtyScore;
    double cachedScore;

    /**
     * Constructor for a node reached from a non-deterministic action. Must be a
     * leaf node, but score is determined from traversing further.
     *
     * @param team  The team being evaluated for, i.e. the team that can make a move
     */
    public RNGBoardStateNode(int team) {
        super(team, 0);
        this.counts = new HashMap<>();
        this.dirtyScore = false;
    }

    /**
     * Record a trial of performing the action that led to this node.
     * Reweights this node's score accordingly.
     *
     * @param next The node obtained after simulating the action that resulted in rng
     */
    public void addTrial(BoardStateNode next) {
        this.counts.merge(next, 1, Integer::sum);
        this.trials++;
        // doesn't have to mark parent dirty, the dbsn that led to this node will handle it
        // by logging evaluation
        this.dirtyScore = true;
    }

    /**
     * Get the number of times we reached a node from a trial.
     *
     * @param node The node to query
     * @return The number of times we hit that node
     */
    public int getCount(BoardStateNode node) {
        return Objects.requireNonNullElse(this.counts.get(node), 0);
    }

    @Override
    public double getScore() {
        if (this.dirtyScore) {
            this.cachedScore = 0;
            for (Map.Entry<BoardStateNode, Integer> entry : this.counts.entrySet()) {
                BoardStateNode bsn = entry.getKey();
                this.cachedScore += bsn.getScore() * bsn.team * this.team * entry.getValue();
            }
            cachedScore /= this.trials;
            this.dirtyScore = false;
        }
        return this.cachedScore;
    }

    @Override
    public String debugString() {
        return String.format("RNG: %.2f score, %d team, %d trials, %d unique results", this.getScore(), this.team, this.trials, this.counts.size());
    }
}
