package dev.JoshBennett.chess.logic;

public class PlayerTurn {

    /*
		white = true,
    	black = false
     */
    private boolean isWhite;

    public PlayerTurn(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhiteTurn() {
        return isWhite;
    }

    public void switchTurn() {
        this.isWhite = !isWhite;
    }
}
