package dev.JoshBennett.chess.models;

import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.service.RoomInfo;

public class GameRoom {

    private final String roomId;
    private String whiteTokenId;
    private String blackTokenId;
    private final GameMaster gameMaster;
    private boolean isWhiteTurn;
    private boolean gameOver;
    private boolean repetition;
    private boolean stalemate;
    private boolean insufficient;
    private boolean fiftyMove;
    private boolean pendingPromotion;
    private int promotionToRow;
    private int promotionToCol;
    private int promotionFromRow;
    private int promotionFromCol;
    private int moveCount;
    private String whitePlayerId;
    private String blackPlayerId;
    private String message;

    public GameRoom(String roomId, String whiteTokenId, String blackTokenId) {
        this.roomId = roomId;
        this.whiteTokenId = whiteTokenId;
        this.blackTokenId = blackTokenId;
        this.gameMaster = new GameMaster();
        this.isWhiteTurn = true;
        this.gameOver = false;
        this.repetition = false;
        this.stalemate = false;
        this.insufficient = false;
        this.fiftyMove = false;
        this.pendingPromotion = false;
        this.moveCount = 0;
        this.whitePlayerId = null;
        this.blackPlayerId = null;

        this.message = "Loading...";
        gameMaster.loadGame();
    }

    public String getWhiteTokenId() {
        return whiteTokenId;
    }

    public String getBlackTokenId() {
        return blackTokenId;
    }

    public String getWhitePlayerId() {
        return whitePlayerId;
    }

    public String getBlackPlayerId() {
        return blackPlayerId;
    }

    public void setWhitePlayerId(String whitePlayerId) {
        this.whitePlayerId = whitePlayerId;
        if (blackPlayerId != null) {
            message = "Game Loaded";
        }
    }

    public void setBlackPlayerId(String blackPlayerId) {
        this.blackPlayerId = blackPlayerId;
        if (whitePlayerId != null) {
            message = "Game Loaded";
        }
    }

    public RoomInfo getRoomInfo() {
        return new RoomInfo(roomId, checkFen(), message, isWhiteTurn, whitePlayerId, blackPlayerId, gameOver);
    }

    public MoveResult makeMove(String tokenId, int fromRow, int fromCol, int toRow, int toCol) {
        String tokenIdComparison = isWhiteTurn ? whiteTokenId : blackTokenId;
        if (!tokenIdComparison.equals(tokenId)) {
            message = "Invalid token";
            return new MoveResult(false, message, checkFen(), moveCount);
        }

        if (gameOver) {
            if (repetition) {
                message = "Repetition Draw";
            } else if (stalemate) {
                message = "Stalemate Draw";
            } else if (insufficient) {
                message = "Insufficient Material Draw";
            } else if (fiftyMove) {
                message = "50-move draw";
            } else {
                message = isWhiteTurn ? "Black Mates!" : "White Mates!";
            }
            return new MoveResult(false, message, checkFen(), moveCount);
        }

        if (pendingPromotion) {
            message = "Pawn must be promoted first";
            return new MoveResult(false, message, getFen(), moveCount);
        }

        boolean selected = gameMaster.selectSquare(fromRow, fromCol, isWhiteTurn);
        if (!selected) {
            message = "Invalid selection";
            return new MoveResult(false, message, checkFen(), moveCount);
        }

        boolean moved = gameMaster.movePiece(isWhiteTurn, toRow, toCol);
        if (!moved) {
            message = "Invalid move";
            return new MoveResult(false, message, checkFen(), moveCount);
        }

        if (isPawnPromotionAvailable(isWhiteTurn)) {
            pendingPromotion = true;
            promotionToRow = toRow;
            promotionToCol = toCol;
            promotionFromRow = fromRow;
            promotionFromCol = fromCol;

            char toFile = (char) ('a' + toCol);
            int toRank = toRow + 1;
            String square = String.format("%c%d", toFile, toRank);
            return new MoveResult(true, "PROMOTION_REQUIRED", getFen(), moveCount, square);
        }

        return finishMoveAndCheckState(fromRow, fromCol, toRow, toCol);
    }

