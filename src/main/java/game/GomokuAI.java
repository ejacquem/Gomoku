package main.java.game;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import main.java.game.BoardAnalyser.PosScore;

public class GomokuAI {
    public BoardAnalyser boardAnalyser;

    private final LongProperty player1Score = new SimpleLongProperty(1);
    private final LongProperty player2Score = new SimpleLongProperty(1);
    private final DoubleProperty percentage = new SimpleDoubleProperty(0f);

    public LongProperty player1ScoreProperty() { return player1Score; }
    public LongProperty player2ScoreProperty() { return player2Score; }
    public DoubleProperty percentageProperty() { return percentage; }

    public int[] iterationPerDepth;
    public int[] prunningPerDepth;
    public int prunningCount = 0;
    private List<GomokuBot> bots = new ArrayList<>();
    private ExecutorService executor;

    private long start = 0;
    public final int MAX_DEPTH = 30;
    private AIState state = AIState.READY;
    private int bestEval;

    volatile private int bestMove;

    public enum AIState {
        READY,
        THINKING,
        IDLE,
        STOP
    }

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

    public GomokuAI() {
    }

    public AIState getState(){
        return state;
    }

    public void setState(AIState state){
        this.state = state;
    }

    public int getBestMove(){
        return bestMove;
    }

    public int getComputationResult(){
        setState(AIState.READY);
        return bestMove;
    }

    public void reset() {
        // private ExecutorService executor;
        evaluatedPos.clear();
        start = 0;
        bots.clear();
        iterationPerDepth = new int[MAX_DEPTH];
        prunningPerDepth = new int[MAX_DEPTH];

        prunningCount = 0;

        state = AIState.READY;
        bestEval = 0;
        bestMove = 0;
        boardAnalyser = null;
    }

    // public int getBestMove() {
    //     return getBestMove(MAX_DEPTH);
    // }

    public void makeBestMove(BoardAnalyser boardAnalyser) {
        System.out.println("make best move called");
        new Thread(() -> {
            reset();
            this.boardAnalyser = boardAnalyser.deepCopy();
            state = AIState.THINKING;
            // System.out.println("Ai calculate best move for player: " + (board.getCurrentPlayer() == 1 ? " white" : " black"));
            start = System.currentTimeMillis();

            computeBestEval();
    
            printThinkingResult(bestEval, bestMove);
    
        }).start();
    }

    private void computeBestEval(){
        List<PosScore> sortedPos = boardAnalyser.getSortedPositions();
        bestEval = Integer.MIN_VALUE + 1;
        bestMove = 0;

        executor = Executors.newFixedThreadPool(sortedPos.size());
        List<Future<Integer>> result = new ArrayList<>(sortedPos.size());
        bots.clear();
        for (int i = 0; i < sortedPos.size(); i++) {
            PosScore pos = sortedPos.get(i);
            // System.out.println("----------- Ai launch thread " + i + "\n" + board.toString());
            // System.out.println("Laucnh Bot for move " + GomokuUtils.indexToString(pos.index));
            bots.add(new GomokuBot(boardAnalyser, MAX_DEPTH, i, pos.index));
            Future<Integer> future = executor.submit(bots.get(i));
            result.add(future);
        }

        for (int i = 0; i < sortedPos.size(); i++) {
            PosScore pos = sortedPos.get(i);
            int score = 0;
            try {
                score = result.get(i).get().intValue(); // blocking
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            evaluatedPos.add(new EvaluatedPosition(Coords.getCoordsById(pos.index), score));
            if (score > bestEval) {
                bestEval = score;
                bestMove = pos.index;
            }
        }

        executor.shutdown();
        state = AIState.IDLE;
    }

    public List<PosScore> getCurrentSearchDepth() {
        List<PosScore> list = new ArrayList<>();
        for (GomokuBot bot : bots){
            list.add(new PosScore(bot.startIndex, bot.currentDepth));
        }
        return list;
    }

    public List<PosScore> getCurrentBestEvals() {
        List<PosScore> list = new ArrayList<>();
        for (GomokuBot bot : bots){
            list.add(new PosScore(bot.startIndex, bot.currentBestEval));
        }
        return list;
    }

    // private void printThinkingResult(int bestEval, int bestMove) {
    //     System.out.println("Best Move Score: " + bestEval);

    //     long totalTime = System.currentTimeMillis() - start;
    //     System.out.printf("Execution time: %d ms%n", totalTime);

    //     System.out.println("Best move: " + bestMove);
    // }

    private void printThinkingResult(int bestEval, int bestMove) {
        System.out.println("Best Move Score: " + bestEval);

        long totalTime = System.currentTimeMillis() - start;
        System.out.printf("Execution time: %d ms%n", totalTime);

        for (GomokuBot bot : bots) {
            for (int i = 0; i < MAX_DEPTH; i++) {
                iterationPerDepth[i] += bot.iterationPerDepth[i];
                prunningPerDepth[i] += bot.prunningPerDepth[i];
            }
        }

        System.out.println("prunningCount: " + prunningCount);
        // System.out.println("weakPrunningCount: " + weakPrunningCount);
        for (int i = 0; i < MAX_DEPTH; i++) {
            System.out.println("Iteration at depth " + (i + 1 < 10 ? " " : "") + (i + 1) + ": " + iterationPerDepth[i] + ", " + prunningPerDepth[i]);
        }
        // percentage.set(evaluatepercent(1));
        System.out.println("Best move: " + bestMove);
    }
}
