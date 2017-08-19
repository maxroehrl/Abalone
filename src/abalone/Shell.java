package abalone;

import abalone.model.AbaloneBoard;
import abalone.model.Board;
import abalone.model.Color;
import abalone.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Interactive shell for user input and program output.
 */
public final class Shell {
    /**
     * The difficulty level the machine uses for its game tree.
     */
    private static int difficultyLevel = 2;

    /**
     * Cannot instantiate utility class.
     */
    private Shell() {
    }

    /**
     * Main method of the Abalone interactive shell.
     *
     * @param args No arguments are used.
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        BufferedReader stdin
                = new BufferedReader(new InputStreamReader(System.in));
        Board abalone = new AbaloneBoard();
        String input;
        boolean quit = false;

        while (!quit) {
            System.out.print("abalone> ");
            input = stdin.readLine();

            if (input == null) {
                break;
            }

            // Split input string by whitespaces.
            String[] tokens = input.trim().split("\\s+");

            // Ignore emtpy input and whitespaces.
            if (!tokens[0].isEmpty()) {
                // Use the first letter to identify a command.
                switch (tokens[0].toLowerCase().charAt(0)) {
                    case 'm':
                        abalone = cmdMove(abalone, tokens);
                        break;
                    case 'l':
                        cmdLevel(abalone, tokens);
                        break;
                    case 's':
                        abalone = cmdSwitch(abalone);
                        break;
                    case 'b':
                        cmdBalls(abalone);
                        break;
                    case 'n':
                        abalone = cmdNew(abalone, tokens);
                        break;
                    case 'p':
                        System.out.println(abalone);
                        break;
                    case 'h':
                        printHelp();
                        break;
                    case 'q':
                        quit = true;
                        break;
                    default:
                        printError("Unknown command!");
                        break;
                }
            }
        }
    }

    /**
     * Check if the tokens array has enough arguments and show an error message
     * if not.
     *
     * @param tokens The tokens array.
     * @param expected The number of expected arguments.
     * @return {@code true} iff there are enough tokens.
     */
    private static boolean hasEnoughArguments(String[] tokens, int expected) {
        if (tokens.length < expected) {
            printError("Missing arguments. Expected: " + expected);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Parse a string to an integer and show an error message if parsing is
     * impossible.
     *
     * @param s The string to be parsed.
     * @return An integer if the string could be parsed or null otherwise.
     */
    private static Integer getInteger(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            printError("Invalid number: " + s);
            return null;
        }
    }

    /**
     * Execute a move command on a copy of the given board.
     *
     * @param abalone The abalone board.
     * @param tokens A string array containing at least 5 strings.
     *               The last 4 string should be the coordinates of the move
     *               operation.
     * @return A new board with the executed move or the given board if the
     *         command was invalid.
     */
    private static Board cmdMove(Board abalone, String[] tokens) {
        if (hasEnoughArguments(tokens, 5)) {
            Integer rowFrom = getInteger(tokens[1]);
            Integer diagFrom = getInteger(tokens[2]);
            Integer rowTo = getInteger(tokens[3]);
            Integer diagTo = getInteger(tokens[4]);

            if (rowFrom != null && diagFrom != null && rowTo != null
                    && diagTo != null) {
                // Rows and diagonals are 0 indexed in the model.
                rowFrom--;
                diagFrom--;
                rowTo--;
                diagTo--;

                if (abalone.isGameOver()) {
                    printError("Game is already over!");
                } else if (!abalone.isValidPosition(rowFrom, diagFrom)) {
                    printError("Invalid position!");
                } else if (!abalone.isValidTarget(rowTo, diagTo)) {
                    printError("Invalid target coordinates!");
                } else {
                    Board newAbalone
                            = abalone.move(rowFrom, diagFrom, rowTo, diagTo);

                    // If the reference is null the move was not possible.
                    if (newAbalone == null) {
                        printError("Move could not be executed!");
                    } else {
                        // No machine move is possible if the game is over.
                        if (newAbalone.isGameOver()) {
                            printWinner(newAbalone);
                            abalone = newAbalone;
                        } else {
                            abalone = cmdMachineMove(newAbalone);
                        }
                    }
                }
            }
        }
        return abalone;
    }

    /**
     * Execute machine moves on the given board as long it is the machine's
     * turn.
     *
     * @param abalone The abalone board.
     * @return The board with the executed move.
     */
    private static Board cmdMachineMove(Board abalone) {
        if (abalone.getNextPlayer() == Player.HUMAN) {
            System.out.println("I must skip (no possible moves).");
        } else {
            abalone = abalone.machineMove();

            if (abalone.isGameOver()) {
                printWinner(abalone);
            } else if (abalone.getNextPlayer() == Player.MACHINE) {
                System.out.println("You must skip (no possible moves).");
                abalone = cmdMachineMove(abalone);
            }
        }
        return abalone;
    }

    /**
     * Print a message if a player has won the game.
     *
     * @param abalone The abalone board.
     */
    private static void printWinner(Board abalone) {
        if (abalone.getWinner() == Player.HUMAN) {
            System.out.println("Congratulations! You won.");
        } else {
            System.out.println("Sorry! Machine wins.");
        }
    }

    /**
     * Create a new board with the given size.
     *
     * @param abalone The old abalone board.
     * @param tokens A string array containing at least 2 string where the last
     *               represents the new size of the board.
     * @return The new abalone board.
     */
    private static Board cmdNew(Board abalone, String[] tokens) {
        if (hasEnoughArguments(tokens, 2)) {
            Integer size = getInteger(tokens[1]);

            if (size != null) {
                if (size < Board.MIN_SIZE || size % 2 == 0) {
                    printError("Board size must be odd and bigger than 6.");
                } else {
                    abalone = createNewBoard(size, abalone.getOpeningPlayer());
                }
            }
        }
        return abalone;
    }

    /**
     * Create a new board with switched opening player.
     * The size of the game does not change.
     *
     * @param abalone The old abalone board.
     * @return The new abalone board.
     */
    private static Board cmdSwitch(Board abalone) {
        return createNewBoard(abalone.getSize(),
                abalone.getOpeningPlayer().other());
    }

    /**
     * Create a new abalone board with the given size and opening player.
     *
     * @param size The size of the new board.
     * @param openingPlayer The new opening player.
     * @return The new abalone board.
     */
    private static Board createNewBoard(int size, Player openingPlayer) {
        Board abalone = new AbaloneBoard(size, openingPlayer, difficultyLevel);

        // If the machine opens the game it now makes a move.
        if (openingPlayer == Player.MACHINE) {
            abalone = cmdMachineMove(abalone);
        }
        System.out.printf("New game started. You are %s.\n",
                abalone.getHumanColor());
        return abalone;
    }

    /**
     * Set the new difficulty level of the machine which is the tree height the
     * machine uses to make its next move.
     *
     * @param abalone The abalone board.
     * @param tokens A string array containing at least 2 string where the last
     *               represents the new difficulty level.
     */
    private static void cmdLevel(Board abalone, String[] tokens) {
        if (hasEnoughArguments(tokens, 2)) {
            Integer level = getInteger(tokens[1]);

            if (level != null) {
                if (level < 1) {
                    printError("Level must be bigger than 1!");
                } else {
                    abalone.setLevel(level);
                    difficultyLevel = level;
                }
            }
        }
    }

    /**
     * Print the number of balls of each color.
     *
     * @param abalone The abalone board.
     */
    private static void cmdBalls(Board abalone) {
        System.out.printf("%s: %s\n", Color.BLACK,
                abalone.getNumberOfBalls(Color.BLACK));
        System.out.printf("%s: %s\n", Color.WHITE,
                abalone.getNumberOfBalls(Color.WHITE));
    }

    /**
     * Print a help text to show the functionality of the program.
     */
    private static void printHelp() {
        System.out.println("A abalone game where a human player plays "
                + "against the machine.\nSupported commands:");
        System.out.println("\tnew s\t\t\t\tCreate a new board with size s.");
        System.out.println("\tswitch\t\t\t\tSwitch opening player.");
        System.out.println("\tmove r1 d1 r2 d2\tMove the ball at row r1 and "
                + "diagonal d1 to row r2 and diagonal d2.");
        System.out.println("\tlevel l\t\t\t\tSet the difficulty level of the "
                + "machine.");
        System.out.println("\tballs\t\t\t\tShow the number of balls of each "
                + "player.");
        System.out.println("\tprint\t\t\t\tShow the abalone board.");
        System.out.println("\thelp\t\t\t\tShow this message.");
        System.out.println("\tquit\t\t\t\tQuit the program.");
    }

    /**
     * Show an error message to the user.
     *
     * @param message The error message to be displayed.
     */
    private static void printError(String message) {
        System.out.println("Error! " + message);
    }
}
