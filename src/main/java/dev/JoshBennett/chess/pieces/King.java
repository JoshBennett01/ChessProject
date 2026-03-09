package dev.JoshBennett.chess.pieces;

import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.logic.SquareStatus;

public class King extends ChessPiece {

    private boolean firstMove = true;

    public King(boolean isWhite) {
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
        Coord[] moves = new Coord[10];
        int row = pos.getCoordRow();
        int col = pos.getCoordCol();
        moves[0] = new Coord(row + 1, col - 1);
        moves[1] = new Coord(row + 1, col + 1);
        moves[2] = new Coord(row - 1, col + 1);
        moves[3] = new Coord(row - 1, col - 1);
        moves[4] = new Coord(row + 1, col);
        moves[5] = new Coord(row, col + 1);
        moves[6] = new Coord(row - 1, col);
        moves[7] = new Coord(row, col - 1);

        if (isFirstMove()) {
            moves[8] = new Coord(row, col + 2);
            moves[9] = new Coord(row, col - 2);
        }

        for (int i = 0; i < moves.length; i++) {
            if (moves[i] == null) {
                continue;
            }
            int r = moves[i].getCoordRow();
            int c = moves[i].getCoordCol();

            if (outOfBounds(r, c)) {
                moves[i] = null;
            } else {
                SquareStatus status = gm.getSquareStatus(r, c, isWhite);
                if (status == SquareStatus.ALLY) {
                    moves[i] = null;
                }
            }
        }
        return moves;
    }
}
