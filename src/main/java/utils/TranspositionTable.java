package main.java.utils;

public class TranspositionTable {
    private static final int size = 100_000_000;
    private static final long[][] table = new long[2][size];

    public static final int EXACT = 0;
    public static final int LOWERBOUND = 1;
    public static final int UPPERBOUND = 2;

    private static long packData(int depth, int score, int bestMove, int flag) {
        return ((long)(flag & 0x3) << 46)        // bits 46-47
             | ((long)(depth & 0x1F) << 41)     // bits 41-45
             | ((long)(bestMove & 0x1FFL) << 32) // bits 32-40
             | (score & 0xFFFFFFFFL);           // bits 0-31
    }
    
    public static int getFlag(long data)     { return (int)((data >>> 46) & 0x3L); }
    public static int getDepth(long data)    { return (int)((data >>> 41) & 0x1FL); }
    public static int getBestMove(long data) { return (int)((data >>> 32) & 0x1FFL); }
    public static int getScore(long data)    { return (int)(data & 0xFFFFFFFFL); }
    

    private static boolean matches(long keyXorData, long data, long zobristKey) {
        return (keyXorData ^ data) == zobristKey;
    }

    public static long probe(long zobristKey) {
        int index = (int)((zobristKey & 0x7FFFFFFFFFFFFFFFL) % size);
        long keyXorData = table[0][index];
        long data = table[1][index];
        if (matches(keyXorData, data, zobristKey)) {
            return data; // hit
        }
        return 0; // miss
    }

    public static void store(long zobristKey, int depth, int score, int bestMove, int flag) {
        int index = (int)((zobristKey & 0x7FFFFFFFFFFFFFFFL) % size);
        long data = table[1][index];

        if (data == 0 || depth >= getDepth(data)) {
            data = packData(depth, score, bestMove, flag);
            table[0][index] = zobristKey ^ data;
            table[1][index] = data;
        }
    }
}