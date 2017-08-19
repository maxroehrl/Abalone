package abalone.gui;

import abalone.model.AbaloneBoard;
import abalone.model.Board;
import abalone.model.Color;
import abalone.model.Player;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

/**
 * The grid panel of the abalone game which contains the slot panels.
 */
public class GridPanel extends JPanel {
    private static final int[][] VALID_MOVE_VECTORS
            = {{0, 1}, {1, 1}, {1, 0}, {0, -1}, {-1, -1}, {-1, 0}};
    private final MouseListener slotClickListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            clickSlot((SlotPanel) e.getSource());
        }
    };
    private final List<SlotPanel> slots = new LinkedList<>();
    private final List<SlotPanel> validTargetSlots = new LinkedList<>();
    private SlotPanel selectedSlot;
    private Board abalone;
    private Thread machineThread;
    private Integer newLevel;
    private int level = 2;

    /**
     * Create a new grid panel.
     */
    public GridPanel() {
        abalone = new AbaloneBoard();
        initGrid(abalone.getSize());

        // Menu labels get updated when the frame creation has finished as it
        // needs this grid panel to be added to the game frame to access the
        // menu panel.
        SwingUtilities.invokeLater(this::updateMenuLabels);
    }

    /**
     * Initialize the grid layout
     *
     * @param size The size of the abalone board.
     */
    private void initGrid(int size) {
        int cols = 2 * size + 3;
        int halfSize = size / 2;
        boolean isHalfSizeEven = (halfSize & 1) == 0;

        setLayout(new GridLayout(size + 2, cols));

        // Fill the grid layout with slot panels.
        for (int row = size; row >= -1; row--) {
            for (int col = 0; col < cols; col++) {
                // Only every second grid entry can be a slot.
                boolean isSlot = (col & 1) == (row & 1) == isHalfSizeEven;
                int diag = (col + row - halfSize) / 2 - 1;

                if (isSlot && abalone.isValidTarget(row, diag)) {
                    SlotPanel slot = new SlotPanel(row, diag,
                            abalone.isValidPosition(row, diag));
                    slot.addMouseListener(slotClickListener);
                    slots.add(slot);
                    add(slot);
                } else {
                    // Invisible grid entries.
                    add(new JPanel());
                }
            }
        }
    }

    /**
     * Execute machine moves on the abalone board as long it is the machine's
     * turn in another thread.
     */
    private void machineMove() {
        if (abalone.getNextPlayer() == Player.HUMAN) {
            showMessage("I must skip (no possible moves).");
        } else {
            // Create a new thread for the machine move.
            machineThread = new Thread(() -> {
                Board board = abalone.machineMove();
                SwingUtilities.invokeLater(() -> machineMoveFinished(board));
            });
            machineThread.setPriority(Thread.MIN_PRIORITY);
            machineThread.setName("MachineMove-Thread");
            machineThread.start();
        }
    }

    /**
     * This method gets called from the Swing thread when the machine thread
     * has finished.
     *
     * @param abalone The new abalone board.
     */
    private void machineMoveFinished(Board abalone) {
        machineThread = null;
        this.abalone = abalone;
        updateAllSlots();

        // If the level was changed while the thread was running we update it.
        updateLevel();

        if (abalone.isGameOver()) {
            showWinner();
        } else if (abalone.getNextPlayer() == Player.MACHINE) {
            showMessage("You must skip (no possible moves).");
            machineMove();
        }
    }

    /**
     * Show a message if a player has won the game.
     */
    private void showWinner() {
        if (abalone.getWinner() == Player.HUMAN) {
            showMessage("Congratulations! You won.\nUse switch or select a new "
                    + "game size to start a new game.");
        } else {
            showMessage("Sorry! Machine wins.\nUse switch or select a new game"
                    + "size to start a new game.");
        }
    }

    /**
     * Start a new game with the given size.
     *
     * @param size The size of the new game.
     */
    public void newGame(int size) {
        boolean reInitGrid = size != abalone.getSize();
        createNewBoard(size, abalone.getOpeningPlayer());

        if (reInitGrid) {
            slots.clear();
            removeAll();
            initGrid(size);
        }
        updateAllSlots();
    }

    /**
     * Switch opening players.
     */
    public void switchPlayers() {
        createNewBoard(abalone.getSize(), abalone.getOpeningPlayer().other());
        updateAllSlots();
    }

    /**
     * Create a new abalone board with the given size and opening player.
     *
     * @param size The size of the new board.
     * @param openingPlayer The new opening player.
     */
    private void createNewBoard(int size, Player openingPlayer) {
        stopMachineThread();

        // If the machine thread was running a new level could be set.
        updateLevel();
        abalone = new AbaloneBoard(size, openingPlayer, level);

        // If the machine opens the game it now makes a move.
        if (openingPlayer == Player.MACHINE) {
            machineMove();
        }
    }

    /**
     * Set the new difficulty level of the machine which is the tree height the
     * machine uses to make its next move.
     *
     * @param level The new difficulty level.
     */
    public void setLevel(int level) {
        // If the machine thread is running the difficulty level gets set when
        // the thread finishes by calling this method again.
        if (machineThread != null) {
            newLevel = level;
        } else {
            this.level = level;
            abalone.setLevel(level);
            updateMenuLabels();
        }
    }

    /**
     * Update the level if a new level was set while the machine thread was
     * running.
     */
    private void updateLevel() {
        if (newLevel != null) {
            setLevel(newLevel);
            newLevel = null;
        }
    }

    /**
     * Stop the machine thread if it is running.
     */
    @SuppressWarnings("deprecation")
    public void stopMachineThread() {
        if (machineThread != null) {
            machineThread.stop();
            machineThread = null;
        }
    }

    /**
     * This method gets called when a slot gets clicked.
     * If the slot contains a human ball it gets selected and valid target get
     * marked.
     * If there is already a selected slot a move is executed if a target slot
     * is clicked or the selection gets abandoned if the already selected slot
     * gets clicked.
     *
     * @param slot The clicked slot.
     */
    private void clickSlot(SlotPanel slot) {
        // If the machine thread is running or the game is over no clicks are
        // recognized.
        if (machineThread == null && !abalone.isGameOver()) {
            if (selectedSlot != null) {
                if (!slot.isSelected() && slot.isTarget()) {
                    // A slot was selected before and this slot is the target of
                    // the move.
                    moveSelectedSlotTo(slot);
                } else if (slot.isSelected()) {
                    // If the selected slot gets clicked again it gets
                    // deselected.
                    deselectAllSlots();
                }
            } else if (slot.isValidSlot()
                    && getSlotColor(slot.getRow(), slot.getDiag())
                    == abalone.getHumanColor()) {
                selectSlot(slot);
            }
        }
    }

    /**
     * Set the given slot as the selected one and mark valid target slots.
     *
     * @param slot The slot to select.
     */
    private void selectSlot(SlotPanel slot) {
        // The slot gets selected.
        selectedSlot = slot;
        selectedSlot.setSelected(true);

        // Mark the valid target slots.
        int row = slot.getRow();
        int diag = slot.getDiag();

        for (int[] vector : VALID_MOVE_VECTORS) {
            int targetRow = row + vector[0];
            int targetDiag = diag + vector[1];
            Board board = abalone.move(row, diag, targetRow, targetDiag);

            if (board != null) {
                SlotPanel targetSlot = getSlot(targetRow, targetDiag);

                if (targetSlot != null) {
                    validTargetSlots.add(targetSlot);
                    targetSlot.setTarget(true);
                }
            }
        }
    }

    /**
     * Move the ball in the selected slot to the given slot.
     *
     * @param targetSlot the target slot.
     */
    private void moveSelectedSlotTo(SlotPanel targetSlot) {
        abalone = abalone.move(selectedSlot.getRow(), selectedSlot.getDiag(),
                targetSlot.getRow(), targetSlot.getDiag());
        updateAllSlots();

        if (abalone.isGameOver()) {
            showWinner();
        } else {
            machineMove();
        }
    }

    /**
     * Get the slot panel at the given row and diagonal.
     *
     * @param row The row.
     * @param diag The diagonal.
     * @return The slot panel or null if coordinates are invalid.
     */
    private SlotPanel getSlot(int row, int diag) {
        for (SlotPanel slot : slots) {
            if (slot.getRow() == row && slot.getDiag() == diag) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Get the color of the slot at the given coordinates.
     *
     * @param row The row.
     * @param diag The diagonal.
     * @return The color of the slot.
     */
    public Color getSlotColor(int row, int diag) {
        if (!abalone.isValidPosition(row, diag)) {
            throw new IllegalArgumentException("Invalid coordinates!");
        } else {
            return abalone.getSlot(row, diag);
        }
    }

    /**
     * Reset all slots and clear the valid target slots.
     */
    private void deselectAllSlots() {
        for (SlotPanel slot : validTargetSlots) {
            slot.setTarget(false);
        }
        validTargetSlots.clear();

        if (selectedSlot != null) {
            selectedSlot.setSelected(false);
            selectedSlot = null;
        }
    }

    /**
     * Deselect all slots and update the grid with updated balls from the model.
     * Also update the number of balls each player has got.
     */
    private void updateAllSlots() {
        updateMenuLabels();
        deselectAllSlots();
        repaint();
    }

    /**
     * Update the menu labels.
     */
    private void updateMenuLabels() {
        int humanBalls = abalone.getNumberOfBalls(abalone.getHumanColor());
        int machineBalls
                = abalone.getNumberOfBalls(abalone.getHumanColor().other());
        getGameFrame().updateMenuLabels(level, machineBalls, humanBalls);
    }

    /**
     * Get the parent game frame.
     *
     * @return The game frame.
     */
    private GameFrame getGameFrame() {
        return (GameFrame) SwingUtilities.getWindowAncestor(this);
    }

    /**
     * Show a message box to the user.
     *
     * @param message The message.
     */
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(getGameFrame(), message);
    }
}
