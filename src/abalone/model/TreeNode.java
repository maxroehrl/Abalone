package abalone.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Tree node containing the last move and the score of an abalone board.
 */
public class TreeNode {
    private final List<TreeNode> children = new LinkedList<>();
    private final Move move;
    private double score;

    /**
     * Creates a new tree node.
     *
     * @param move The move.
     */
    public TreeNode(Move move) {
        this.move = move;
    }

    /**
     * Add a node as a child of the current node.
     *
     * @param node The node to add as a child.
     */
    public void addChild(TreeNode node) {
        children.add(node);
    }

    /**
     * Set the score.
     *
     * @param score The score.
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Get the score.
     *
     * @return The score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Get the children of this node.
     *
     * @return The list of children.
     */
    public List<TreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Get the move of this node.
     *
     * @return The move.
     */
    public Move getMove() {
        return move;
    }

    /**
     * Get the move and the score of this node and its children in post order.
     *
     * @return The string representation of this tree node.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (TreeNode node : children) {
            // Add indents to child nodes.
            if (move != null) {
                sb.append("    ");
            }
            sb.append(node);
        }

        // The root of the tree has no last move so it gets skipped.
        if (move != null) {
            sb.append(move);

            // Use US local to get dots instead of commas.
            sb.append(String.format(Locale.US, ": %.6f\n", score));
        }
        return sb.toString();
    }
}
