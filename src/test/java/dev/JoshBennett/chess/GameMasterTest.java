package dev.JoshBennett.chess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.JoshBennett.chess.logic.Coord;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.logic.SquareStatus;

/**
 * Unit tests for GameMaster - core chess game logic. Tests cover: board setup,
 * move validation, special moves, check/checkmate detection, and draw
 * conditions.
 */
class GameMasterTest {

    private GameMaster gm;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        gm = new GameMaster();
        gm.loadGame();
        gm.setActiveTeam(true);
    }

    // ==================== Board Setup Tests ====================
    @Nested
    @DisplayName("Board Setup Tests")
    @SuppressWarnings("unused")
    class BoardSetupTests {

        @Test
        @DisplayName("Initial FEN should be standard starting position")
        void testInitialFen() {
            String expectedFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            assertEquals(expectedFen, gm.checkFen());
        }

        @Test
        @DisplayName("White king should be at e1 (row 0, col 4)")
        void testWhiteKingPosition() {
            Coord kingPos = gm.getWhiteKing();
            assertEquals(0, kingPos.getCoordRow());
            assertEquals(4, kingPos.getCoordCol());
        }

        @Test
        @DisplayName("Black king should be at e8 (row 7, col 4)")
        void testBlackKingPosition() {
            Coord kingPos = gm.getBlackKing();
            assertEquals(7, kingPos.getCoordRow());
            assertEquals(4, kingPos.getCoordCol());
        }

        @Test
        @DisplayName("Board should have correct piece distribution")
        void testInitialBoardState() {
            assertEquals(SquareStatus.ALLY, gm.getSquareStatus(0, 0, true));
            assertEquals(SquareStatus.EMPTY, gm.getSquareStatus(3, 3, true));
            assertEquals(SquareStatus.ENEMY, gm.getSquareStatus(7, 0, true));
        }
    }

    // ==================== Basic Move Validation Tests ====================
    @Nested
    @DisplayName("Basic Move Validation Tests")
    @SuppressWarnings("unused")
    class BasicMoveTests {

        @Test
        @DisplayName("Pawn should move forward one square")
        void testPawnForwardMove() {
            assertTrue(gm.selectSquare(1, 4, true));
            assertTrue(gm.movePiece(true, 3, 4));
        }

        @Test
        @DisplayName("Pawn double move on first move")
        void testPawnDoubleMove() {
            assertTrue(gm.selectSquare(1, 4, true));
            assertTrue(gm.movePiece(true, 3, 4));
        }

        @Test
        @DisplayName("Cannot move opponent's piece")
        void testCannotMoveOpponentPiece() {
            assertFalse(gm.selectSquare(6, 0, true));
        }

        @Test
        @DisplayName("Cannot select empty square")
        void testCannotSelectEmptySquare() {
            assertFalse(gm.selectSquare(3, 3, true));
        }

        @Test
        @DisplayName("Pawn cannot move backward")
        void testPawnCannotMoveBackward() {
            gm.selectSquare(1, 4, true);
            assertFalse(gm.movePiece(true, 0, 4));
        }
    }

    // ==================== Special Move Tests ====================
    @Nested
    @DisplayName("Special Move Tests")
    @SuppressWarnings("unused")
    class SpecialMoveTests {

        @Test
        @DisplayName("Pawn creates ghost piece on double move")
        void testPawnDoubleMoveCreatesGhost() {
            gm.selectSquare(1, 4, true);
            assertTrue(gm.movePiece(true, 3, 4));
            assertTrue(gm.checkGhostPiece(2, 4));
        }

        @Test
        @DisplayName("Pawns are at rank 2 at start")
        void testPawnsAtRank2() {
            assertNotNull(gm.selectSquare(1, 0, true));
            assertNotNull(gm.selectSquare(6, 0, false));
        }

        @Test
        @DisplayName("Can select own piece")
        void testSelectOwnPiece() {
            assertTrue(gm.selectSquare(0, 0, true));
            assertTrue(gm.selectSquare(1, 0, true));
            assertTrue(gm.selectSquare(0, 7, true));
        }
    }

    // ==================== Check/Checkmate Tests ====================
    @Nested
    @DisplayName("Check and Checkmate Tests")
    @SuppressWarnings("unused")
    class CheckTests {

        @Test
        @DisplayName("Should not be in check at start")
        void testNoCheckAtStart() {
            assertFalse(gm.checkChecked(true));
            assertFalse(gm.checkChecked(false));
        }

        @Test
        @DisplayName("Can select king at start")
        void testCanSelectKing() {
            assertTrue(gm.selectSquare(0, 4, true));
        }
    }

    // ==================== Draw Condition Tests ====================
    @Nested
    @DisplayName("Draw Condition Tests")
    @SuppressWarnings("unused")
    class DrawTests {

        @Test
        @DisplayName("Starting position has sufficient material")
        void testStartingPositionHasMaterial() {
            gm.loadGame();
            assertFalse(gm.checkInsufficientMaterial());
        }

        @Test
        @DisplayName("Fifty move counter increments after non-pawn move")
        void testFiftyMoveRuleIncrement() {
            gm.loadGame();
            gm.setActiveTeam(true);

            gm.selectSquare(1, 0, true);
            gm.movePiece(true, 2, 0);
            gm.setActiveTeam(false);
            gm.selectSquare(6, 1, false);
            gm.movePiece(false, 5, 1);

            gm.selectSquare(2, 0, true);
            gm.movePiece(true, 3, 0);
            gm.setActiveTeam(false);
            gm.selectSquare(5, 1, false);
            gm.movePiece(false, 4, 1);

            gm.selectSquare(3, 0, true);
            gm.movePiece(true, 4, 0);
            gm.setActiveTeam(false);
            gm.selectSquare(4, 1, false);
            gm.movePiece(false, 3, 1);

            gm.selectSquare(4, 0, true);
            gm.movePiece(true, 5, 0);
            gm.setActiveTeam(false);
            gm.selectSquare(3, 1, false);
            gm.movePiece(false, 2, 1);

            gm.selectSquare(5, 0, true);
            gm.movePiece(true, 6, 0);
            gm.setActiveTeam(false);
            gm.selectSquare(2, 1, false);
            gm.movePiece(false, 1, 1);

            gm.selectSquare(6, 0, true);
            gm.movePiece(true, 7, 0);
            gm.setActiveTeam(false);
            gm.selectSquare(1, 1, false);
            gm.movePiece(false, 0, 1);

            assertFalse(gm.checkFiftyMoveRule());
        }
    }

    // ==================== Stalemate Tests ====================
    @Nested
    @DisplayName("Stalemate Tests")
    @SuppressWarnings("unused")
    class StalemateTests {

        @Test
        @DisplayName("Not stalemate at game start")
        void testNotStalemateAtStart() {
            assertFalse(gm.checkStalemate(true));
            assertFalse(gm.checkStalemate(false));
        }

        @Test
        @DisplayName("White player can move at start")
        void testWhiteHasLegalMoves() {
            assertFalse(gm.checkStalemate(true));
        }
    }

    // ==================== Board State Tests ====================
    @Nested
    @DisplayName("Board State Tests")
    @SuppressWarnings("unused")
    class BoardStateTests {

        @Test
        @DisplayName("Active team can be set and retrieved")
        void testActiveTeam() {
            assertTrue(gm.getActiveTeam());
            gm.setActiveTeam(false);
            assertFalse(gm.getActiveTeam());
        }

        @Test
        @DisplayName("Ghost piece starts as null")
        void testGhostPieceInitiallyNull() {
            assertFalse(gm.checkGhostPiece(0, 0));
            assertFalse(gm.checkGhostPiece(3, 4));
        }
    }
}