    public MoveResult promotePawn(String tokenId, String pieceType) {
        String tokenIdComparison = isWhiteTurn ? whiteTokenId : blackTokenId;
        if (!tokenIdComparison.equals(tokenId)) {
            message = "Invalid token";
            return new MoveResult(false, message, checkFen(), moveCount);
        }
        if (!pendingPromotion) {
            message = "No promotion is pending";
            return new MoveResult(false, message, checkFen(), moveCount);
        }

        try {
            gameMaster.setPromotion(isWhiteTurn, pieceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            message = "Invalid promotion piece: " + pieceType;
            return new MoveResult(false, message, checkFen(), moveCount);
        }

        pendingPromotion = false;
        return finishMoveAndCheckState(promotionFromRow, promotionFromCol, promotionToRow, promotionToCol);
    }

    private MoveResult finishMoveAndCheckState(int fromRow, int fromCol, int toRow, int toCol) {
        isWhiteTurn = !isWhiteTurn;
        gameMaster.setActiveTeam(isWhiteTurn);
        if (isWhiteTurn) {
            gameMaster.countMove();
            moveCount++;
        }

        String generatedFen = getFen();

        boolean inCheck = gameMaster.checkChecked(isWhiteTurn);
        boolean inCheckmate = gameMaster.checkMate(isWhiteTurn);

        if (inCheck) {
            if (inCheckmate) {
                gameOver = true;
                message = "Checkmate! " + (isWhiteTurn ? "Black" : "White") + " wins!";
                return new MoveResult(true, message, generatedFen, moveCount);
            }
            message = "Check!";
            return new MoveResult(true, message, generatedFen, moveCount);
        }

        boolean inRepetition = gameMaster.checkRepetition();
        boolean inStalemate = gameMaster.checkStalemate(isWhiteTurn);
        boolean inInsufficientMaterial = gameMaster.checkInsufficientMaterial();
        boolean inFiftyMoveRule = gameMaster.checkFiftyMoveRule();

        if (inRepetition) {
            gameOver = true;
            repetition = true;
            message = "Repetition Draw";
            return new MoveResult(true, message, generatedFen, moveCount);
        }
        if (inStalemate) {
            gameOver = true;
            stalemate = true;
            message = "Stalemate Draw";
            return new MoveResult(true, message, generatedFen, moveCount);
        }
        if (inInsufficientMaterial) {
            gameOver = true;
            insufficient = true;
            message = "Insufficient Material Draw";
            return new MoveResult(true, message, generatedFen, moveCount);
        }
        if (inFiftyMoveRule) {
            gameOver = true;
            fiftyMove = true;
            message = "50-move draw";
            return new MoveResult(true, message, generatedFen, moveCount);
        }

        char fromFile = (char) ('a' + fromCol);
        int fromRank = fromRow + 1;
        char toFile = (char) ('a' + toCol);
        int toRank = toRow + 1;
        String moveStr = String.format("%c%d → %c%d", fromFile, fromRank, toFile, toRank);
        message = moveStr;

        return new MoveResult(true, message, generatedFen, moveCount);
    }

    public void resetGame() {
        if (gameOver) {
            isWhiteTurn = true;
            gameOver = false;
            repetition = false;
            stalemate = false;
            insufficient = false;
            fiftyMove = false;
            pendingPromotion = false;
            moveCount = 0;
            whitePlayerId = null;
            blackPlayerId = null;

            this.message = "Loaded";
            gameMaster.loadGame();
        }
    }

    public String getFen() {
        return gameMaster.getFen();
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public String checkFen() {
        return gameMaster.checkFen();
    }

    public boolean isPawnPromotionAvailable(boolean isWhite) {
        return gameMaster.pawnPromotion(isWhite);
    }

    public String getGameState() {
        return gameMaster.getFen() + "|" + (isWhiteTurn ? "w" : "b") + "|" + moveCount;
    }

    public static class MoveResult {

        private final boolean success;
        private final String message;
        private final String fen;
        private final int moveCount;
        private final String promotionSquare;

        public MoveResult(boolean success, String message, String fen, int moveCount, String promotionSquare) {
            this.success = success;
            this.message = message;
            this.fen = fen;
            this.moveCount = moveCount;
            this.promotionSquare = promotionSquare;
        }

        public MoveResult(boolean success, String message, String fen, int moveCount) {
            this(success, message, fen, moveCount, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getFen() {
            return fen;
        }

        public int getMoveCount() {
            return moveCount;
        }

        public String getPromotionSquare() {
            return promotionSquare;
        }
    }
}
