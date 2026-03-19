package dev.JoshBennett.chess.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.JoshBennett.chess.pieces.Bishop;
import dev.JoshBennett.chess.pieces.ChessPiece;
import dev.JoshBennett.chess.pieces.King;
import dev.JoshBennett.chess.pieces.Knight;
import dev.JoshBennett.chess.pieces.Pawn;
import dev.JoshBennett.chess.pieces.Queen;
import dev.JoshBennett.chess.pieces.Rook;

/**
 * Manages the chess game state and rules.
 *
 * Responsibilities: - Maintains board and piece positions - Tracks kings and
 * special moves (e.g., en passant) - Calculates available moves - Checks for
 * check and checkmate
 */
public class GameMaster {

    private final ChessPiece[][] chessBoard = new ChessPiece[8][8];
    private Coord sel;
    private boolean isWhite;
    private int counter;
    private String fenString = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private int fiftyMoveCounter = 0;

    private Coord whiteKingPos;
    private Coord blackKingPos;

    private boolean castleKingSideWhite;
    private boolean castleQueenSideWhite;
    private boolean castleKingSideBlack;
    private boolean castleQueenSideBlack;

    private Coord ghostPiece;

    private final Set<Coord> attackerPos = new HashSet<>();
    private final List<int[]> attackerDelta = new ArrayList<>();
    private final HashMap<String, Integer> fenMap = new HashMap<>();

    /**
     * Places pieces in their starting positions.
     */
    public void loadGame() {
        for (int i = 0; i < chessBoard[1].length; i++) { // White pawns
            chessBoard[1][i] = new Pawn(true);
        }
        for (int i = 0; i < chessBoard[6].length; i++) { // Black pawns
            chessBoard[6][i] = new Pawn(false);
        }

        // White back line
        chessBoard[0][0] = new Rook(true);
        chessBoard[0][1] = new Knight(true);
        chessBoard[0][2] = new Bishop(true);
        chessBoard[0][3] = new Queen(true);
        chessBoard[0][4] = new King(true);
        whiteKingPos = new Coord(0, 4);
        chessBoard[0][5] = new Bishop(true);
        chessBoard[0][6] = new Knight(true);
        chessBoard[0][7] = new Rook(true);

        // Black back line
        chessBoard[7][0] = new Rook(false);
        chessBoard[7][1] = new Knight(false);
        chessBoard[7][2] = new Bishop(false);
        chessBoard[7][3] = new Queen(false);
        chessBoard[7][4] = new King(false);
        blackKingPos = new Coord(7, 4);
        chessBoard[7][5] = new Bishop(false);
        chessBoard[7][6] = new Knight(false);
        chessBoard[7][7] = new Rook(false);

        // Empty middle
        for (int i = 2; i < chessBoard.length - 2; i++) {
            for (int j = 0; j < chessBoard[i].length; j++) {
                chessBoard[i][j] = null;
            }
        }

        fenString = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        castleKingSideWhite = true;
        castleQueenSideWhite = true;
        castleKingSideBlack = true;
        castleQueenSideBlack = true;
        ghostPiece = null;
        counter = 1;
        fiftyMoveCounter = 0;
        attackerPos.clear();
        attackerDelta.clear();
        fenMap.clear();
    }

    /**
     * Ends the game immediately.
     */
    public void endGame() {
        System.exit(0);
    }

    /**
     * Returns the white king's current position.
     */
    public Coord getWhiteKing() {
        return whiteKingPos;
    }

    /**
     * Counts turns for fen generation.
     */
    public void countMove() {
        counter++;
    }

    /**
     * Returns the black king's current position.
     */
    public Coord getBlackKing() {
        return blackKingPos;
    }

    /**
     * Returns current playing team.
     */
    public boolean getActiveTeam() {
        return this.isWhite;
    }

    /**
     * Sets current playing team.
     */
    public void setActiveTeam(boolean isWhite) {
        this.isWhite = isWhite;
    }

    /**
     * Checks whether a coordinate matches the current en passant ghost square.
     */
    public boolean checkGhostPiece(int row, int col) {
        return new Coord(row, col).equals(ghostPiece);
    }

