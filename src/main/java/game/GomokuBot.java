package main.java.game;

import java.util.List;
import java.util.concurrent.*;

// Worker that evaluates a single move
class GomokuBot implements Callable<Integer> {
    private final BoardAnalyser boardAnalyser;
    private final Board board;

    public final long TIME_LIMIT = 5000;
    public boolean limitExcceeded = false;
    private long start = 0;
    public final int startIndex;
    public final int id;

    public final int INF = 1_000_000;
    public final int MAX_DEPTH;
    private int maxDepth;
    public final int[] captureScore = new int[]{0, 1000, 2000, 3000, 5000, 10000};
    public int[] iterationPerDepth;
    public int[] prunningPerDepth;
    public int prunningCount = 0;

    public volatile int currentDepth = 0;
    public volatile int currentBestEval = 0;

    public GomokuBot(BoardAnalyser boardAnalyser, int maxDepth, int id, int startIndex) {
        // deep copy so each thread works independently
        this.boardAnalyser = boardAnalyser.deepCopy();
        this.board = this.boardAnalyser.board;
        this.MAX_DEPTH = maxDepth;
        this.id = id;
        this.startIndex = startIndex;
        iterationPerDepth = new int[MAX_DEPTH + 1];
        prunningPerDepth = new int[MAX_DEPTH + 1];
    }

    public void setMaxDepth(int depth){}
    
    @Override
    public Integer call() {
        // String s = "";
        // s = board.toString() + "@@@@@@@";
        // System.out.println("@@@@@@@\nThread: " + id + " doing its job\n" + s);
        // return captureScore[board.getCaptureCount(1) / 2];
        board.placePieceAt(startIndex);
        start = System.currentTimeMillis();
        int score = 0;
        for (int i = 2; i < MAX_DEPTH; i++){
            int temp = -search(0, -INF, INF);
            currentDepth = i;
            maxDepth = i;
            if (!timeLimitExceeded()) {
                score = temp;
                currentBestEval = score;
            }
            else {
                break;
            }

        }
        return score;
        // return -search(MAX_DEPTH, -INF, INF);
    }

    // private int[] playerPositionScore = new int[2];
    public int evaluate(int depth) {
        if (board.getWinner() != 0) {
            int score = (50_000 - (depth) * 10);
            if (board.getWinner() != board.getCurrentPlayer()) {
                return -score;
            }
            return score;
        }
        int color = board.getCurrentPlayer() == 1 ? 1 : -1;
        int player1CaptureScore = captureScore[board.getCaptureCount(1) / 2];
        int player2CaptureScore = captureScore[board.getCaptureCount(2) / 2];
        if (player1CaptureScore > 0) player1CaptureScore -= (depth) * 10;
        if (player2CaptureScore > 0) player2CaptureScore -= (depth) * 10;
        int captureScore = player1CaptureScore - player2CaptureScore;

        double p = boardAnalyser.getEvaluationPercentage();
        p = (p - 0.5) * 2.; // -1 to 1
        int positionScore = (int)(p * 100.);
        return color * (positionScore + captureScore);
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
        iterationPerDepth[depth]++;
        if (depth == maxDepth || timeLimitExceeded() || board.getWinner() != 0) {
            return evaluate(depth);
        }

        // List<PosScore> sortedPos = boardAnalyser.getSortedPositions();

        int minScore = 1;
        // switch (depth) {
        //     case 0: minScore = 1; break;
        //     case 1: minScore = 1; break;
        //     case 2: minScore = 2; break;
        //     case 3: minScore = 20; break;
        //     default: minScore = 50; break;
        // }
        switch (depth) {
            case 0, 1, 2: minScore = 1; break;
            case 3: minScore = 1; break;
            case 4: minScore = 2; break;
            case 5: minScore = 20; break;
            default: minScore = 50; break;
        }

        List<Integer> sortedPos = boardAnalyser.scoreBuckets.getMovesSortedAbove(minScore);

        int value = -INF;
        for (int index : sortedPos) {
            board.placePieceAt(index);
            
            value = Math.max(value, -search(depth + 1, -beta, -alpha));
            alpha = Math.max(alpha, value);

            board.undo();

            if (alpha >= beta) {
                prunningCount++;
                prunningPerDepth[depth]++;
                break;
            }
        }
        if (value == -INF) {
            return evaluate(depth);
        }
        return value;
    }
}