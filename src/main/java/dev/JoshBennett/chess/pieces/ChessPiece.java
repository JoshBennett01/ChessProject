package dev.JoshBennett.chess.pieces;

import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;

/**
 * Base class for all chess pieces.
 *
 * Responsibilities: - Tracks the piece's team (white or black) - Provides an
 * interface for calculating available moves - Utility for checking board
 * boundaries
 */
public abstract class ChessPiece {

    boolean isWhite;

    /**
     * Creates a chess piece for the specified team.
     *
     * @param isWhite true for white, false for black
     */
    public ChessPiece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    /**
     * Calculates all valid moves for this piece from the given position.
     *
     * @param gm the game master
     * @param pos the piece's current position
     * @return array of valid coordinates the piece can move to
     */
    public abstract Coord[] availableMoves(GameMaster gm, Coord pos);

    /**
     * @return true if this piece is white, false if black
     */
    public boolean getTeam() {
        return this.isWhite;
    }

    /**
     * Checks whether a coordinate is outside the board.
     *
     * @param row row index
     * @param col column index
     * @return true if out-of-bounds, false otherwise
     */
    boolean outOfBounds(int row, int col) {
        return row < 0 || row > 7 || col < 0 || col > 7;
    }
}
