package main.java.game;

import java.util.List;

public class Move {
    int player; // who made the move
    Coords coords;
    List<Coords> capturesCoords;

    Move(int player, Coords coords, List<Coords> capturesCoords) {
        this.player = player;
        this.coords = coords;
        this.capturesCoords = capturesCoords;
    }
}
