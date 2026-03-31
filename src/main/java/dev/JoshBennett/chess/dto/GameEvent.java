package dev.JoshBennett.chess.dto;

public class GameEvent {

    public record MoveMade(
            String type,
            String fen,
            boolean isCheckmate,
            boolean isWhiteTurn,
            String message
            ) {

        public MoveMade(String fen, boolean isCheckmate, boolean isWhiteTurn, String message) {
            this("MoveMade", fen, isCheckmate, isWhiteTurn, message);
        }
    }

    public record JoinedRoom(
            String type
            ) {

        public JoinedRoom() {
            this("JoinedRoom");
        }
    }

    public record GameReset(
            String type,
            String fen,
            boolean isWhiteTurn
            ) {

        public GameReset(String fen, boolean isWhiteTurn) {
            this("GameReset", fen, isWhiteTurn);
        }
    }
}
