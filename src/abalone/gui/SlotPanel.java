package abalone.gui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * A panel which represents a slot on the abalone board in the GUI.
 */
public class SlotPanel extends JPanel {
    private static final Stroke STROKE = new BasicStroke(2);
    private final boolean isVisible;
    private final int row;
    private final int diag;
    private boolean isSelected = false;
    private boolean isTarget = false;

    /**
     * Create a new slot panel.
     *
     * @param row The row.
     * @param diag The diagonal.
     * @param isVisible If the slot is a valid ball position.
     */
    public SlotPanel(int row, int diag, boolean isVisible) {
        this.row = row;
        this.diag = diag;
        this.isVisible = isVisible;
    }

    /**
     * Get the row of this slot.
     *
     * @return The row.
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the diagonal of this slot.
     *
     * @return The diagonal.
     */
    public int getDiag() {
        return diag;
    }

    /**
     * Get if the slot is selected.
     *
     * @return If the slot is selected.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Set if the slot is selected.
     *
     * @param isSelected If the slot is selected.
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        repaint();
    }

    /**
     * Get if the slot is a valid target from the selected slot.
     *
     * @return If the slot is a valid target.
     */
    public boolean isTarget() {
        return isTarget;
    }

    /**
     * Set if the slot is a valid target of the selected slot.
     *
     * @param isTarget If the slot is a valid target.
     */
    public void setTarget(boolean isTarget) {
        this.isTarget = isTarget;
        repaint();
    }

    /**
     * Get if the slot is a valid slot on the abalone board.
     *
     * @return If the slot is a valid slot.
     */
    public boolean isValidSlot() {
        return isVisible;
    }

    /**
     * This method gets called when repaint() is called.
     * If the slot is visible it gets a black border and the color of its ball
     * or orange if the slot is empty
     *
     * @param g The graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Enable anti-aliasing.
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Make the circle of the slot as large as possible.
        int diameter = Math.min(height, width);
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2;

        int smallDiameter = diameter / 2;
        int smallX = x + smallDiameter / 2;
        int smallY = y + smallDiameter / 2;

        if (isVisible) {
            // Draw the circle with the color of the ball in this slot.
            fillCircle(g, x, y, diameter, getColor());

            // Mark the slot as selected.
            if (isSelected) {
                fillCircle(g, smallX, smallY, smallDiameter, Color.RED);
            }
        }

        // Highlight target slots.
        if (isTarget) {
            fillCircle(g, smallX, smallY, smallDiameter, Color.GREEN);
        }
    }

    /**
     * Draw a circle. If the circle is white, a black border is added.
     *
     * @param g The graphics context.
     * @param x The x offset.
     * @param y The y offset.
     * @param diameter The diameter.
     * @param color The color.
     */
    private static void fillCircle(Graphics g, int x, int y, int diameter,
                                   Color color) {
        g.setColor(color);
        g.fillOval(x, y, diameter, diameter);

        // Draw a border for white circles.
        if (color == Color.WHITE) {
            g.setColor(Color.BLACK);
            ((Graphics2D) g).setStroke(STROKE);
            g.drawOval(x + 1, y + 1, diameter - 3, diameter - 3);
        }
    }

    /**
     * Get the color of the slot which is the color of the ball or orange if
     * the slot is emtpy.
     *
     * @return The color.
     */
    private Color getColor() {
        switch (((GridPanel) getParent()).getSlotColor(row, diag)) {
            case WHITE:
                return Color.WHITE;
            case BLACK:
                return Color.BLACK;
            default:
                return Color.ORANGE;
        }
    }
}
