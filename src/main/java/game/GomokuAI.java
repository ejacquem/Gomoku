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
        for (PosScore pos : sortedPos) {
            // System.out.println("sortedIndices: " + pos.index);
            board.placePieceAt(pos.index);
            boardAnalyser.scanLastMove();
            // System.out.println("player: " + board.getCurrentPlayer());
            int score = -search(depth, Integer.MIN_VALUE, Integer.MAX_VALUE); // = negamax(rootNode, depth, −∞, +∞);
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

        printThinkingResult(bestEval, bestMove);

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

    // public void evaluate() {
    //     // System.out.println("Evaluating");
    //     player1Score.set(findAndSumMatch(patterns, 1, 2, game.getCurrentPlayer() == 1 ? 1 : 0));
    //     player2Score.set(findAndSumMatch(patterns2, 2, 1, game.getCurrentPlayer() == 2 ? 1 : 0));

    //     System.out.println("Eval: P1 " + player1Score.get() + " P2 " + player2Score.get());

    //     double score1 = (double)player1Score.get();
    //     double score2 = (double)player2Score.get();
    //     percentage.set(score2 / (score1 + score2));
    // }

    private int[] playerPositionScore = new int[2];
    public int evaluate(int depth) {
        if (board.getWinner() != 0) {
            int score = (50_000 - (MAX_DEPTH - depth) * 10);
            if (board.getWinner() != board.getCurrentPlayer()) {
                return -score;
            }
            return score;
        }
        int color = board.getCurrentPlayer() == 1 ? 1 : -1;
        boardAnalyser.getPlayerScore(playerPositionScore);
        int player1Score = playerPositionScore[0] + captureScore[board.getCaptureCount(1) / 2];
        int player2Score = playerPositionScore[1] + captureScore[board.getCaptureCount(2) / 2];
        int positionScore = player1Score - player2Score;
        return color * positionScore;
    }

    // public double evaluatepercent(int depth) {
    //     boardAnalyser.updateMoveCount();
    //     if (board.getWinner() == 1)
    //         return (int)(+1000 * depthFactor(depth, 0.5f));
    //     if (board.getWinner() == 2)
    //         return (int)(-1000 * depthFactor(depth, 0.5f));
    //     boardAnalyser.getPlayerScore(playerPositionScore);
    //     int player1Score = playerPositionScore[0] + captureScore[board.getCaptureCount(1) / 2];
    //     int player2Score = playerPositionScore[1] + captureScore[board.getCaptureCount(2) / 2];

    //     System.out.printf("Player1 | %-10s | %-15s | %-10s%n", "Score", "Position Score", "Capture Score");
    //     System.out.printf("Player1 | %-10d | %-15d | %-10d%n",
    //     player1Score, playerPositionScore[0], captureScore[board.getCaptureCount(1) / 2]);
    //     System.out.printf("Player2 | %-10d | %-15d | %-10d%n",
    //     player2Score, playerPositionScore[1], captureScore[board.getCaptureCount(2) / 2]);

    //     double positionScore = (double)(player2Score + 1) / (double)(player2Score + player1Score + 1);
    //     return positionScore;
    // }

    // move 0 : - 0%
    // move MAX_ITERATION : - 50%
    // private float depthFactor(int depth, float maxFactor) {
    //     return 1f - (((MAX_DEPTH - depth) / (float)MAX_DEPTH) * maxFactor);
    // }

    public boolean timeLimitExceeded() {
        if ((System.currentTimeMillis() - start) >= TIME_LIMIT) {
            limitExcceeded = true;
        }
        return limitExcceeded;
    }

    /*
    https://en.wikipedia.org/wiki/Negamax
    function negamax(node, depth, α, β, color) is
    if depth = 0 or node is a terminal node then
        return color × the heuristic value of node

    childNodes := generateMoves(node)
    childNodes := orderMoves(childNodes)
    value := −∞
    foreach child in childNodes do
        value := max(value, −negamax(child, depth − 1, −β, −α, −color))
        α := max(α, value)
        if α ≥ β then
            break (* cut-off *)
    return value


    (* Initial call for Player A's root node *)
    negamax(rootNode, depth, −∞, +∞, 1)

     */
    public int search(int depth, int alpha, int beta) {
        iterationPerDepth[MAX_DEPTH - depth]++;
        if (depth == 1 || timeLimitExceeded() || board.getWinner() != 0) {
            // int eval = evaluate(depth);
            // if (eval > 0) {
            //     System.out.println("Search stop at depth: " + depth + " player: " + board.getCurrentPlayer());
            // }
            // return eval;
            return evaluate(depth);
        }

        List<PosScore> sortedPos = boardAnalyser.getSortedPositions();

        int value = -INF;
        for (PosScore pos : sortedPos) {
            if (depth <= MAX_DEPTH - 2 && pos.score <= 1) {
                break;
            }
            if (depth <= MAX_DEPTH - 3 && pos.score <= 2) {
                break;
            }
            if (depth <= MAX_DEPTH - 4 && pos.score < 5) {
                break;
            }
            // if (depth <= MAX_DEPTH - 6 && pos.score < 50) {
            //     break;
            // }
            if (depth <= MAX_DEPTH - 5 && pos.score < 100) {
                break;
            }
            board.placePieceAt(pos.index);
            boardAnalyser.scanLastMove();
            
            value = Math.max(value, -search(depth - 1, -beta, -alpha));
            alpha = Math.max(alpha, value);

            board.undo();

            if (alpha >= beta) {
                prunningCount++;
                prunningPerDepth[MAX_DEPTH - depth]++;
                break;
            }
        }
        if (value == -INF) {
            return evaluate(depth);
        }
        return value;
    }
}
