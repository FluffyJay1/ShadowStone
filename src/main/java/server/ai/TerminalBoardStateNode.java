package server.ai;

public class TerminalBoardStateNode extends BoardStateNode {
    /**
     * Constructs a node for when we just decide to stop evaluating. Nothing
     * past this node is evaluated.
     *
     * @param team The team being evaluated for, i.e. the team that can make a move
     * @param score The score of the board at this state
     */
    public TerminalBoardStateNode(int team, double score) {
        super(team, score);
    }

    @Override
    public String debugString() {
        return String.format("TBSN: %.2f score, %d team", this.getScore(), this.team);
    }
}
