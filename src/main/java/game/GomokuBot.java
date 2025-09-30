package main.java.game;

import java.util.List;
import java.util.concurrent.*;

import main.java.utils.TranspositionTable;

// Worker that evaluates a single move
class GomokuBot implements Callable<Integer> {
    private final BoardAnalyser boardAnalyser;
    private final Board board;

    public final long TIME_LIMIT = 500;
    public boolean limitExcceeded = false;
    private long start = 0;
    public final int startIndex;
    public final int id;

    public final int INF = 100_000_000;
    public final int MAX_DEPTH;
    private int maxDepth;
    public final int WIN_SCORE = 1_000_000;
    public final int[] captureScore = new int[]{0, 10000, 20000, 30000, 50000, WIN_SCORE};
    public int[] iterationPerDepth;
    public int[] prunningPerDepth;
    public int[] ttPrunningPerDepth;
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
        ttPrunningPerDepth = new int[MAX_DEPTH + 1];
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
        for (int i = 2; i <= MAX_DEPTH; i++){
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
            int score = (WIN_SCORE);
            // int score = (WIN_SCORE - (depth) * 100);
            if (board.getWinner() != board.getCurrentPlayer()) {
                return -score;
            }
            return score;
        }
        // return 0;
        int color = board.getCurrentPlayer() == 1 ? 1 : -1;
        int player1CaptureScore = captureScore[board.getCaptureCount(1) / 2];
        int player2CaptureScore = captureScore[board.getCaptureCount(2) / 2];
        // if (player1CaptureScore > 0) player1CaptureScore -= (depth) * 10;
        // if (player2CaptureScore > 0) player2CaptureScore -= (depth) * 10;
        int captureScore = player1CaptureScore - player2CaptureScore;

        double p = boardAnalyser.getEvaluationPercentage();
        p = (p - 0.5) * 2.; // -1 to 1
        int positionScore = (int)(p * 100.);
        // int positionScore = 0;
        return color * Math.min(WIN_SCORE, Math.max(-WIN_SCORE, positionScore + captureScore));
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
        int alphaOrigin = alpha;
        iterationPerDepth[depth]++;

        if (GomokuAI.useTT){
            int score = getScoreFromCache(depth, alpha, beta);
            if (score != -INF){
                ttPrunningPerDepth[depth]++;
                return score;
            }
        }

        if (depth == maxDepth || timeLimitExceeded() || board.getWinner() != 0) {
            return evaluate(depth);
        }

        // List<PosScore> sortedPos = boardAnalyser.getSortedPositions();

        int minScore = 1;
        switch (depth) {
            case 0, 1, 2: minScore = 1; break;
            case 3: minScore = 2; break;
            case 4: minScore = 20; break;
            default: minScore = 50; break;
        }

        List<Integer> sortedPos = boardAnalyser.scoreBuckets.getMovesSortedAbove(minScore);

        int value = -INF;
        int bestMove = 0;
        int bestEval = value;
        for (int index : sortedPos) {
            board.placePieceAt(index);
            
            value = Math.max(value, -search(depth + 1, -beta, -alpha));
            alpha = Math.max(alpha, value);

            board.undo();


            if (value > bestEval) {
                bestEval = value;
                bestMove = index;
            }
            if (alpha >= beta) {
                prunningCount++;
                prunningPerDepth[depth]++;
                break;
            }
        }
        if (value == -INF) {
            return evaluate(depth);
        }

        if (GomokuAI.useTT){
            storeSearch(depth, value, bestMove, alphaOrigin, beta);
        }

        return value;
    }

    // (* Transposition Table Store; node is the lookup key for ttEntry *)
    // if value ≤ alphaOrig then
    //     ttEntry.flag := UPPERBOUND
    // else if value ≥ β then
    //     ttEntry.flag := LOWERBOUND
    // else
    //     ttEntry.flag := EXACT
    // ttEntry.value := value
    // ttEntry.depth := depth
    // ttEntry.is_valid := true
    // transpositionTableStore(node, ttEntry)
    private void storeSearch(int depth, int value, int bestMove, int alpha, int beta){
        int searchedDepth = maxDepth - depth;

        int flag = 0;
        if (value <= alpha)
            flag = TranspositionTable.UPPERBOUND;
        else if (value >= beta)
            flag = TranspositionTable.LOWERBOUND;
        else
            flag = TranspositionTable.EXACT;
        TranspositionTable.store(board.getZobristKey(), searchedDepth, value, bestMove, flag);
    }

    // (* Transposition Table Lookup; node is the lookup key for ttEntry *)
    // ttEntry := transpositionTableLookup(node)
    // if ttEntry.is_valid and ttEntry.depth ≥ depth then
    //     if ttEntry.flag = EXACT then
    //         return ttEntry.value
    //     else if ttEntry.flag = LOWERBOUND and ttEntry.value ≥ beta then
    //         return ttEntry.value
    //     else if ttEntry.flag = UPPERBOUND and ttEntry.value ≤ alpha then
    //         return ttEntry.value
    // private boolean foundInTT(int depth, int alpha, int beta) {
    //     return false;
    // }
    private int getScoreFromCache(int depth, int alpha, int beta){
        int searchedDepth = maxDepth - depth;
        long data = TranspositionTable.probe(board.getZobristKey());
        int dataDepth = TranspositionTable.getDepth(data);
        if (data != 0 && dataDepth > searchedDepth + 1){
            int flag = TranspositionTable.getFlag(data);
            int value = TranspositionTable.getScore(data);
            if ((flag == TranspositionTable.EXACT) || 
            (flag == TranspositionTable.LOWERBOUND && value >= beta) ||
            (flag == TranspositionTable.UPPERBOUND && value <= alpha)){
                // System.out.println(
                // "[TT PRUNE] depth=" + depth +
                // " searchedDepth=" + searchedDepth +
                // " data depth=" + dataDepth +
                // " alpha=" + alpha +
                // " beta=" + beta +
                // " flag=" + flag +
                // " value=" + value);
                return value;
            }
        }
        return -INF;
    }
}