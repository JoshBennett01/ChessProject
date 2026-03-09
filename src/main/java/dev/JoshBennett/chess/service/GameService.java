package dev.JoshBennett.chess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.JoshBennett.chess.logic.GameMaster;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
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

    public GameService() {
        this.gameMaster = new GameMaster();
        this.isWhiteTurn = true;
        this.gameOver = false;
        this.repetition = false;
        this.stalemate = false;
        this.insufficient = false;
        this.fiftyMove = false;
        this.pendingPromotion = false;
        gameMaster.loadGame();
    }

    public MoveResult makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (gameOver) {
            String msg;
            if (repetition) {
                msg = "Repetition Draw";
            } else if (stalemate) {
                msg = "Stalemate Draw";
            } else if (insufficient) {
                msg = "Insufficient Material Draw";
            } else if (fiftyMove) {
                msg = "50-move draw";
            } else {
                msg = isWhiteTurn ? "Black Mates!" : "White Mates!";
            }
            return new MoveResult(false, msg, checkFen());
        }

        if (pendingPromotion) {
            return new MoveResult(false, "Pawn must be promoted first", getFen());
        }

        logger.info("Move request: from=({},{}) to=({},{})", fromRow, fromCol, toRow, toCol);

        boolean selected = gameMaster.selectSquare(fromRow, fromCol, isWhiteTurn);

        if (!selected) {
            logger.warn("Invalid selection at ({},{}) for {}", fromRow, fromCol, isWhiteTurn ? "white" : "black");
            return new MoveResult(false, "Invalid selection", checkFen());
        }

        boolean moved = gameMaster.movePiece(isWhiteTurn, toRow, toCol);
        if (!moved) {
            logger.warn("Invalid move from ({},{}) to ({},{})", fromRow, fromCol, toRow, toCol);
            return new MoveResult(false, "Invalid move", checkFen());
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
            return new MoveResult(true, "PROMOTION_REQUIRED", getFen(), square);
        }

        return finishMoveAndCheckState(fromRow, fromCol, toRow, toCol);
    }

    public MoveResult promotePawn(String pieceType) {
        if (!pendingPromotion) {
            return new MoveResult(false, "No promotion is pending", checkFen());
        }

        try {
            gameMaster.setPromotion(isWhiteTurn, pieceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new MoveResult(false, "Invalid promotion piece: " + pieceType, checkFen());
        }

        pendingPromotion = false;

        return finishMoveAndCheckState(promotionFromRow, promotionFromCol, promotionToRow, promotionToCol);
    }

    private MoveResult finishMoveAndCheckState(int fromRow, int fromCol, int toRow, int toCol) {
        isWhiteTurn = !isWhiteTurn;
        gameMaster.setActiveTeam(isWhiteTurn);
        if (isWhiteTurn) {
            gameMaster.countMove();
        }

        String generatedFen = getFen();

        boolean inCheck = gameMaster.checkChecked(isWhiteTurn);
        boolean inCheckmate = gameMaster.checkMate(isWhiteTurn);

        if (inCheck) {
            if (inCheckmate) {
                gameOver = true;
                return new MoveResult(true, "Checkmate! " + (isWhiteTurn ? "Black" : "White") + " wins!", generatedFen);
            }
            return new MoveResult(true, "Check!", generatedFen);
        }

        boolean inRepetition = gameMaster.checkRepetition();
        boolean inStalemate = gameMaster.checkStalemate(isWhiteTurn);
        boolean inInsufficientMaterial = gameMaster.checkInsufficientMaterial();
        boolean inFiftyMoveRule = gameMaster.checkFiftyMoveRule();

        if (inRepetition) {
            gameOver = true;
            repetition = true;
            return new MoveResult(true, "Repetition Draw", generatedFen);
        }
        if (inStalemate) {
            gameOver = true;
            stalemate = true;
            return new MoveResult(true, "Stalemate Draw", generatedFen);
        }
        if (inInsufficientMaterial) {
            gameOver = true;
            insufficient = true;
            return new MoveResult(true, "Insufficient Material Draw", generatedFen);
        }
        if (inFiftyMoveRule) {
            gameOver = true;
            fiftyMove = true;
            return new MoveResult(true, "50-move draw", generatedFen);
        }

        char fromFile = (char) ('a' + fromCol);
        int fromRank = fromRow + 1;

        char toFile = (char) ('a' + toCol);
        int toRank = toRow + 1;

        String moveStr = String.format("%c%d → %c%d",
                fromFile, fromRank, toFile, toRank);

        return new MoveResult(true, moveStr, generatedFen);

    }

    public void resetGame() {
        gameMaster.loadGame();
        isWhiteTurn = true;
        gameOver = false;
        repetition = false;
        stalemate = false;
        insufficient = false;
        fiftyMove = false;
        pendingPromotion = false;
        logger.info("Game reset");
    }

    public String getFen() {
        String fen = gameMaster.getFen();
        return fen;
    }

    public String checkFen() {
        String fen = gameMaster.checkFen();
        return fen;
    }

    public boolean isPawnPromotionAvailable(boolean isWhite) {
        return gameMaster.pawnPromotion(isWhite);
    }

    public record MoveResult(
            boolean success,
            String message,
            String fen,
            String promotionSquare // null if not a promotion move
            ) {

        public MoveResult(boolean success, String message, String fen) {
            this(success, message, fen, null);
        }
    }
}
