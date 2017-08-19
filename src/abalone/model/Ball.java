package abalone.model;

import java.util.Arrays;

/**
 * A ball with a color, an owner and two coordinates.
 */
public class Ball implements Cloneable {
    private final Color color;
    private final Player owner;
    private int row;
    private int diag;

    /**
     * Create a new ball.
     *
     * @param color The color of the ball.
     * @param owner The owner of the ball.
     * @param row The row of the ball.
     * @param diag The diagonal of the ball.
     */
    public Ball(Color color, Player owner, int row, int diag) {
        this.color = color;
        this.owner = owner;
        this.row = row;
        this.diag = diag;
    }

    /**
     * Get the color of the ball.
     *
     * @return The color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get the owner of the ball.
     *
     * @return The owner.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Get the row coordinate of the ball.
     *
     * @return The row coordinate.
     */
    public int getRow() {
        return row;
    }

    /**
     * Set the row coordinate of the ball.
     *
     * @param row The new row coordinate.
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Get the diagonal coordinate of the ball.
     *
     * @return The diagonal coordinate.
     */
    public int getDiag() {
        return diag;
    }

    /**
     * Set the diagonal coordinate of the ball.
     *
     * @param diag The new diagonal coordinate.
     */
    public void setDiag(int diag) {
        this.diag = diag;
    }

    /**
     * Get the distance of this ball to the edge of the board.
     *
     * @param size The size of the abalone board.
     * @return The distance to the edge of the board.
     */
    public int distToEdge(int size) {
        int diag2 = row - diag + size / 2;
        int[] dists = {row, diag, diag2, size - row - 1, size - diag - 1,
                size - diag2 - 1};
        return Arrays.stream(dists).min().getAsInt();
    }

    /**
     * Get a deep copy of this ball.
     *
     * @return A copy of this ball.
     */
    @Override
    public Ball clone() {
        try {
            return (Ball) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Get row, diagonal, owner and color of the ball as a string.
     *
     * @return The string representation of this ball.
     */
    @Override
    public String toString() {
        return String.format("%s's Ball at (%s,%s): %s", owner, row, diag,
                color);
    }
}
