package dev.JoshBennett.chess.pieces;

import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.logic.SquareStatus;

public class Knight extends ChessPiece {

    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public Coord[] availableMoves(GameMaster gm, Coord pos) {
        Coord[] moves = new Coord[8];
        int row = pos.getCoordRow();
        int col = pos.getCoordCol();
        moves[0] = new Coord(row + 2, col - 1);
        moves[1] = new Coord(row + 2, col + 1);
        moves[2] = new Coord(row + 1, col + 2);
        moves[3] = new Coord(row - 1, col + 2);
        moves[4] = new Coord(row - 2, col + 1);
        moves[5] = new Coord(row - 2, col - 1);
        moves[6] = new Coord(row - 1, col - 2);
        moves[7] = new Coord(row + 1, col - 2);

        for (int i = 0; i < moves.length; i++) {
            int r = moves[i].getCoordRow();
            int c = moves[i].getCoordCol();

            if (outOfBounds(r, c)) {
                moves[i] = null;
            } else {
                SquareStatus status = gm.getSquareStatus(r, c, isWhite);
                if (status == SquareStatus.ALLY) {
                    moves[i] = null;
                }
                if (new Coord(r, c).equals((isWhite ? gm.getBlackKing() : gm.getWhiteKing()))) {
                    gm.addAttackerPos(pos, new int[0]);
                }
            }
        }
        return moves;
    }
}