    /**
     * Records the position and movement direction of a piece attacking the
     * king.
     */
    public void addAttackerPos(Coord atk, int[] d) {
        attackerPos.add(atk);
        attackerDelta.add(d);
    }

    /**
     * Returns current fen notation of chessboard.
     */
    public String checkFen() {
        return fenString;
    }

    /**
     * Returns generated fen notation of chessboard.
     */
    public String getFen() {
        StringBuilder sb = new StringBuilder();

        for (int i = 7; i >= 0; i--) {
            int nullCounter = 0;
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = chessBoard[i][j];
                if (piece == null) {
                    nullCounter++;
                } else {
                    if (nullCounter > 0) {
                        sb.append(nullCounter);
                        nullCounter = 0;
                    }
                    sb.append(getPieceSymbol(piece));
                }
            }
            if (nullCounter > 0) {
                sb.append(nullCounter);
            }
            if (i > 0) {
                sb.append("/");
            }
        }
        if (getActiveTeam()) {
            sb.append(" w ");
        } else {
            sb.append(" b ");
        }

        StringBuilder fenCastling = new StringBuilder();

        if (castleKingSideWhite) {
            fenCastling.append("K");
        }
        if (castleQueenSideWhite) {
            fenCastling.append("Q");
        }
        if (castleKingSideBlack) {
            fenCastling.append("k");
        }
        if (castleQueenSideBlack) {
            fenCastling.append("q");
        }
        if (!fenCastling.isEmpty()) {
            sb.append(fenCastling.toString());
        } else {
            sb.append("-");
        }

        if (ghostPiece != null) {
            char file = (char) (ghostPiece.getCoordCol() + 'a');
            int rank = ghostPiece.getCoordRow() + 1;

            String enPasssant = String.format(" %c%d",
                    file, rank);

            sb.append(" ").append(enPasssant);
        } else {
            sb.append(" -");
        }
        String key = sb.toString();

        fenMap.put(key, fenMap.getOrDefault(key, 0) + 1);

        sb.append(" ")
                .append(fiftyMoveCounter)
                .append(" ")
                .append(counter);

