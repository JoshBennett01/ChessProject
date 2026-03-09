package dev.JoshBennett.chess.logic;

import java.util.Objects;

/**
 * Represents a coordinate on the chess board.
 *
 * The coordinate is zero-indexed internally: - row: 0–7 (rank 1–8) - col: 0–7
 * (file a–h)
 *
 * Immutable value object used for board indexing and move handling.
 */
public class Coord {

    private final int row;
    private final int col;

    public Coord(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getCoordRow() {
        return this.row;
    }

    public int getCoordCol() {
        return this.col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coord coord = (Coord) o;
        return row == coord.row && col == coord.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        char file = (char) ('a' + col);
        char rank = (char) ('1' + row);
        return "" + file + rank;
    }
}
