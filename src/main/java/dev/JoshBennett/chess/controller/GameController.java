package dev.JoshBennett.chess.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.JoshBennett.chess.service.GameService;
import dev.JoshBennett.chess.service.GameService.MoveResult;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/fen")
    public ResponseEntity<String> getFen() {
        return ResponseEntity.ok(gameService.checkFen());
    }

    @PostMapping("/move")
    public ResponseEntity<MoveResult> makeMove(@RequestBody MoveRequest request) {
        MoveResult result = gameService.makeMove(
                request.fromRow(),
                request.fromCol(),
                request.toRow(),
                request.toCol()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetGame() {
        gameService.resetGame();
        return ResponseEntity.ok(gameService.checkFen());
    }

    @PostMapping("/promote")
    public ResponseEntity<MoveResult> promote(@RequestBody PromoteRequest request) {
        MoveResult result = gameService.promotePawn(request.pieceType());
        return ResponseEntity.ok(result);
    }

    public record MoveRequest(int fromRow, int fromCol, int toRow, int toCol) {

    }

    public record PromoteRequest(String pieceType) {

    }
}
