package abalone.model;

/**
 * Color of a ball.
 */
public enum Color {
    /**
     * Black color.
     */
    BLACK("X") {
        /**
         * {@inheritDoc}
         */
        @Override
        public Color other() {
            return WHITE;
        }
    },

    /**
     * White color.
     */
    WHITE("O") {
        /**
         * {@inheritDoc}
         */
        @Override
        public Color other() {
            return BLACK;
        }
    },

    /**
     * No color.
     */
    NONE(".") {
        /**
         * {@inheritDoc}
         */
        @Override
        public Color other() {
            return NONE;
        }
    };

    private final String symbol;

    Color(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Get a 'X' for black, an 'O' for white and a '.' for no color.
     *
     * @return The string representation of this color.
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Get the other color. The other color of NONE is NONE.
     *
     * @return The other color.
     */
    public abstract Color other();
}
