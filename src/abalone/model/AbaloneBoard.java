package abalone.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Abalone (lite) game.
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
public class AbaloneBoard implements Board, Cloneable {
    /**
     * Matrix containing the balls of the abalone board.
     */
    private Ball[][] board;

    /**
     * List of all balls of the human player.
     */
    private List<Ball> humanBalls = new LinkedList<>();

    /**
     * List of all balls of the machine player.
     */
    private List<Ball> machineBalls = new LinkedList<>();

    /**
     * Number of balls each player has at the beginning of a match.
     */
    private final int startBalls;

    /**
     * The player who has opened the game.
     */
    private final Player openingPlayer;

    /**
     * The player who is allowed to make the next move.
     */
    private Player nextPlayer;

    /**
     * The depth of the tree the machine uses for the game tree.
     */
    private int difficultyLevel;

    /**
     * The valid move vectors a ball has got.
     */
    private static final int[][] VALID_MOVE_VECTORS
            = {{0, 1}, {1, 1}, {1, 0}, {0, -1}, {-1, -1}, {-1, 0}};

    /**
     * Creates a new abalone board with the default size of 9, a difficulty
     * level of 2 and the human starts the game.
     */
    public AbaloneBoard() {
        this(9, Player.HUMAN, 2);
    }

    /**
     * Creates a new abalone board with the given size.
     *
     * @param size The size of the board must not be smaller than 7 and odd.
     * @param openingPlayer The player who makes the first move.
     * @param difficultyLevel The difficulty level the machine uses.
     * @throws IllegalArgumentException If the size is smaller than 7 or even or
     *                                  if the given level is smaller than 1.
     */
    public AbaloneBoard(int size, Player openingPlayer, int difficultyLevel) {
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("The smallest size for the board"
                    + " is 7. Given size: " + size);
        } else if (size % 2 == 0) {
            throw new IllegalArgumentException("Only odd board sizes are "
                    + "allowed. Given size: " + size);
        } else {
            this.openingPlayer = openingPlayer;
            nextPlayer = openingPlayer;

            setLevel(difficultyLevel);

            initializeBoard(size);
            startBalls = machineBalls.size();
        }
    }

    /**
     * Initialize the board with slots and assign balls to the correct slots.
     * The balls of the human player are at the bottom.
     * Also add the balls to the correct lists.
     *
     * @param size Size of the board must be bigger or equal than 7 and odd.
     */
    private void initializeBoard(int size) {
        board = new Ball[size][size];

        // Add the starting balls to the board.
        for (int row = 0; row < size; row++) {
            for (int diag = getFirstDiag(row); diag <= getLastDiag(row);
                 diag++) {
                Ball ball = null;

                if (row <= 1 || row == 2 && diag >= 2
                        && diag <= size / 2) {
                    ball = new Ball(getHumanColor(), Player.HUMAN, row, diag);
                    humanBalls.add(ball);
                } else if (row >= size - 2 || row == size - 3
                        && diag <= size - 3 && diag >= size / 2) {
                    ball = new Ball(getHumanColor().other(), Player.MACHINE,
                            row, diag);
                    machineBalls.add(ball);
                }
                board[row][diag] = ball;
            }
        }

        // Machine balls need to be reversed to have the correct order.
        Collections.reverse(machineBalls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getOpeningPlayer() {
        return openingPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getHumanColor() {
        return getOpeningPlayer() == Player.HUMAN ? Color.BLACK : Color.WHITE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getNextPlayer() {
        if (isGameOver()) {
            throw new IllegalStateException("Game is already over!");
        } else {
            return nextPlayer;
        }
    }

    /**
     * Sets the next player to the player who has not made the last move if
     * possible.
     */
    private void setNextPlayer() {
        for (Ball ball : getListOfBalls(nextPlayer.other())) {
            if (!getPossibleMoves(ball).isEmpty()) {
                nextPlayer = nextPlayer.other();
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidPosition(int row, int diag) {
        boolean isValidRow = 0 <= row && row < getSize();
        boolean isValidDiag = getFirstDiag(row) <= diag
                && diag <= getLastDiag(row);
        return isValidRow && isValidDiag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidTarget(int row, int diag) {
        boolean isValidRow = -1 <= row && row <= getSize();
        boolean isValidDiag = getFirstDiag(row) - 1 <= diag
                && diag <= getLastDiag(row) + 1;
        return isValidRow && isValidDiag;
    }

    /**
     * Get the first valid diagonal index in the matrix for the given row.
     *
     * @param row The row to compute the first diagonal index.
     * @return The first diagonal index.
     */
    private int getFirstDiag(int row) {
        return Math.max(0, row - getSize() / 2);
    }

    /**
     * Get the last valid diagonal index in the matrix for the given row.
     *
     * @param row The row to compute the last diagonal index.
     * @return The last diagonal index.
     */
    private int getLastDiag(int row) {
        int size = getSize();
        return Math.min(row + size / 2, size - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board move(int rowFrom, int diagFrom, int rowTo, int diagTo) {
        if (isGameOver()) {
            throw new IllegalStateException("The game is already over!");
        } else if (nextPlayer != Player.HUMAN) {
            throw new IllegalStateException("It is the machines turn!");
        } else if (!isValidPosition(rowFrom, diagFrom)) {
            throw new IllegalArgumentException("Invalid position!");
        } else if (!isValidTarget(rowTo, diagTo)) {
            throw new IllegalArgumentException("Invalid target coordinates!");
        } else {
            Move move = new Move(rowFrom, diagFrom, rowTo, diagTo);
            Ball ball = board[rowFrom][diagFrom];

            if (ball != null && ball.getOwner() == Player.HUMAN
                    && isValidMove(move)) {
                return executeMove(move);
            } else {
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Board machineMove() {
        if (isGameOver()) {
            throw new IllegalStateException("Game is already over!");
        } else if (nextPlayer != Player.MACHINE) {
            throw new IllegalStateException("It is the human's turn!");
        } else {
            TreeNode root = new TreeNode(null);
            buildGameTree(root, 0);

            // Use infinity to make sure the first move overwrites this
            // variable.
            double bestScore = Double.NEGATIVE_INFINITY;
            Move bestMove = null;

            // Get the child of the root with the highest score and execute its
            // move.
            for (TreeNode child : root.getChildren()) {
                double childScore = child.getScore();

                if (childScore > bestScore) {
                    bestScore = childScore;
                    bestMove = child.getMove();
                }
            }
            return executeMove(bestMove);
        }
    }

    /**
     * Create all child nodes if the maximum height is not yet reached and
     * the current abalone game is not over.
     * Also set the score of the parent node while the game tree is build.
     *
     * @param parent The node to add the children.
     * @param height The height of the parent node in the tree.
     */
    private void buildGameTree(TreeNode parent, int height) {
        double score = getSize() * getDifferenceScore() + getPositionScore()
                + getWinnerScore(height);

        // Create all child nodes if the maximum height is not yet reached and
        // the current abalone game is not over.
        if (height < difficultyLevel && !isGameOver()) {
            // Use infinity to make sure the first move overwrites this
            // variable.
            double bestChildScore = nextPlayer == Player.HUMAN
                    ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;

            for (Ball ball : getListOfBalls(nextPlayer)) {
                for (Move move : getPossibleMoves(ball)) {
                    // Recursively build the game tree.
                    AbaloneBoard child = executeMove(move);
                    TreeNode node = new TreeNode(move);
                    child.buildGameTree(node, height + 1);

                    // After the subtree of the child node has been built the
                    // child score can be accessed.
                    double childScore = node.getScore();

                    // Get the best child score for the machine with the Minimax
                    // algorithm.
                    if (nextPlayer == Player.HUMAN) {
                        bestChildScore = Math.min(bestChildScore, childScore);
                    } else {
                        bestChildScore = Math.max(bestChildScore, childScore);
                    }
                    parent.addChild(node);
                }
            }
            score += bestChildScore;
        }
        parent.setScore(score);
    }

    /**
     * Get the score of the abalone board based on the moves a player needs
     * to win the game.
     *
     * @param height The number of moves for anyone to archive a win.
     * @return The score.
     */
    private double getWinnerScore(int height) {
        double score = 0;

        if (isGameOver()) {
            if (getWinner() == Player.HUMAN) {
                score = -1.5 * 5_000_000 / height;
            } else {
                score = 5_000_000.0 / height;
            }
        }
        return score;
    }

    /**
     * Get the score of the abalone board based on the difference of the number
     * of balls each player has.
     *
     * @return The score.
     */
    private double getDifferenceScore() {
        return machineBalls.size() - 1.5 * humanBalls.size();
    }

    /**
     * Get the score of the abalone board based on the position of each ball.
     *
     * @return The score.
     */
    private double getPositionScore() {
        return getDistanceSum(Player.MACHINE) - 1.5
                * getDistanceSum(Player.HUMAN);
    }

    /**
     * Get the sum of the distances of each ball of the given player to the edge
     * of the abalone board.
     *
     * @param player The player to compute the sum.
     * @return The sum of the distances of the balls of the given player.
     */
    private int getDistanceSum(Player player) {
        int size = getSize();
        int sum = 0;

        for (Ball ball : getListOfBalls(player)) {
            sum += ball.distToEdge(size);
        }
        return sum;
    }

    /**
     * Checks if this move operation is possible on the given board.
     * The owner of the moved ball is ignored.
     *
     * @param move The move which should be executed on the board.
     * @return {@code true} iff this move operation is possible.
     */
    private boolean isValidMove(Move move) {
        int row = move.getRowFrom();
        int diag = move.getDiagFrom();
        int rowDiff = move.getRowTo() - row;
        int diagDiff = move.getDiagTo() - diag;
        Ball ball = board[row][diag];

        // Check if the target slot is next to the current slot.
        // If the current slot is emtpy the move is invalid.
        if (Math.abs(rowDiff) > 1 || Math.abs(diagDiff) > 1
                || rowDiff + diagDiff == 0 || ball == null) {
            return false;
        }

        Color initialColor = ball.getColor();
        Color lastColor = initialColor;
        int colorChanges = 0;
        int blackCounter = initialColor == Color.BLACK ? 1 : 0;
        int whiteCounter = initialColor == Color.WHITE ? 1 : 0;

        // Count the color changes and the number of balls of each color from
        // the start ball along the directional vector until there is a free
        // slot.
        do {
            row += rowDiff;
            diag += diagDiff;

            if (isValidPosition(row, diag)) {
                ball = board[row][diag];

                if (ball != null) {
                    Color color = ball.getColor();

                    if (lastColor != color) {
                        lastColor = color;
                        colorChanges++;
                    }

                    if (color == Color.BLACK) {
                        blackCounter++;
                    } else {
                        whiteCounter++;
                    }
                }
            } else {
                ball = null;
            }
        } while (ball != null);

        // There must be more balls with the initial color than with the enemy
        // color to make a valid move and only one or no color change is
        // allowed.
        return !((initialColor == Color.BLACK && blackCounter <= whiteCounter)
                || (initialColor == Color.WHITE && whiteCounter <= blackCounter)
                || colorChanges > 1);
    }

    /**
     * Executes a move on a clone of the given board without checking if the
     * move is valid.
     *
     * @param move The move to execute.
     * @return A clone of the current board with the executed move.
     */
    private AbaloneBoard executeMove(Move move) {
        AbaloneBoard clone = clone();
        int row = move.getRowFrom();
        int diag = move.getDiagFrom();
        int rowDiff = move.getRowTo() - row;
        int diagDiff = move.getDiagTo() - diag;
        Ball previousBall = null;
        Ball ball;

        // Move all balls in one direction.
        do {
            ball = clone.board[row][diag];
            clone.board[row][diag] = previousBall;

            // Update the coordinates of the ball to represent its new place.
            if (previousBall != null) {
                previousBall.setRow(row);
                previousBall.setDiag(diag);
            }

            // Repeat until a slot is emtpy and ball is null.
            if (ball != null) {
                previousBall = ball;
                row += rowDiff;
                diag += diagDiff;
            }
        } while (clone.isValidPosition(row, diag) && ball != null);

        // The current position is not valid any more and the previous ball is
        // not in the board anymore so it needs to be removed from the the
        // corresponding list.
        if (previousBall != null && !clone.isValidPosition(row, diag)) {
            clone.getListOfBalls(previousBall.getOwner()).remove(previousBall);
        }
        clone.setNextPlayer();
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must bigger than 0!");
        } else {
            difficultyLevel = level;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOver() {
        return startBalls - humanBalls.size() >= ELIM
                || startBalls - machineBalls.size() >= ELIM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        if (!isGameOver()) {
            throw new IllegalStateException("Game is not over yet!");
        } else {
            if (humanBalls.size() < machineBalls.size()) {
                return Player.MACHINE;
            } else {
                return Player.HUMAN;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfBalls(Color color) {
        if (color == getHumanColor()) {
            return humanBalls.size();
        } else if (color == getHumanColor().other()) {
            return machineBalls.size();
        } else {
            throw new IllegalArgumentException("Color NONE has no balls!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getSlot(int row, int diag) {
        if (!isValidPosition(row, diag)) {
            throw new IllegalArgumentException("Invalid coordinates!");
        } else {
            Ball ball = board[row][diag];

            if (ball == null) {
                return Color.NONE;
            } else {
                return ball.getColor();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return board.length;
    }

    /**
     * Get the list of balls of the given player.
     *
     * @param player The player.
     * @return The list of balls of the given player.
     */
    private List<Ball> getListOfBalls(Player player) {
        return player == Player.HUMAN ? humanBalls : machineBalls;
    }

    /**
     * Get a list of possible moves of a ball.
     *
     * @param ball The ball to be moved.
     * @return A list with valid moves.
     */
    private List<Move> getPossibleMoves(Ball ball) {
        int row = ball.getRow();
        int diag = ball.getDiag();
        List<Move> moves = new LinkedList<>();

        for (int[] vector : VALID_MOVE_VECTORS) {
            Move move = new Move(row, diag, row + vector[0], diag + vector[1]);

            if (isValidMove(move)) {
                moves.add(move);
            }
        }
        return moves;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        int size = getSize();
        StringBuilder sb = new StringBuilder();

        for (int row = size - 1; row >= 0; row--) {
            int indentation = Math.abs(row - size / 2);
            int spaceCounter = 0;

            // Add indentation in front.
            for (int i = 0; i < indentation; i++) {
                sb.append(' ');
            }

            // Append the content of the row.
            for (int diag = getFirstDiag(row); diag <= getLastDiag(row);
                 diag++) {
                sb.append(getSlot(row, diag));

                // Only append a space if there are following slots.
                if (++spaceCounter < size - indentation) {
                    sb.append(' ');
                }
            }

            // Only append a newline if there are following rows.
            if (row > 0) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Get a deep copy of the abalone board.
     *
     * @return A deep copy of the abalone board.
     */
    @Override
    public AbaloneBoard clone() {
        AbaloneBoard abalone;

        try {
            abalone = (AbaloneBoard) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
        int size = getSize();
        abalone.board = new Ball[size][size];
        abalone.humanBalls = abalone.cloneBalls(humanBalls);
        abalone.machineBalls = abalone.cloneBalls(machineBalls);
        return abalone;
    }

    /**
     * Clone the given list of balls and add them to a new list.
     * Also add the cloned balls to the correct position of the given board.
     *
     * @param oldBalls The balls to be cloned.
     * @return New list of cloned balls.
     */
    private List<Ball> cloneBalls(List<Ball> oldBalls) {
        List<Ball> balls = new LinkedList<>();

        for (Ball oldBall : oldBalls) {
            Ball ball = oldBall.clone();
            balls.add(ball);
            board[ball.getRow()][ball.getDiag()] = ball;
        }
        return balls;
    }
}
