package abalone.model;

/**
 * Represents a move operation on any abalone board.
 */
public class Move {
    private final int rowFrom;
    private final int diagFrom;
    private final int rowTo;
    private final int diagTo;

    /**
     * Create a new move with the given coordinates.
     *
     * @param rowFrom The start row.
     * @param diagFrom The start diagonal.
     * @param rowTo The target row.
     * @param diagTo The target diagonal.
     */
    public Move(int rowFrom, int diagFrom, int rowTo, int diagTo) {
        this.rowFrom = rowFrom;
        this.diagFrom = diagFrom;
        this.rowTo = rowTo;
        this.diagTo = diagTo;
    }

    /**
     * Get the start row.
     *
     * @return The start row.
     */
    public int getRowFrom() {
        return rowFrom;
    }

    /**
     * Get the start diagonal.
     *
     * @return The start diagonal.
     */
    public int getDiagFrom() {
        return diagFrom;
    }

    /**
     * Get the target row.
     *
     * @return The target row.
     */
    public int getRowTo() {
        return rowTo;
    }

    /**
     * Get the target diagonal.
     *
     * @return The target diagonal.
     */
    public int getDiagTo() {
        return diagTo;
    }

    /**
     * Get start row and diagonal and target row and diagonal as a string.
     *
     * @return The string representation of this move.
     */
    @Override
    public String toString() {
        return String.format("move %d %d %d %d", rowFrom + 1, diagFrom + 1,
                rowTo + 1, diagTo + 1);
    }
}
