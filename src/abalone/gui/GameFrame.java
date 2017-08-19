package abalone.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Abalone game window.
 */
public class GameFrame extends JFrame {
    private final MenuPanel menu;

    /**
     * Create a new abalone game window.
     */
    public GameFrame() {
        super("Abalone");

        Container root = getContentPane();
        root.setLayout(new BorderLayout());

        // Setup grid panel
        GridPanel grid = new GridPanel();
        root.add(grid, BorderLayout.CENTER);

        // Setup menu panel
        menu = new MenuPanel(grid, this);
        root.add(menu, BorderLayout.SOUTH);

        // Stop machine thread if the window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                grid.stopMachineThread();
            }
        });

        setSize(1000, 750);
        setMinimumSize(new Dimension(640, 400));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Update the menu labels with the current number of balls and the
     * difficulty level.
     *
     * @param level The difficulty level.
     * @param machineBalls The number of machine balls.
     * @param humanBalls The number of human balls.
     */
    public void updateMenuLabels(int level, int machineBalls, int humanBalls) {
        menu.updateLabels(level, machineBalls, humanBalls);
    }

    /**
     * Create a new abalone game window.
     *
     * @param args The arguments are ignored.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