        fenString = sb.toString();
        return fenString;
    }

    /**
     * Builds a string representation of the board for console display, with
     * black on top, white on the bottom, and column letters at the bottom.
     */
    public String getBoardString() {
        StringBuilder sb = new StringBuilder();

        for (int i = chessBoard.length - 1; i >= 0; i--) { // iterate from top to bottom
            sb.append(i + 1).append(" "); // row number
            for (ChessPiece piece : chessBoard[i]) {
                if (piece == null) {
                    sb.append(". ");
                } else {
                    sb.append(getPieceSymbol(piece)).append(" ");
                }
            }
            sb.append("\n");
        }

        // append column letters at the bottom
        sb.append("  a b c d e f g h\n");

        return sb.toString();
    }

    /**
     * Returns character based on instance of object.
     */
    private char getPieceSymbol(ChessPiece piece) {
        char symbol;
        if (piece instanceof Pawn) {
            symbol = 'P';
        } else if (piece instanceof King) {
            symbol = 'K';
        } else if (piece instanceof Queen) {
            symbol = 'Q';
        } else if (piece instanceof Rook) {
            symbol = 'R';
        } else if (piece instanceof Bishop) {
            symbol = 'B';
        } else if (piece instanceof Knight) {
            symbol = 'N';
        } else {
            throw new IllegalArgumentException("Unknown piece type: " + piece);
        }

        return piece.getTeam() ? symbol : Character.toLowerCase(symbol);
    }

    /**
     * Selects a square if it contains a piece of the current player.
     */
    public boolean selectSquare(int row, int col, boolean isWhite) {
        if (chessBoard[row][col] != null && chessBoard[row][col].getTeam() == isWhite) {
            sel = new Coord(row, col);
            return true;
        }
        return false;
    }

    /**
     * Returns a string describing the currently selected piece.
     */
    public String selectedSquareInfo() {
        return chessBoard[sel.getCoordRow()][sel.getCoordCol()].toString();
    }

    /**
     * Checks the status of a square relative to the current player.
     */
    public SquareStatus getSquareStatus(int row, int col, boolean isWhite) {
        if (chessBoard[row][col] != null) {
            return chessBoard[row][col].getTeam() != isWhite ? SquareStatus.ENEMY : SquareStatus.ALLY;
        }
        return SquareStatus.EMPTY;
    }

    /**
     * Checks for available promotions the current player.
     */
    public boolean pawnPromotion(boolean isWhite) {
        int row = isWhite ? 7 : 0;
        for (ChessPiece piece : chessBoard[row]) {
            if (piece != null && piece instanceof Pawn) {
                return true;
            }
        }
        return false;
    }

    /**
     * Promotes pawn to player's desirable piece.
     */
    public void setPromotion(boolean isWhite, String promotion) {
        int row = isWhite ? 7 : 0;
        int col = 0;
        for (int i = 0; i < chessBoard[row].length; i++) {
            ChessPiece piece = chessBoard[row][i];
            if (piece != null && piece instanceof Pawn) {
                col = i;
            }
        }

        switch (promotion.toUpperCase()) {
            case "Q" -> {
                chessBoard[row][col] = new Queen(isWhite);
            }
            case "R" -> {
                chessBoard[row][col] = new Rook(isWhite);
            }
            case "B" -> {
                chessBoard[row][col] = new Bishop(isWhite);
            }
            case "N" -> {
                chessBoard[row][col] = new Knight(isWhite);
            }
            default ->
                throw new IllegalArgumentException("Unknown promotion: " + promotion);
        }
    }

    /**
     * Checks for available castling.
     */
    private CastlingSide canCastle(boolean isWhite, int row, int col) {
        List<Coord> castlingSquares = new ArrayList<>();
        CastlingSide castlingSide;

        switch (col) {
            case 6 -> {
                if (!(chessBoard[row][7] instanceof Rook rook) || !rook.isFirstMove()) {
                    return CastlingSide.NONE;
                }
                castlingSquares.add(new Coord(row, 4));
                castlingSquares.add(new Coord(row, 5));
                castlingSquares.add(new Coord(row, 6));
                castlingSide = CastlingSide.KING;
            }
            case 2 -> {
                if (!(chessBoard[row][0] instanceof Rook rook) || !rook.isFirstMove()) {
                    return CastlingSide.NONE;
                }
                castlingSquares.add(new Coord(row, 4));
                castlingSquares.add(new Coord(row, 3));
                castlingSquares.add(new Coord(row, 2));
                castlingSquares.add(new Coord(row, 1));
                castlingSide = CastlingSide.QUEEN;
            }
            default -> {
                return CastlingSide.NONE;
            }
        }

        if (!castlingSquares.isEmpty()) {
            Coord kingPos = isWhite ? getWhiteKing() : getBlackKing();
            for (Coord castlingSquare : castlingSquares) {
                if (castlingSquare.getCoordCol() != 4) {
                    if (chessBoard[row][castlingSquare.getCoordCol()] != null) {
                        return CastlingSide.NONE;
                    }
                }
                if (futureChecked(isWhite, kingPos, castlingSquare)) {
                    return CastlingSide.NONE;
                }
            }
            return castlingSide;
        }
        return CastlingSide.NONE;
    }

    /**
     * Calculates all possible moves for the specified team.
     */
    public Coord[] calculateMoves(boolean isWhite) {
        Set<Coord> targets = new HashSet<>();

        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[i].length; j++) {
                ChessPiece piece = chessBoard[i][j];
                if (piece != null) {
                    if (piece.getTeam() == isWhite) {
                        Coord[] pieceMoves = piece.availableMoves(this, new Coord(i, j));
                        targets.addAll(Arrays.asList(pieceMoves));
                    }
                }
            }
        }

        return targets.toArray(Coord[]::new);
    }

    /**
     * Moves the currently selected piece to the given destination only if the
     * move is valid.
     */
    public boolean movePiece(Boolean isWhite, int row, int col) {
        int r = sel.getCoordRow();
        int c = sel.getCoordCol();
        ChessPiece selPiece = chessBoard[r][c];
        Coord dest = new Coord(row, col);

        for (Coord move : selPiece.availableMoves(this, sel)) {
            if (dest.equals(move) && !futureChecked(isWhite, sel, dest)) { // Checks move availability

                if (selPiece instanceof King king) {
                    if (king.isFirstMove() && Math.abs(col - c) == 2) { // Castling logic
                        CastlingSide castlingSide = canCastle(isWhite, row, col);

                        switch (castlingSide) {
                            case KING -> {
                                Rook rook = (Rook) chessBoard[row][7];
                                rook.setFirstMoveFalse();
                                chessBoard[row][7] = null;
                                chessBoard[row][5] = rook;
                            }
                            case QUEEN -> {
                                Rook rook = (Rook) chessBoard[row][0];
                                rook.setFirstMoveFalse();
                                chessBoard[row][0] = null;
                                chessBoard[row][3] = rook;
                            }
                            case NONE -> {
                                return false;
                            }
                            default ->
                                throw new IllegalArgumentException("Unknown castling: " + castlingSide);
                        }
                    }
                    king.setFirstMoveFalse();

                    if (isWhite) {
                        castleKingSideWhite = false;
                        castleQueenSideWhite = false;
                        whiteKingPos = dest;
                    } else {
                        castleKingSideBlack = false;
                        castleQueenSideBlack = false;
                        blackKingPos = dest;
                    }
                }
                if (chessBoard[row][col] != null) {
                    fiftyMoveCounter = 0;
                } else {
                    fiftyMoveCounter++;
                }

                chessBoard[row][col] = selPiece; // Actual move
                chessBoard[r][c] = null;

                if (selPiece instanceof Pawn pawn) { // En passant logic
                    pawn.setFirstMoveFalse();
                    if (Math.abs(row - r) == 2) {
                        ghostPiece = new Coord(r + (isWhite ? 1 : -1), c);
                    } else if (dest.equals(ghostPiece)) {
                        chessBoard[row + (isWhite ? -1 : 1)][col] = null;
                        ghostPiece = null;
                    } else {
                        ghostPiece = null;
                    }

                } else if (selPiece instanceof Rook rook) { // Castling fen indicator
                    if (rook.isFirstMove()) {
                        if (isWhite) {
                            if (c == 0) {
                                castleQueenSideWhite = false;
                            } else if (c == 7) {
                                castleKingSideWhite = false;
                            }
                        } else {
                            if (c == 0) {
                                castleQueenSideBlack = false;
                            } else if (c == 7) {
                                castleKingSideBlack = false;
                            }
                        }
                        rook.setFirstMoveFalse();
                    }
                    ghostPiece = null;
                } else {
                    ghostPiece = null;
                }

                attackerPos.clear();
                attackerDelta.clear();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks Fifty move rule.
     */
    public boolean checkFiftyMoveRule() {
        return fiftyMoveCounter >= 100;
    }

    /**
     * Checks three fold repetition rule.
     */
    public boolean checkRepetition() {
        boolean repetition = false;

        for (int value : fenMap.values()) {
            if (value >= 3) {
                repetition = true;
                break;
            }
        }
        return repetition;
    }

    /**
     * Checks if a team is out of moves.
     */
    public boolean checkStalemate(boolean isWhite) {
        for (int i = 0; i < chessBoard.length; i++) { // iterate through board
            for (int j = 0; j < chessBoard[i].length; j++) {
                ChessPiece piece = chessBoard[i][j];
                if (piece != null && piece.getTeam() == isWhite) {
                    Coord source = new Coord(i, j);
                    for (Coord dest : piece.availableMoves(this, new Coord(i, j))) { // iterate through moves
                        if (dest != null && !futureChecked(isWhite, source, dest)) {
                            return false; // found move
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks insufficient material rule.
     */
    public boolean checkInsufficientMaterial() {
        int whiteCount = 0;
        int blackCount = 0;

        for (ChessPiece[] row : chessBoard) {
            for (ChessPiece piece : row) {
                if (piece != null && !(piece instanceof King)) {
                    if (piece instanceof Queen || piece instanceof Rook || piece instanceof Pawn) {
                        return false;
                    }

                    if (piece.getTeam()) {
                        whiteCount++;
                    } else {
                        blackCount++;
                    }
                }
            }
        }

        return (whiteCount <= 1 && blackCount <= 1);
    }

    /**
     * Checks if the specified team's king is in check.
     */
    public boolean checkChecked(boolean isWhite) {
        Coord kingPos = isWhite ? whiteKingPos : blackKingPos;
        for (Coord atk : calculateMoves(!isWhite)) {
            if (kingPos.equals(atk)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for mate of the specified team.
     */
    public boolean checkMate(boolean isWhite) {
        Coord kingPos = isWhite ? whiteKingPos : blackKingPos;

        // Check if king has any escape moves
        Coord[] kingMoves = chessBoard[kingPos.getCoordRow()][kingPos.getCoordCol()]
                .availableMoves(this, kingPos);
        for (int i = 0; i < kingMoves.length; i++) {
            if (kingMoves[i] == null || Math.abs(kingPos.getCoordCol() - kingMoves[i].getCoordCol()) == 2) { // Ignore nulls and castling moves
                kingMoves[i] = null;
                continue;
            }
            if (!futureChecked(isWhite, kingPos, kingMoves[i])) {
                return false;
            }
        }
        if (attackerPos.size() >= 2) {
            return true;
        }
        Coord atkCoord;
        if (attackerPos.size() == 1) {
            atkCoord = attackerPos.iterator().next();
        } else {
            return false;
        }

        // Check if team can block or capture attackers
        List<Coord> teamPos = new ArrayList<>();
        List<Coord[]> teamMoves = new ArrayList<>();

        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[i].length; j++) {
                ChessPiece piece = chessBoard[i][j];
                if (piece != null && piece.getTeam() == isWhite) {
                    Coord piecePos = new Coord(i, j);
                    if (piece instanceof King) {
                        teamPos.add(piecePos);
                        teamMoves.add(kingMoves);
                        continue;
                    }
                    teamPos.add(piecePos);
                    teamMoves.add(piece.availableMoves(this, piecePos));
                }
            }
        }

        ChessPiece attacker = chessBoard[atkCoord.getCoordRow()][atkCoord.getCoordCol()];
        List<Coord> atkAxis = new ArrayList<>();

        if (attacker instanceof Queen || attacker instanceof Rook || attacker instanceof Bishop) {
            int r = kingPos.getCoordRow();
            int c = kingPos.getCoordCol();
            while (true) {
                r += -attackerDelta.get(0)[0];
                c += -attackerDelta.get(0)[1];
                atkAxis.add(new Coord(r, c));
                if (atkCoord.equals(new Coord(r, c))) {
                    break;
                }
            }

            for (Coord square : atkAxis) { // Attacker can maybe be blocked
                for (int j = 0; j < teamMoves.size(); j++) {
                    for (Coord move : teamMoves.get(j)) {
                        if (move != null && move.equals(square)
                                && !futureChecked(isWhite, teamPos.get(j), move)) {
                            return false;
                        }
                    }
                }
            }

        } else { // attacker cannot be blocked
            for (int j = 0; j < teamMoves.size(); j++) {
                for (Coord move : teamMoves.get(j)) {
                    if (move != null && move.equals(atkCoord)
                            && !futureChecked(isWhite, teamPos.get(j), move)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks whether a future move leads to checkmate.
     */
    private boolean futureChecked(boolean isWhite, Coord from, Coord to) {
        ChessPiece tempTo = chessBoard[to.getCoordRow()][to.getCoordCol()];
        ChessPiece tempFrom = chessBoard[from.getCoordRow()][from.getCoordCol()];

        boolean isKing = tempFrom instanceof King;
        Coord kingPos = isKing ? to : (isWhite ? whiteKingPos : blackKingPos);

        chessBoard[to.getCoordRow()][to.getCoordCol()] = tempFrom;
        chessBoard[from.getCoordRow()][from.getCoordCol()] = null;

        for (Coord atk : calculateMoves(!isWhite)) {
            if (kingPos.equals(atk)) {
                chessBoard[from.getCoordRow()][from.getCoordCol()] = tempFrom;
                chessBoard[to.getCoordRow()][to.getCoordCol()] = tempTo;
                return true;
            }
        }

        chessBoard[from.getCoordRow()][from.getCoordCol()] = tempFrom;
        chessBoard[to.getCoordRow()][to.getCoordCol()] = tempTo;
        return false;
    }
}
