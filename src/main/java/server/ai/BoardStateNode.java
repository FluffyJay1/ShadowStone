package server.ai;

import java.util.*;

/**
 * A node in the AI action traversal tree, mainly to cache results of previous
 * traversals. The AI class still handles traversal logic, with the manipulation
 * of board states and everything. This is more of a companion data class that
 * deals with the tree-like structure of decision making. It also maintains an
 * ordering of the branches to traverse.
 * 
 * @author Michael
 *
 */
public abstract class BoardStateNode {

    final int team; // just to be clear which team we're evaluating for
    // There may be multiple ways to go from state A to state B, so we may have multiple ancestors
    // for value caching, if this node changes, we should mark the ancestor nodes as dirty too
    final Set<DeterministicBoardStateNode> ancestors;
    double currScore;
    /**
     * Default constructor, sets up everything required for the node itself
     *
     * @param team The team being evaluated for, i.e. the team that can make a move
     * @param currScore The score of the board at this state
     */
    public BoardStateNode(int team, double currScore) {
        this.team = team;
        this.currScore = currScore;
        this.ancestors = new HashSet<>();
    }

    /**
     * Gets the score of the current node. This is used to compare against other
     * branches when deciding which one to traverse.
     *
     * @return The score of this node.
     */
    public double getScore() {
        return this.currScore;
    }

    public abstract String debugString();
}
