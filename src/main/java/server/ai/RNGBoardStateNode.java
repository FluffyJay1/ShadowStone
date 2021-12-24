package server.ai;

public class RNGBoardStateNode extends BoardStateNode {
    // we should keep track of how many trials we ran already
    int trials;

    /**
     * Constructor for a node reached from a non-deterministic action. Must be a
     * leaf node, but score is determined from traversing further.
     *
     * @param team  The team being evaluated for, i.e. the team that can make a move
     * @param totalScore The total score from performing the action across multiple trials
     * @param trials The number of trials performed to reach this node
     */
    public RNGBoardStateNode(int team, double totalScore, int trials) {
        super(team, totalScore / trials);
        this.trials = trials;
    }

    /**
     * Record extra trials of performing the action that led to this node.
     * Reweights this node's score accordingly.
     *
     * @param totalScore The sum of the score across all the trials
     * @param trials The number of trials conducted
     */
    public void addTrials(double totalScore, int trials) {
        this.currScore = ((this.currScore * this.trials) + (totalScore)) / (this.trials + trials);
        this.trials += trials;
    }

    @Override
    public String debugString() {
        return String.format("RNG: %.2f score, %d team, %d trials", this.getScore(), this.team, this.trials);
    }
}
