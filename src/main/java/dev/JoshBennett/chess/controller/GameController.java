package dev.JoshBennett.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.JoshBennett.chess.models.GameRoom;
import dev.JoshBennett.chess.service.GameService;
import dev.JoshBennett.chess.service.RoomInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom() {
        String roomId = gameService.createRoom();
        return ResponseEntity.ok(new RoomResponse(roomId, "/game/" + roomId));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<RoomInfo> getRoom(@PathVariable String roomId) {
        RoomInfo info = gameService.getRoomInfo(roomId);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(
            @PathVariable @Size(min = 8, max = 8, message = "Room ID must be exactly 8 characters") String roomId,
            @RequestParam @NotBlank(message = "Player ID is required") String playerId,
            @RequestParam(defaultValue = "white") @Pattern(regexp = "^(?i)white|black$", message = "Color must be 'white' or 'black'") String color) {

        JoinResponse info = gameService.joinRoom(roomId, playerId, color);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @PostMapping("/rooms/{roomId}/move")
    public ResponseEntity<?> makeRoomMove(
            @PathVariable @Size(min = 8, max = 8, message = "Room ID must be exactly 8 characters") String roomId,
            @Valid @RequestBody MoveRequest request) {

        GameRoom.MoveResult result = gameService.makeRoomMove(
                roomId,
                request.tokenId(),
                request.fromRow(),
                request.fromCol(),
                request.toRow(),
                request.toCol()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/rooms/{roomId}/promote")
    public ResponseEntity<?> promoteRoomPawn(
            @PathVariable @Size(min = 8, max = 8, message = "Room ID must be exactly 8 characters") String roomId,
            @Valid @RequestBody PromoteRequest request) {

        GameRoom.MoveResult result = gameService.promoteRoomPawn(roomId, request.tokenId(), request.pieceType());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rooms/{roomId}/poll")
    public ResponseEntity<RoomInfo> pollRoom(@PathVariable String roomId) {
        RoomInfo info = gameService.getRoomInfo(roomId);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @PostMapping("/rooms/{roomId}/reset")
    public ResponseEntity<RoomInfo> resetGame(@PathVariable String roomId) {
        RoomInfo info = gameService.resetRoom(roomId);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    public record MoveRequest(
            @NotBlank(message = "Token ID is required") String tokenId,
            @NotNull(message = "fromRow is required")
            @Min(value = 0, message = "fromRow must be between 0 and 7")
            @Max(value = 7, message = "fromRow must be between 0 and 7") Integer fromRow,
            @NotNull(message = "fromCol is required")
            @Min(value = 0, message = "fromCol must be between 0 and 7")
            @Max(value = 7, message = "fromCol must be between 0 and 7") Integer fromCol,
            @NotNull(message = "toRow is required")
            @Min(value = 0, message = "toRow must be between 0 and 7")
            @Max(value = 7, message = "toRow must be between 0 and 7") Integer toRow,
            @NotNull(message = "toCol is required")
            @Min(value = 0, message = "toCol must be between 0 and 7")
            @Max(value = 7, message = "toCol must be between 0 and 7") Integer toCol
            ) {

    }

    public record PromoteRequest(
            @NotBlank(message = "Token ID is required") String tokenId,
            @NotBlank(message = "Piece type is required")
            @Pattern(regexp = "^(?i)Q|R|B|N$", message = "Piece type must be Q, R, B, or N") String pieceType
            ) {

    }

    public record RoomResponse(String roomId, String shareUrl) {

    }

    public record JoinResponse(String tokenId) {

    }

    public record ErrorResponse(String error, String message) {

    }
}
