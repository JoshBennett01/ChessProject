package dev.JoshBennett.chess.service;

import dev.JoshBennett.chess.controller.ConsoleController;
import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.logic.PlayerTurn;

/**
 * Entry point for the console chess application.
 *
 * Responsible for: - Running the main game loop - Handling turn switching -
 * Coordinating input and game state
 *
 * Delegates rule validation and board state management to GameMaster.
 */
public class ConsoleGameService {

    ConsoleController contr;
    GameMaster gm;

    public ConsoleGameService(GameMaster gm, ConsoleController contr) {
        this.gm = gm;
        this.contr = contr;
    }

    /**
     * Starts the main game loop. Continues until checkmate or manual
     * termination.
     */
    public void startGame() {
        System.out.println("Welcome to the chess prototype!");
        System.out.println("""
            This is the console version, enter inputs in the terminal to play, 
            use [A-H or a-h][1-8] for your moves . You must select the square first enter [redo] to unselect. Gl
            """);

        boolean gameActive = true; // Game status
        PlayerTurn playerTurn = new PlayerTurn(true); // Track player turns
        while (gameActive) {

            System.out.println(gm.getBoardString());

            // If current player is in check, verify whether it is checkmate.
            if (gm.checkChecked(playerTurn.isWhiteTurn())) {

                if (gm.checkMate(playerTurn.isWhiteTurn())) { // End game
                    System.out.println("CheckMate! " + (playerTurn.isWhiteTurn() ? "Black wins" : "White wins"));
                    gm.endGame();
                }

                System.out.println("Check");
            }

            selectSquare(playerTurn.isWhiteTurn());

            if (selectDestination(playerTurn.isWhiteTurn())) {
                if (gm.pawnPromotion(playerTurn.isWhiteTurn())) { // Pawn promotion logic
                    promotePawn(playerTurn.isWhiteTurn());
                }

                playerTurn.switchTurn(); // Switch teams
                gm.setActiveTeam(playerTurn.isWhiteTurn());
            }
        }
    }

    private boolean selectSquare(boolean isWhite) {
        Coord sel;
        while (true) {
            System.out.println("Enter a square to select: ");
            sel = contr.handleInputs();
            if (sel == null) {
                System.out.println("Invalid input, redo is not accepted on square selection");
                continue;
            }
            if (gm.selectSquare(sel.getCoordRow(), sel.getCoordCol(), isWhite)) {
                System.out.println(gm.selectedSquareInfo());
                return true;
            }
            System.out.println("Selected square must have you're own pawn");
        }
    }

    private boolean selectDestination(Boolean isWhite) {
        Coord sel;
        while (true) {
            System.out.println("Enter destination, type redo to reselect: ");
            sel = contr.handleInputs();
            if (sel == null) {
                return false;
            }
            if (gm.movePiece(isWhite, sel.getCoordRow(), sel.getCoordCol())) {
                System.out.println("Pawn moved to " + sel.toString());
                return true;
            }
            System.out.println("Invalid move!");
        }
    }

    private void promotePawn(boolean isWhite) {
        System.out.println("Promote pawn to (q, r, b, n): ");

        gm.setPromotion(isWhite, contr.handlePromotionInput());
    }
}
