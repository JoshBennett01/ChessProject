package dev.JoshBennett.chess.controller;

import java.util.Scanner;

import dev.JoshBennett.chess.logic.Coord;

/**
 * Handles user input for the console chess game.
 *
 * Responsibilities: - Reads player commands from the console - Validates input
 * format (e.g., "a1" to "h8") - Converts valid input into Coord objects
 *
 * Returns null for special commands like "redo".
 */
public class ConsoleController {

    private final Scanner scanner = new Scanner(System.in);

    public Coord handleInputs() {
        String s;
        while (true) {
            s = scanner.nextLine().trim().toLowerCase();
            if (s.equals("redo")) {
                return null;
            }
            if (checkInput(s)) {
                break;
            }
            System.out.println("Please enter moves in the format [a-h][1-8]. Example: B4");
        }
        char file = s.charAt(0);
        char rank = s.charAt(1);

        int col = file - 'a';
        int row = rank - '1';
        return new Coord(row, col);
    }

    private boolean checkInput(String s) {
        return s.matches("^[a-h][1-8]$");
    }

    public String handlePromotionInput() {
        String s;

        while (true) {
            s = scanner.nextLine().trim().toLowerCase();

            switch (s) {
                case "q", "queen" -> {
                    return "Q";
                }
                case "r", "rook" -> {
                    return "R";
                }

                case "b", "bishop" -> {
                    return "B";
                }
                case "n", "knight" -> {
                    return "N";
                }

                default ->
                    System.out.println("Invalid input. Enter q, r, b, or n:");
            }
        }
    }
}
