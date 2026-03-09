/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dev.JoshBennett.chess;

import dev.JoshBennett.chess.controller.ConsoleController;
import dev.JoshBennett.chess.logic.GameMaster;
import dev.JoshBennett.chess.service.ConsoleGameService;

/**
 *
 * @author Josh Bennett
 */
public class ConsoleChessApplication {

    public static void main(String[] args) {
        GameMaster gm = new GameMaster();
        ConsoleController contr = new ConsoleController();
        ConsoleGameService cg = new ConsoleGameService(gm, contr);
        gm.loadGame(); // Places pieces
        cg.startGame(); // Starts loops and input
    }

}
