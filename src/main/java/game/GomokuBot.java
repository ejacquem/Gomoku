package main.java.game;

import java.util.List;
import java.util.concurrent.*;

import main.java.game.BoardAnalyser.PosScore;

// Worker that evaluates a single move
class GomokuBot implements Callable<Integer> {
    private final BoardAnalyser boardAnalyser;
    private final Board board;

    public final long TIME_LIMIT = 30000;
    public boolean limitExcceeded = false;
    private long start = 0;
    public final int id;

    public final int INF = 1_000_000;
    public final int MAX_DEPTH;
    public final int[] captureScore = new int[]{0, 1000, 2000, 3000, 4000, 50000};
    public int[] iterationPerDepth;
    public int[] prunningPerDepth;
    public int prunningCount = 0;

    public GomokuBot(BoardAnalyser boardAnalyser, int maxDepth, int id) {
        // deep copy so each thread works independently
        this.boardAnalyser = boardAnalyser.deepCopy(); 
        this.board = this.boardAnalyser.board;
        this.MAX_DEPTH = maxDepth;
        this.id = id;
        iterationPerDepth = new int[MAX_DEPTH];
        prunningPerDepth = new int[MAX_DEPTH];
    }

    public void setMaxDepth(int depth){}
    
    @Override
    public Integer call() {
        // String s = "";
        // s = board.toString() + "@@@@@@@";
        // System.out.println("@@@@@@@\nThread: " + id + " doing its job\n" + s);
        // return captureScore[board.getCaptureCount(1) / 2];
        start = System.currentTimeMillis();
        return -search(MAX_DEPTH, -INF, INF);
    }

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
        int player1CaptureScore = captureScore[board.getCaptureCount(1) / 2];
        int player2CaptureScore = captureScore[board.getCaptureCount(2) / 2];
        if (player1CaptureScore > 0) player1CaptureScore -= (MAX_DEPTH - depth) * 100;
        if (player2CaptureScore > 0) player2CaptureScore -= (MAX_DEPTH - depth) * 100;
        // int player1Score = playerPositionScore[0] + player1CaptureScore;
        // int player2Score = playerPositionScore[1] + player2CaptureScore;
        // int positionScore = player1Score - player2Score;
        int positionScore = player1CaptureScore - player2CaptureScore;
        return color * positionScore;
    }

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
     */
    public int search(int depth, int alpha, int beta) {
        iterationPerDepth[MAX_DEPTH - depth]++;
        if (depth == 1 || timeLimitExceeded() || board.getWinner() != 0) {
            return evaluate(depth);
        }

        List<PosScore> sortedPos = boardAnalyser.getSortedPositions();

        int value = -INF;
        for (PosScore pos : sortedPos) {
            if (depth <= MAX_DEPTH - 1 && pos.score <= 1) {
                break;
            }
            if (depth <= MAX_DEPTH - 2 && pos.score <= 2) {
                break;
            }
            if (depth <= MAX_DEPTH - 3 && pos.score < 20) {
                break;
            }
            if (depth <= MAX_DEPTH - 4 && pos.score < 100) {
                break;
            }
            board.placePieceAt(pos.index);
            
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