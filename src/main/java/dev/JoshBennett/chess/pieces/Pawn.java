package dev.JoshBennett.chess.pieces;

import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.logic.SquareStatus;

public class Pawn extends ChessPiece {

    boolean firstMove = true;

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    public void setFirstMoveFalse() {
        firstMove = false;
    }

    @Override
    public Coord[] availableMoves(GameMaster gm, Coord pos) {
        Coord[] moves = new Coord[4];
        int row = pos.getCoordRow();
        int col = pos.getCoordCol();
        int r;
        int c;
        SquareStatus status;
        int n = (isWhite ? 1 : -1);

        r = row + n;
        c = col - 1;
        if (!outOfBounds(r, c)) { // attacks
            status = gm.getSquareStatus(r, c, isWhite);
            if (status == SquareStatus.ENEMY || gm.checkGhostPiece(r, c)) {
                moves[0] = new Coord(r, c);
            }
        }
        c = col + 1;
        if (!outOfBounds(r, c)) {
            status = gm.getSquareStatus(r, c, isWhite);
            if (status == SquareStatus.ENEMY || gm.checkGhostPiece(r, c)) {
                moves[1] = new Coord(r, c);
            }
        }

        c = col;
        if (!outOfBounds(r, c)) { // movements
            status = gm.getSquareStatus(r, c, isWhite);
            if (status == SquareStatus.EMPTY) {
                moves[2] = new Coord(r, c);
            }
        }
        r = row + (n + n);
        if (firstMove && !outOfBounds(r, c)) {
            status = gm.getSquareStatus(r, c, isWhite);
            if (status == SquareStatus.EMPTY && moves[2] != null) {
                moves[3] = new Coord(r, c);
            }
        }

        for (Coord move : moves) {
            if (move == null) {
                continue;
            }
            if (new Coord(move.getCoordRow(), move.getCoordCol()).equals((isWhite ? gm.getBlackKing() : gm.getWhiteKing()))) {
                gm.addAttackerPos(pos, new int[0]);
            }
        }
        return moves;
    }
}
