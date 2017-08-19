package abalone.model;

/**
 * A abalone player.
 */
public enum Player {
    /**
     * Human player.
     */
    HUMAN {
        /**
         * {@inheritDoc}
         */
        @Override
        public Player other() {
            return MACHINE;
        }
    },

    /**
     * Machine player.
     */
    MACHINE {
        /**
         * {@inheritDoc}
         */
        @Override
        public Player other() {
            return HUMAN;
        }
    };

    /**
     * Get the other player.
     *
     * @return The other player.
     */
    public abstract Player other();
}
