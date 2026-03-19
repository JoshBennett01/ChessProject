package dev.JoshBennett.chess.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.JoshBennett.chess.controller.GameController.JoinResponse;
import dev.JoshBennett.chess.models.GameRoom;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameService() {
    }

    public String createRoom() {
        String roomId = generateRoomId();
        rooms.put(roomId, new GameRoom(roomId, generateTokenID(), generateTokenID()));
        logger.info("Created room: {}", roomId);
        return roomId;
    }

    private String generateTokenID() {
        return UUID.randomUUID().toString();
    }

    private String generateRoomId() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (rooms.containsKey(id));
        return id;
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    public JoinResponse joinRoom(String roomId, String playerId, String color) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return null;
        }
        if (room.getWhitePlayerId() != null && room.getBlackPlayerId() != null) {
            return null;
        }
        if ("white".equalsIgnoreCase(color) && room.getWhitePlayerId() == null) {
            room.setWhitePlayerId(playerId);
            return new JoinResponse(room.getWhiteTokenId());
        } else if ("black".equalsIgnoreCase(color) && room.getBlackPlayerId() == null) {
            room.setBlackPlayerId(playerId);
            return new JoinResponse(room.getBlackTokenId());
        }
        return null;
    }

    public RoomInfo resetRoom(String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return null;
        }
        room.resetGame();
        return room.getRoomInfo();
    }

    public RoomInfo getRoomInfo(String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return null;
        }
        return room.getRoomInfo();
    }

    public GameRoom.MoveResult makeRoomMove(String roomId, String tokenId, int fromRow, int fromCol, int toRow, int toCol) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return new GameRoom.MoveResult(false, "Room not found", "", 0);
        }
        return room.makeMove(tokenId, fromRow, fromCol, toRow, toCol);
    }

    public GameRoom.MoveResult promoteRoomPawn(String roomId, String tokenId, String pieceType) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return new GameRoom.MoveResult(false, "Room not found", "", 0);
        }
        return room.promotePawn(tokenId, pieceType);
    }
}
