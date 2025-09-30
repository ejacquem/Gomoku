package main.java.utils;

import java.util.Random;

public class Zobrist {
    private static final int SIZE = 441; // 441 = board max index
    private static final long[][] table = new long[SIZE][2]; // 2 = black/white
    private long hash = 0;

    static {
        Random rng = new Random(123456789); // fixed seed for determinism
        for (int cell = 0; cell < SIZE; cell++) {
            for (int piece = 0; piece < 2; piece++) {
                table[cell][piece] = rng.nextLong();
            }
        }
    }

    public static long get(int cell, int piece) {
        return table[cell][piece];
    }

    public void makeMove(int index, int piece) { 
        hash ^= Zobrist.get(index, piece);
        // System.out.println("Zobrist make move, hash = " + hash);
    }
    
    public void undoMove(int index, int piece) {
        hash ^= Zobrist.get(index, piece);
        // System.out.println("Zobrist undo move, hash = " + hash);
    }

    public long getHash(){
        return hash;
    }
}    