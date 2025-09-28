package main.java.utils;

import java.util.ArrayList;
import java.util.List;

import main.java.app.GameSettings;

public class ScoreBuckets {
    private final int MAX_SCORE;
    public final List<Integer>[] buckets;

    private static final int MAX_HISTORY_LEN = GameSettings.BOARD_MAX_INDEX_SIZE + 50; // the board size plus some room for captures
    private static final int CHANGES_PER_MOVE = 8; // Could go up with capture, but captures are limited to 9 per game
    private final int[] history = new int[MAX_HISTORY_LEN * CHANGES_PER_MOVE * 3];
    private final int[] moveBoundaries = new int[MAX_HISTORY_LEN + 1]; 
    private int historySize = 0;
    private int moveCount = 0;

    @SuppressWarnings("unchecked")
    public ScoreBuckets(int maxScore) {
        MAX_SCORE = maxScore;
        buckets = new List[MAX_SCORE + 1];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new ArrayList<>();
        }
    }

    public ScoreBuckets deepCopy() {
        ScoreBuckets copy = new ScoreBuckets(MAX_SCORE);
        
        for (int i = 0; i <= MAX_SCORE; i++) {
            copy.buckets[i].addAll(this.buckets[i]);
        }
    
        System.arraycopy(this.history, 0, copy.history, 0, MAX_HISTORY_LEN * CHANGES_PER_MOVE * 3);
        System.arraycopy(this.moveBoundaries, 0, copy.moveBoundaries, 0, MAX_HISTORY_LEN + 1);
    
        copy.historySize = this.historySize;
        copy.moveCount = this.moveCount;
    
        return copy;
    }

    public void startMove() {
        // System.out.println("[ScoreBucket] startMove");
        // System.out.println("[ScoreBucket] historySize: " + historySize);
        // System.out.println("[ScoreBucket] moveCount: " + moveCount);
        moveCount++;
    }

    public void reset() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i].clear();
        }
        historySize = 0;
        moveCount = 0;
    }

    public void redoMove() {
        moveCount++;
        // int start = moveBoundaries[moveCount];
        int end = moveBoundaries[moveCount + 1];
        // System.out.println("[ScoreBucket] redo Bucket");
        // System.out.println("[ScoreBucket] moveBoundaries[moveCount]: " + moveBoundaries[moveCount]);
        // System.out.println("[ScoreBucket] moveBoundaries[moveCount + 1]: " + moveBoundaries[moveCount + 1]);
        // System.out.println("[ScoreBucket] moveBoundaries[moveCount + 2]: " + moveBoundaries[moveCount + 2]);
        // System.out.println("[ScoreBucket] historySize: " + historySize);
        // System.out.println("[ScoreBucket] start: " + start);
        // System.out.println("[ScoreBucket] end: " + end);
        while (historySize < end) {
            int index    = history[historySize++];
            int oldScore = history[historySize++];
            int newScore = history[historySize++];
            
            // System.out.printf("[ScoreBucket] Redo: index=%d, oldScore=%d -> newScore=%d\n", index, oldScore, newScore);

            buckets[oldScore].remove((Integer) index);
            if (newScore != 0)
                buckets[newScore].add(index);
        }
    }

    public void undoMove() {
        int start = moveBoundaries[moveCount];

        // System.out.println("[ScoreBucket] undoMove");
        // System.out.println("[ScoreBucket] historySize: " + historySize);
        // System.out.println("[ScoreBucket] moveCount: " + moveCount);
        // System.out.println("[ScoreBucket] start: " + start);
        while (historySize > start) {
            int newScore = history[--historySize];
            int oldScore = history[--historySize];
            int index    = history[--historySize];

            // System.out.printf("[ScoreBucket] Undo: index=%d, oldScore=%d -> newScore=%d\n", index, oldScore, newScore);

            buckets[newScore].remove((Integer) index);
            if (oldScore != 0)
                buckets[oldScore].add(index);
        }
        moveCount--;
    }

    public void update(int index, int oldScore, int newScore) {
        history[historySize++] = index;
        history[historySize++] = oldScore;
        history[historySize++] = newScore;

        buckets[oldScore].remove((Integer) index);
        if (newScore != 0)
            buckets[newScore].add(index);

        moveBoundaries[moveCount + 1] = historySize;
    }

    public List<Integer> getMovesSorted() {
        List<Integer> result = new ArrayList<>();
        for (int score = buckets.length - 1; score >= 0; score--) {
            result.addAll(buckets[score]);
        }
        return result;
    }

    public List<Integer> getMovesSortedAbove(int minScore) {
        List<Integer> result = new ArrayList<>();
        for (int score = buckets.length - 1; score >= minScore; score--) {
            result.addAll(buckets[score]);
        }
        return result;
    }

    public void printCurrentBuckets(){
        for (int score = buckets.length - 1; score >= 0; score--) {
            if (buckets[score].isEmpty() == false){
                System.out.printf("bucket score: %3d: ", score);
                boolean first = true;
                for (int index : buckets[score]){
                    if (!first) System.out.print(", ");
                    first = false;
                    System.out.print(index);
                }
                System.out.println();
            }
        }
    }
}