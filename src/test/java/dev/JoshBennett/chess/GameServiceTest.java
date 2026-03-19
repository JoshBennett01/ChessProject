package dev.JoshBennett.chess;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.JoshBennett.chess.controller.GameController.JoinResponse;
import dev.JoshBennett.chess.models.GameRoom;
import dev.JoshBennett.chess.service.GameService;
import dev.JoshBennett.chess.service.RoomInfo;

/**
 * Unit tests for GameService - room and game session management. Tests cover:
 * room creation, player joining, token validation, move processing, and game
 * state management.
 */
class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        gameService = new GameService();
    }

    // ==================== Room Creation Tests ====================
    @Nested
    @DisplayName("Room Creation Tests")
    @SuppressWarnings("unused")
    class RoomCreationTests {

        @Test
        @DisplayName("Should create a room and return room ID")
        void testCreateRoom() {
            String roomId = gameService.createRoom();
            assertNotNull(roomId);
            assertFalse(roomId.isEmpty());
        }

        @Test
        @DisplayName("Room ID should be 8 characters")
        void testRoomIdLength() {
            String roomId = gameService.createRoom();
            assertEquals(8, roomId.length());
        }

        @Test
        @DisplayName("Should generate unique room IDs")
        void testUniqueRoomIds() {
            String roomId1 = gameService.createRoom();
            String roomId2 = gameService.createRoom();
            assertNotEquals(roomId1, roomId2);
        }

        @Test
        @DisplayName("Room ID should be uppercase alphanumeric")
        void testRoomIdFormat() {
            String roomId = gameService.createRoom();
            assertTrue(roomId.matches("^[A-Z0-9]+$"));
        }

        @Test
        @DisplayName("Created room should exist")
        void testRoomExists() {
            String roomId = gameService.createRoom();
            assertTrue(gameService.roomExists(roomId));
        }

        @Test
        @DisplayName("Non-existent room should not exist")
        void testRoomDoesNotExist() {
            assertFalse(gameService.roomExists("NONEXIST"));
        }
    }

    // ==================== Room Info Tests ====================
    @Nested
    @DisplayName("Room Info Tests")
    @SuppressWarnings("unused")
    class RoomInfoTests {

        @Test
        @DisplayName("New room should have initial FEN")
        void testNewRoomInitialFen() {
            String roomId = gameService.createRoom();
            RoomInfo info = gameService.getRoomInfo(roomId);

            assertNotNull(info);
            assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", info.fen());
        }

        @Test
        @DisplayName("New room should have white turn")
        void testNewRoomWhiteTurn() {
            String roomId = gameService.createRoom();
            RoomInfo info = gameService.getRoomInfo(roomId);

            assertTrue(info.isWhiteTurn());
        }

        @Test
        @DisplayName("New room should not be game over")
        void testNewRoomNotGameOver() {
            String roomId = gameService.createRoom();
            RoomInfo info = gameService.getRoomInfo(roomId);

            assertFalse(info.gameOver());
        }

        @Test
        @DisplayName("New room should have no players")
        void testNewRoomNoPlayers() {
            String roomId = gameService.createRoom();
            RoomInfo info = gameService.getRoomInfo(roomId);

            assertNull(info.whitePlayerId());
            assertNull(info.blackPlayerId());
        }

        @Test
        @DisplayName("Getting info for non-existent room should return null")
        void testGetInfoForNonExistentRoom() {
            RoomInfo info = gameService.getRoomInfo("NONEXIST");
            assertNull(info);
        }
    }

    // ==================== Player Join Tests ====================
    @Nested
    @DisplayName("Player Join Tests")
    @SuppressWarnings("unused")
    class PlayerJoinTests {

        @Test
        @DisplayName("Player should be able to join as white")
        void testJoinAsWhite() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();

            JoinResponse response = gameService.joinRoom(roomId, playerId, "white");

            assertNotNull(response);
            assertNotNull(response.tokenId());
        }

        @Test
        @DisplayName("Player should be able to join as black")
        void testJoinAsBlack() {
            String roomId = gameService.createRoom();
            String playerId1 = UUID.randomUUID().toString();
            String playerId2 = UUID.randomUUID().toString();

            gameService.joinRoom(roomId, playerId1, "white");
            JoinResponse response = gameService.joinRoom(roomId, playerId2, "black");

            assertNotNull(response);
            assertNotNull(response.tokenId());
        }

        @Test
        @DisplayName("Joining should update room info")
        void testJoinUpdatesRoomInfo() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();

            gameService.joinRoom(roomId, playerId, "white");
            RoomInfo info = gameService.getRoomInfo(roomId);

            assertEquals(playerId, info.whitePlayerId());
        }

        @Test
        @DisplayName("Joining non-existent room should return null")
        void testJoinNonExistentRoom() {
            JoinResponse response = gameService.joinRoom("NONEXIST", "player123", "white");
            assertNull(response);
        }

        @Test
        @DisplayName("Cannot join full room")
        void testCannotJoinFullRoom() {
            String roomId = gameService.createRoom();
            String playerId1 = UUID.randomUUID().toString();
            String playerId2 = UUID.randomUUID().toString();
            String playerId3 = UUID.randomUUID().toString();

            gameService.joinRoom(roomId, playerId1, "white");
            gameService.joinRoom(roomId, playerId2, "black");
            JoinResponse response = gameService.joinRoom(roomId, playerId3, "white");

            assertNull(response);
        }

        @Test
        @DisplayName("Cannot join with same color twice")
        void testCannotJoinSameColorTwice() {
            String roomId = gameService.createRoom();
            String playerId1 = UUID.randomUUID().toString();
            String playerId2 = UUID.randomUUID().toString();

            gameService.joinRoom(roomId, playerId1, "white");
            JoinResponse response = gameService.joinRoom(roomId, playerId2, "white");

            assertNull(response);
        }

        @Test
        @DisplayName("Color should be case-insensitive")
        void testColorCaseInsensitive() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();

            JoinResponse response1 = gameService.joinRoom(roomId, playerId, "WHITE");
            assertNotNull(response1);

            String playerId2 = UUID.randomUUID().toString();
            JoinResponse response2 = gameService.joinRoom(roomId, playerId2, "BLACK");
            assertNotNull(response2);
        }
    }

    // ==================== Token Validation Tests ====================
    @Nested
    @DisplayName("Token Validation Tests")
    @SuppressWarnings("unused")
    class TokenValidationTests {

        @Test
        @DisplayName("Valid token should be accepted for move")
        void testValidTokenAccepted() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();
            JoinResponse joinResponse = gameService.joinRoom(roomId, playerId, "white");

            GameRoom.MoveResult result = gameService.makeRoomMove(
                    roomId, joinResponse.tokenId(), 1, 4, 3, 4
            );

            assertTrue(result.success());
        }

        @Test
        @DisplayName("Invalid token should be rejected")
        void testInvalidTokenRejected() {
            String roomId = gameService.createRoom();
            gameService.joinRoom(roomId, UUID.randomUUID().toString(), "white");

            GameRoom.MoveResult result = gameService.makeRoomMove(
                    roomId, "invalid-token", 6, 4, 5, 4
            );

            assertFalse(result.success());
            assertEquals("Invalid token", result.message());
        }
    }

    // ==================== Move Processing Tests ====================
    @Nested
    @DisplayName("Move Processing Tests")
    @SuppressWarnings("unused")
    class MoveProcessingTests {

        @Test
        @DisplayName("Valid move should succeed")
        void testValidMove() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();
            JoinResponse joinResponse = gameService.joinRoom(roomId, playerId, "white");

            GameRoom.MoveResult result = gameService.makeRoomMove(
                    roomId, joinResponse.tokenId(), 1, 4, 3, 4
            );

            assertTrue(result.success());
            assertNotNull(result.fen());
        }

        @Test
        @DisplayName("Invalid move should fail")
        void testInvalidMove() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();
            JoinResponse joinResponse = gameService.joinRoom(roomId, playerId, "white");

            GameRoom.MoveResult result = gameService.makeRoomMove(
                    roomId, joinResponse.tokenId(), 1, 4, 4, 4
            );

            assertFalse(result.success());
        }

        @Test
        @DisplayName("Move should update FEN")
        void testMoveUpdatesFen() {
            String roomId = gameService.createRoom();
            String playerId = UUID.randomUUID().toString();
            JoinResponse joinResponse = gameService.joinRoom(roomId, playerId, "white");

            gameService.makeRoomMove(roomId, joinResponse.tokenId(), 1, 4, 3, 4);
            RoomInfo info = gameService.getRoomInfo(roomId);

            assertNotEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", info.fen());
        }

        @Test
        @DisplayName("Black move should succeed after white")
        void testBlackMoveAfterWhite() {
            String roomId = gameService.createRoom();
            String whiteId = UUID.randomUUID().toString();
            String blackId = UUID.randomUUID().toString();
            JoinResponse whiteJoin = gameService.joinRoom(roomId, whiteId, "white");
            JoinResponse blackJoin = gameService.joinRoom(roomId, blackId, "black");

            gameService.makeRoomMove(roomId, whiteJoin.tokenId(), 1, 4, 3, 4);
            GameRoom.MoveResult blackMove = gameService.makeRoomMove(
                    roomId, blackJoin.tokenId(), 6, 4, 4, 4
            );

            assertTrue(blackMove.success());
        }

        @Test
        @DisplayName("Wrong team cannot move")
        void testWrongTeamCannotMove() {
            String roomId = gameService.createRoom();
            String whiteId = UUID.randomUUID().toString();
            String blackId = UUID.randomUUID().toString();
            JoinResponse whiteJoin = gameService.joinRoom(roomId, whiteId, "white");
            gameService.joinRoom(roomId, blackId, "black");

            GameRoom.MoveResult blackMove = gameService.makeRoomMove(
                    roomId, whiteJoin.tokenId(), 6, 4, 5, 4
            );

            assertFalse(blackMove.success());
        }
    }

    // ==================== Game State Tests ====================
    @Nested
    @DisplayName("Game State Tests")
    @SuppressWarnings("unused")
    class GameStateTests {

        @Test
        @DisplayName("Room should track both players")
        void testTrackBothPlayers() {
            String roomId = gameService.createRoom();
            String whiteId = UUID.randomUUID().toString();
            String blackId = UUID.randomUUID().toString();

            gameService.joinRoom(roomId, whiteId, "white");
            gameService.joinRoom(roomId, blackId, "black");

            RoomInfo info = gameService.getRoomInfo(roomId);
            assertEquals(whiteId, info.whitePlayerId());
            assertEquals(blackId, info.blackPlayerId());
        }

        @Test
        @DisplayName("Making move should update turn")
        void testTurnUpdates() {
            String roomId = gameService.createRoom();
            String whiteId = UUID.randomUUID().toString();
            JoinResponse whiteJoin = gameService.joinRoom(roomId, whiteId, "white");

            RoomInfo before = gameService.getRoomInfo(roomId);
            assertTrue(before.isWhiteTurn());

            gameService.makeRoomMove(roomId, whiteJoin.tokenId(), 1, 4, 3, 4);

            RoomInfo after = gameService.getRoomInfo(roomId);
            assertFalse(after.isWhiteTurn());
        }
    }

    // ==================== Pawn Promotion Tests ====================
    @Nested
    @DisplayName("Pawn Promotion Tests")
    @SuppressWarnings("unused")
    class PawnPromotionTests {

        @Test
        @DisplayName("Invalid promotion piece type should fail")
        void testInvalidPromotionPiece() {
            String roomId = gameService.createRoom();
            String whiteId = UUID.randomUUID().toString();
            String blackId = UUID.randomUUID().toString();
            JoinResponse whiteJoin = gameService.joinRoom(roomId, whiteId, "white");
            gameService.joinRoom(roomId, blackId, "black");

            for (int i = 0; i < 12; i++) {
                gameService.makeRoomMove(roomId, whiteJoin.tokenId(), 1, 0, 2, 0);
                gameService.makeRoomMove(roomId, whiteJoin.tokenId(), 6, 1, 5, 1);
            }

            GameRoom.MoveResult result = gameService.promoteRoomPawn(roomId, whiteJoin.tokenId(), "X");
            assertFalse(result.success());
        }

        @Test
        @DisplayName("Promotion with invalid token should fail")
        void testPromotionInvalidToken() {
            String roomId = gameService.createRoom();
            gameService.joinRoom(roomId, UUID.randomUUID().toString(), "white");

            GameRoom.MoveResult result = gameService.promoteRoomPawn(roomId, "invalid-token", "Q");
            assertFalse(result.success());
        }
    }

    // ==================== Reset Game Tests ====================
    @Nested
    @DisplayName("Reset Game Tests")
    @SuppressWarnings("unused")
    class ResetGameTests {

        @Test
        @DisplayName("Reset should return room info")
        void testResetReturnsRoomInfo() {
            String roomId = gameService.createRoom();
            RoomInfo info = gameService.resetRoom(roomId);
            assertNotNull(info);
        }

        @Test
        @DisplayName("Reset non-existent room should return null")
        void testResetNonExistentRoom() {
            RoomInfo info = gameService.resetRoom("NONEXIST");
            assertNull(info);
        }
    }
}
