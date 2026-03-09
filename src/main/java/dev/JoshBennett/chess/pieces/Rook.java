package dev.JoshBennett.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.logic.SquareStatus;

public class Rook extends ChessPiece {

    private boolean firstMove = true;

    public Rook(boolean isWhite) {
        super(isWhite);
    }

    public void setFirstMoveFalse() {
        firstMove = false;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    @Override
    public Coord[] availableMoves(GameMaster gm, Coord pos) {
        List<Coord> moves = new ArrayList<>();
        int row = pos.getCoordRow();
        int col = pos.getCoordCol();

        int[][] delta = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
        };

        for (int[] d : delta) {
            int r = row;
            int c = col;
            OUTER:
            for (int i = 0; i < 7; i++) {
                r = r + d[0];
                c = c + d[1];
                if (outOfBounds(r, c)) {
                    break;
                }
                SquareStatus status = gm.getSquareStatus(r, c, isWhite);
                switch (status) {
                    case ENEMY -> {
                        if (new Coord(r, c).equals((isWhite ? gm.getBlackKing() : gm.getWhiteKing()))) {
                            gm.addAttackerPos(pos, d);
                        }
                        moves.add(new Coord(r, c));
                        break OUTER;
                    }
                    case ALLY -> {
                        break OUTER;
                    }
                    default ->
                        moves.add(new Coord(r, c));
                }
            }
        }
        return moves.toArray(Coord[]::new);
    }
}
