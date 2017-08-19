package abalone.model;


/**
 * Interface for an Abalone (lite) game.
 *
 * A human plays against the machine. The human's ground lines are always the
 * lowest rows, whereas the ground lines of the machine are the highest rows.
 * The human plays from bottom to top, the machine from top to bottom. The user
 * with the black balls opens the game. Winner is who first pushes
 * out/eliminates six of the opponent's balls.
 *
 * <p>
 * There are some differences to traditional Abalone:
 * <ul>
 * <li>In case that one player has no option to make a valid move, he must miss
 *     a turn. The other player can make a move in any case.
 * <li>A move can involve more than three (own) balls.
 * <li>Sideward moves, i.e., changing the diagonal of more than one own ball not
 *     in the same row, are not allowed.
 * <li>The game may never end.
 * </ul>
 */
public interface Board {

    /**
     * The least allowed number of rows and diagonals of the game.
     */
    int MIN_SIZE = 7;

    /**
     * The number of opponent balls which must be eliminated to win.
     */
    int ELIM = 6;

    /**
     * Gets the player who should open or already has opened the game. As an
     * invariant, this player has the black balls.
     *
     * @return The player who makes the initial move.
     */
    Player getOpeningPlayer();

    /**
     * Gets the color of the human player.
     *
     * @return The tile color of the human.
     */
    Color getHumanColor();

    /**
     * Gets the player who is allowed to execute the next move.
     *
     * @return The player who shall make the next move.
     * @throws IllegalStateException If the game is already over.
     */
    Player getNextPlayer();

    /**
     * Checks if the provided coordinates are valid slots within the game.
     *
     * @param row The row.
     * @param diag The diagonal.
     * @return {@code true} iff valid coordinates.
     */
    boolean isValidPosition(int row, int diag);

    /**
     * Checks if the provided coordinates are valid slots within the game or 1
     * slot outside.
     *
     * @param row The row.
     * @param diag The diagonal.
     * @return {@code true} iff valid coordinates.
     */
    boolean isValidTarget(int row, int diag);

    /**
     * Executes a human move. Eliminating own balls is allowed. This method does
     * not change the state of this instance, which is treated here as
     * immutable. Instead, a new board/game is returned, which is a copy of
     * {@code this} with the move executed.
     *
     * @param rowFrom The slot's row number from which the ball of the human
     *        player should be moved.
     * @param diagFrom The slot's diagonal number from which the ball of the
     *        human player should be moved.
     * @param rowTo The slot's row number to which the ball of the human player
     *        should be moved.
     * @param diagTo The slot's diagonal number to which the ball of the human
     *        player should be moved.
     * @return A new board with the move executed. If the move is not valid,
     *         then {@code null} will be returned.
     * @throws IllegalStateException If the game is already over, or it is not
     *         the human's turn.
     * @throws IllegalArgumentException If the provided parameters are invalid,
     *         e.g., the from slot lies outside the grid or the to slot outside
     *         the grid plus an one-element border.
     */
    Board move(int rowFrom, int diagFrom, int rowTo, int diagTo);

    /**
     * Executes a machine move. This method does not change the state of this
     * instance, which is treated here as immutable. Instead, a new board/game
     * is returned, which is a copy of {@code this} with the move executed.
     *
     * @return A new board with the move executed.
     * @throws IllegalStateException If the game is already over, or it is not
     *         the machine's turn.
     */
    Board machineMove();

    /**
     * Sets the skill level of the machine.
     *
     * @param level The skill as a number, must be at least 1.
     */
    void setLevel(int level);

    /**
     * Checks if the game is over.
     *
     * @return {@code true} if and only if the game is over.
     */
    boolean isGameOver();

    /**
     * Checks if the game state is won. Only valid if the game is already over.
     *
     * @return The winner.
     * @throws IllegalStateException If the game is not over yet, then there is
     *         no winner.
     */
    Player getWinner();

    /**
     * Gets the number of balls currently placed on the grid for the provided
     * color. Only valid for black or white.
     *
     * @param color The color for which to count the balls.
     * @return The number of balls.
     */
    int getNumberOfBalls(Color color);

    /**
     * Gets the color (black or white) of a ball in the slot at the specified
     * coordinates. If the slot is empty, then the result is no color (e.g.
     * NONE). 
     *
     * @param row The row of the slot in the game grid.
     * @param diag The diagonal of the slot in the game grid.
     * @return The slot color.
     */
    Color getSlot(int row, int diag);

    /**
     * Gets the number of rows = the number of diagonals of this game.
     *
     * @return The size of the game.
     */
    int getSize();

    /**
     * Gets the string representation of this board as row x diagonals hexagon.
     * Each slot is represented by one the three chars '.', 'X', or 'O'. '.'
     * means that the slot currently contains no ball. 'X' means that it
     * contains a black tile. 'O' means that it contains a white tile.
     *
     * @return The string representation of the current state of this game.
     */
    @Override
    String toString();

}