package dev.JoshBennett.chess.service;

public record RoomInfo(
        String roomId,
        String fen,
        String message,
        boolean isWhiteTurn,
        String whitePlayerId,
        String blackPlayerId,
        boolean gameOver
        ) {

}
