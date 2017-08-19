package abalone.gui;

import abalone.model.Board;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;

/**
 * The menu panel of the abalone game.
 */
public class MenuPanel extends JPanel {
    private final JLabel humanLabel = makeCenteredLabel();
    private final JLabel machineLabel = makeCenteredLabel();
    private int selectedSize = Board.MIN_SIZE + 2;

    /**
     * Create a new menu panel.
     *
     * @param grid The grid panel.
     * @param frame The parent frame.
     */
    public MenuPanel(GridPanel grid, JFrame frame) {
        setLayout(new GridLayout(1, 6));
        setBackground(new Color(210, 225, 239));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(humanLabel);
        add(machineLabel);
        add(makeComboBox("Level: ", 3, i -> i + 1, grid::setLevel));
        add(makeComboBox("Size: ", 5, i -> Board.MIN_SIZE + 2 * i,
                i -> selectedSize = i));
        add(makeButton("New", () -> grid.newGame(selectedSize)));
        add(makeButton("Switch", grid::switchPlayers));
        add(makeButton("Quit", frame::dispose));
    }

    /**
     * Make a centered label.
     *
     * @return The label.
     */
    private static JLabel makeCenteredLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * Make a combobox which contains items with a prefix and an integer.
     * The second item always gets selected by default.
     *
     * @param prefix The prefix of every item.
     * @param numberOfItems The number of selectable items which must be bigger
     *                      than 1.
     * @param numerator The function which turns the item index to the
     *                  integer of the item.
     * @param selector The consumer which gets called with the integer of the
     *                 selected item if an item gets selected.
     * @return The combobox.
     */
    private static JComboBox<String> makeComboBox(String prefix,
                                                  int numberOfItems,
                                                  IntUnaryOperator numerator,
                                                  IntConsumer selector) {
        String[] items = new String[numberOfItems];

        // Items with the prefix and the integer of the numerator get created.
        for (int i = 0; i < numberOfItems; i++) {
            items[i] = prefix + numerator.applyAsInt(i);
        }
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setSelectedIndex(1);

        // When a new item gets selected, the selector gets called with the
        // integer of the selected item.
        comboBox.addActionListener(e -> {
            String selection = ((String) comboBox.getSelectedItem())
                    .substring(prefix.length());
            selector.accept(Integer.valueOf(selection));
        });
        return comboBox;
    }

    /**
     * Make a button with the given clicked action.
     *
     * @param text The text of the button.
     * @param clicked The method which gets called when the button gets clicked.
     * @return The button.
     */
    private static JButton makeButton(String text, Runnable clicked) {
        JButton button = new JButton(text);
        button.addActionListener(e -> clicked.run());
        return button;
    }

    /**
     * Update the labels with the current number of balls and the difficulty
     * level.
     *
     * @param level The difficulty level.
     * @param machineBalls The number of machine balls.
     * @param humanBalls The number of human balls.
     */
    public void updateLabels(int level, int machineBalls, int humanBalls) {
        humanLabel.setText(String.format("<html>Human:<br>%s Balls</html>",
                humanBalls));
        machineLabel.setText(String.format("<html>Machine (lvl %s):<br>%s "
                + "Balls</html>", level, machineBalls));
    }
}
