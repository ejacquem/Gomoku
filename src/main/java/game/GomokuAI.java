package main.java.game;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import main.java.game.BoardAnalyser.PosScore;

public class GomokuAI {
    private Board board;
    public BoardAnalyser boardAnalyser;

    private final LongProperty player1Score = new SimpleLongProperty(1);
    private final LongProperty player2Score = new SimpleLongProperty(1);
    private final DoubleProperty percentage = new SimpleDoubleProperty(0f);

    public LongProperty player1ScoreProperty() { return player1Score; }
    public LongProperty player2ScoreProperty() { return player2Score; }
    public DoubleProperty percentageProperty() { return percentage; }

    public final long TIME_LIMIT = 30000;
    public final int INF = 1_000_000;
    private long start = 0;
    public final int MAX_DEPTH = 10;
    public boolean limitExcceeded = false;
    public int[] iterationPerDepth = new int[MAX_DEPTH];
    public int[] prunningPerDepth = new int[MAX_DEPTH];
    public int prunningCount = 0;
    public int weakPrunningCount = 0;
    public int iter0 = 0;
    public final int[] captureScore = new int[]{0, 1000, 2000, 5000, 10000, 50000};

    // private float player1PositionScore = 0f;
    // private float player2PositionScore = 0f;

    public List<EvaluatedPosition> evaluatedPos = new ArrayList<>();

    public class EvaluatedPosition{
        public Coords pos;
        public int score;

        EvaluatedPosition (Coords pos, int score) {
            this.pos = pos; 
            this.score = score; 
        }
    }

    public GomokuAI(Board board, BoardAnalyser boardAnalyser) {
        this.board = board;
        this.boardAnalyser = boardAnalyser;
    }

    public void reset() {
        evaluatedPos.clear();
        limitExcceeded = false;
        start = 0;
        prunningCount = 0;
        // player1PositionScore = 0f;
        // player2PositionScore = 0f;
        for (int i = 0; i < MAX_DEPTH; i++) {
            iterationPerDepth[i] = 0;
            prunningPerDepth[i] = 0;
        }
    }

    public int getBestMove() {
        return getBestMove(MAX_DEPTH);
    }

    public int getBestMove(int depth) {
        reset();
        int player = board.getCurrentPlayer();
        System.out.println("Ai calculate best move for player: " + player + (board.getCurrentPlayer() == 1 ? " white" : " black"));
        start = System.currentTimeMillis();
        limitExcceeded = false;
        
        // int[] sortedIndices = boardAnalyser.getSortedIndices();
        List<PosScore> sortedPos = boardAnalyser.getSortedPositions();
        
        int bestEval = Integer.MIN_VALUE + 1;
        int color = board.getCurrentPlayer() == 1 ? 1 : -1;
        System.out.println("color: " + color );
        int bestMove = 0;

        // int i = 0;
        GomokuBot bots[] = new GomokuBot[sortedPos.size()];
        for (int i = 0; i < sortedPos.size(); i++) {
            PosScore pos = sortedPos.get(i);
            // System.out.println("sortedIndices: " + pos.index);
            board.placePieceAt(pos.index);
            boardAnalyser.scanLastMove();
            // System.out.println("player: " + board.getCurrentPlayer());
            // int score = -search(depth, Integer.MIN_VALUE, Integer.MAX_VALUE); // = negamax(rootNode, depth, −∞, +∞);
            
            bots[i] = new GomokuBot(boardAnalyser, MAX_DEPTH);
            GomokuBot bot = bots[i];
            int score = bot.call();

            evaluatedPos.add(new EvaluatedPosition(Coords.getCoordsById(pos.index), score));
            // System.out.println("searchReturnValue: " + searchReturnValue);
            // System.out.println("Move score: " + score);
            if (score > bestEval) {
                bestEval = score;
                bestMove = pos.index;
            }
            board.undo();
            // GomokuUtils.printLoadingBar(++i / (double)sortedPos.size());
        }

        // printThinkingResult(bestEval, bestMove);

        return bestMove;
    }

    private void printThinkingResult(int bestEval, int bestMove) {
        if (limitExcceeded) {
            System.out.println("Time Limit exceeded !");
        }
        System.out.println("Best Move Score: " + bestEval);

        long totalTime = System.currentTimeMillis() - start;
        System.out.printf("Execution time: %d ms%n", totalTime);

        System.out.println("prunningCount: " + prunningCount);
        System.out.println("weakPrunningCount: " + weakPrunningCount);
        for (int i = 0; i < MAX_DEPTH; i++) {
            System.out.println("Iteration at depth " + (i + 1 < 10 ? " " : "") + (i + 1) + ": " + iterationPerDepth[i] + ", " + prunningPerDepth[i]);
        }
        // percentage.set(evaluatepercent(1));
        System.out.println("Best move: " + bestMove);
    }

}
