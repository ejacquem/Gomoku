package main.java;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import main.java.game.Board;
import main.java.game.BoardAnalyser;
import main.java.game.CellInfo;
import main.java.game.Coords;

public class GomokuAI {
    private Board board;
    public BoardAnalyser boardAnalyser;

    private final LongProperty player1Score = new SimpleLongProperty(1);
    private final LongProperty player2Score = new SimpleLongProperty(1);
    private final DoubleProperty percentage = new SimpleDoubleProperty(0f);

    public LongProperty player1ScoreProperty() { return player1Score; }
    public LongProperty player2ScoreProperty() { return player2Score; }
    public DoubleProperty percentageProperty() { return percentage; }

    public final long TIME_LIMIT = 10000;
    public final int MAX_DEPTH = 7;
    public boolean limitExcceeded = false;
    public long start;
    public long sortTimer, sortTimeStart;
    public long boardActionTimer, boardActionTimeStart;
    public int[] iterationPerDepth = new int[MAX_DEPTH];
    public int[] prunningPerDepth = new int[MAX_DEPTH];
    public int prunningCount = 0;
    public int iter0 = 0;
    public final int[] captureScore = new int[]{0, 1000, 2000, 5000, 10000, 1000000};

    // private float player1PositionScore = 0f;
    // private float player2PositionScore = 0f;

    public List<EvaluatedPosition> evaluatedPos = new ArrayList<>();

    public class EvaluatedPosition{
        public Coords pos;
        public int score;

        EvaluatedPosition (Coords pos, int score){
            this.pos = pos; 
            this.score = score; 
        }
    }

    Pattern[] patterns = {
        new Pattern(new int[]{1,1,1,1,1}, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,1,1,0}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,1,0,1}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,0,1,1}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,0,1,1,1}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{2,1,1,1,1,0}, 150, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,1,0,0}, 150, 10000),
        new Pattern(new int[]{0,1,1,1,0}, 100, 150),
        new Pattern(new int[]{2,1,1,1,0,0}, 50, 150),
        new Pattern(new int[]{2,1,0,1,0,1,0}, 50, 100),
        new Pattern(new int[]{0,1,1,0}, 20, 30),
        new Pattern(new int[]{2,1,1,0}, 10, 15),
    };
    Pattern[] patterns2;

    GomokuAI(Board board, BoardAnalyser boardAnalyser){
        this.board = board;
        this.boardAnalyser = boardAnalyser;

        createPattern2();
        // game.currentPlayerProperty().addListener((obs, oldVal, newVal) -> {
        //     evaluate();
        // });
    }

    public class Pattern {
        int [] pattern;
        long score[] = new long[2]; // store the basic score, and the score if its the player turn

        Pattern(int [] pattern, long score){
            this.pattern = pattern;
            this.score[0] = score;
            this.score[1] = score;
        }

        Pattern(int [] pattern, long score, long scorePlayerTurn){
            this.pattern = pattern;
            this.score[0] = score;
            this.score[1] = scorePlayerTurn;
        }
    }

    //copy patterns but reverse player 1 and player 2
    private void createPattern2(){
        patterns2 = new Pattern[patterns.length];
        for (int i = 0; i < patterns.length; i++){
            patterns2[i] = new Pattern(new int[patterns[i].pattern.length], patterns[i].score[0], patterns[i].score[1]);
            for (int j = 0; j < patterns[i].pattern.length; j++) {
                int p1 = patterns[i].pattern[j];
                if (p1 == 0) patterns2[i].pattern[j] = 0;
                else if (p1 == 1) patterns2[i].pattern[j] = 2;
                else if (p1 == 2) patterns2[i].pattern[j] = 1;
            }
        }
    }

    public void reset(){
        evaluatedPos.clear();
        limitExcceeded = false;
        start = 0;
        prunningCount = 0;
        // player1PositionScore = 0f;
        // player2PositionScore = 0f;
        for (int i = 0; i < MAX_DEPTH; i++){
            iterationPerDepth[i] = 0;
            prunningPerDepth[i] = 0;
        }
    }

    public int getBestMove(){
        reset();
        System.out.println("Ai calculate best move");
        start = System.currentTimeMillis();
        sortTimer = 0;
        boardActionTimer = 0;
        limitExcceeded = false;

        // Coords[] moves = getPossibleMove();
        // CellInfo[] sortedMoves = sortMoves(moves);
        int[] sortedIndices = boardAnalyser.getSortedIndices();
        // shuffleSameScore(sortedMoves);

        int bestEval = Integer.MIN_VALUE;
        int color = board.getCurrentPlayer() == 1 ? 1 : -1;
        int bestMove = 0;

        // int maxMove = 0;
        for (int index : sortedIndices){
            // if (maxMove >= 3 && move.score.getScore() < 10f) break;
            // if (maxMove >= 5 && move.score.getScore() < 25f) break;
            // if (maxMove == 7) break;
            board.placePieceAt(index);
            int score = search(MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, color); // = negamax(rootNode, depth, −∞, +∞, 1)
            // evaluatedPos.add(new EvaluatedPosition(pos, score));
            // int score = evaluate();
            // System.out.println("Move score: " + score);
            if (score > bestEval){
                bestEval = score;
                bestMove = index;
            }
            board.undo();
            // maxMove++;
        }
        if (limitExcceeded){
            System.out.println("Time Limit exceeded !");
        }
        System.out.println("Best Move Score: " + bestEval);

        long totalTime = System.currentTimeMillis() - start;
        System.out.printf("Execution time: %d ms%n", totalTime);
        System.out.printf("  sortTime:        %d ms (%.2f%%)%n", sortTimer, (sortTimer * 100.0) / totalTime);
        System.out.printf("  boardActionTime: %d ms (%.2f%%)%n", boardActionTimer, (boardActionTimer * 100.0) / totalTime);
        
        System.out.println("prunningCount: " + prunningCount);
        for (int i = 0; i < MAX_DEPTH; i++){
            System.out.println("Iteration at depth " + (i + 1 < 10 ? " " : "") + (i + 1) + ": " + iterationPerDepth[i] + ", " + prunningPerDepth[i]);
        }
        percentage.set(evaluatepercent(1));
        return bestMove;
    }

    // public void evaluate(){
    //     // System.out.println("Evaluating");
    //     player1Score.set(findAndSumMatch(patterns, 1, 2, game.getCurrentPlayer() == 1 ? 1 : 0));
    //     player2Score.set(findAndSumMatch(patterns2, 2, 1, game.getCurrentPlayer() == 2 ? 1 : 0));

    //     System.out.println("Eval: P1 " + player1Score.get() + " P2 " + player2Score.get());

    //     double score1 = (double)player1Score.get();
    //     double score2 = (double)player2Score.get();
    //     percentage.set(score2 / (score1 + score2));
    // }

    public int evaluate(int depth){
        if (board.getWinner() == 1)
            return (int)(+10000000 * depthFactor(depth, 0.5f));
        if (board.getWinner() == 2)
            return (int)(-10000000 * depthFactor(depth, 0.5f));
        // int player1Score = (int)(player1PositionScore) + captureScore[board.getCaptureCount(1) / 2];
        // int player2Score = (int)(player2PositionScore) + captureScore[board.getCaptureCount(2) / 2];
        int player1Score = captureScore[board.getCaptureCount(1) / 2];
        int player2Score = captureScore[board.getCaptureCount(2) / 2];
        int positionScore = player1Score - player2Score;
        // System.out.println("evaluate called: " + positionScore);
        return positionScore;
    }

    public double evaluatepercent(int depth){
        if (board.getWinner() == 1)
            return (+10000000 * depthFactor(depth, 0.5f));
        if (board.getWinner() == 2)
            return (-10000000 * depthFactor(depth, 0.5f));

        // Coords[] moves = getPossibleMove();
        // CellInfo[] sortedMoves = sortMoves(moves);
        // int[] sortedIndices = boardAnalyser.getSortedIndices();

        double player1PositionScore = 0, player2PositionScore = 0;
        // for (int index : sortedIndices){
        //     player1PositionScore += move.getPlayerScore(1);
        //     player2PositionScore += move.getPlayerScore(2);
        // }

        double player1Score = player1PositionScore + captureScore[board.getCaptureCount(1) / 2];
        double player2Score = player2PositionScore + captureScore[board.getCaptureCount(2) / 2];

        System.out.printf("Player1 | %-10s | %-15s | %-10s%n", "Score", "Position Score", "Capture Score");
        System.out.printf("Player1 | %-10.1f | %-15.1f | %-10d%n",
        player1Score, player1PositionScore, captureScore[board.getCaptureCount(1) / 2]);
        System.out.printf("Player2 | %-10.1f | %-15.1f | %-10d%n",
        player2Score, player2PositionScore, captureScore[board.getCaptureCount(2) / 2]);

        double positionScore = player2Score / (player2Score + player1Score);
        return positionScore;
    }

    // move 0 : - 0%
    // move MAX_ITERATION : - 50%
    private float depthFactor(int depth, float maxFactor){
        return 1f - (((MAX_DEPTH - depth) / (float)MAX_DEPTH) * maxFactor);
    }

    public boolean timeLimitExceeded(){
        if ((System.currentTimeMillis() - start) >= TIME_LIMIT) {
            limitExcceeded = true;
        }
        return limitExcceeded;
    }
    

    private CellInfo[] sortMoves(Coords[] moves) {
        CellInfo[] infos = new CellInfo[moves.length];
    
        for (int i = 0; i < moves.length; i++) {
            CellInfo ci = new CellInfo();
            ci = boardAnalyser.getCellInfoAt(moves[i].getId());
            ci.pos = moves[i];
            infos[i] = ci;
        }
    
        Arrays.sort(infos, CellInfo.SCORE_COMPARATOR);
        return infos;
    }

    // private void shuffleSameScore(CellInfo[] infos){
    //     Random rng = new Random();
    //     int start = 0;
    //     while (start < infos.length) {
    //         int end = start + 1;
    //         while (end < infos.length && infos[end].score.getScore() == infos[start].score.getScore()) {
    //             end++;
    //         }
    //         for (int i = end - 1; i > start; i--) {
    //             int j = start + rng.nextInt(i - start + 1);
    //             CellInfo tmp = infos[i];
    //             infos[i] = infos[j];
    //             infos[j] = tmp;
    //         }
    //         start = end;
    //     }
    // }

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
    public int search(int depth, int alpha, int beta, int color){
        iterationPerDepth[MAX_DEPTH - depth]++;
        if (depth == 1 || timeLimitExceeded() || board.getWinner() != 0){
            return color * evaluate(depth);
        }

        // Coords[] moves = getPossibleMove();
        sortTimeStart = System.currentTimeMillis();
        // CellInfo[] sortedMoves = sortMoves(moves);
        int[] sortedIndices = boardAnalyser.getSortedIndices();
        sortTimer += System.currentTimeMillis() - sortTimeStart;

        // float player1score = 0;
        // float player2score = 0;
        // for (CellInfo move : sortedMoves){
        //     player1score += move.score.getPlayerScore(1);
        //     player2score += move.score.getPlayerScore(2);
        // }
        
        // int maxMove = 0;
        int value = Integer.MIN_VALUE;
        for (int index : sortedIndices){
            // if (maxMove >= 3 && move.score.getScore() < 10f) break;
            // if (maxMove >= 5 && move.score.getScore() < 25f) break;
            // if (maxMove == 7) break;
            // Coords pos = move.pos;
            
            boardActionTimeStart = System.currentTimeMillis();
            board.placePieceAt(index);
            boardActionTimer += System.currentTimeMillis() - boardActionTimeStart;

            // player1PositionScore = player1score;
            // player2PositionScore = player2score;
            value = Math.max(value, -search(depth - 1, -beta, -alpha, -color));
            alpha = Math.max(alpha, value);

            boardActionTimeStart = System.currentTimeMillis();
            board.undo();
            boardActionTimer += System.currentTimeMillis() - boardActionTimeStart;

            if (alpha >= beta){
                prunningCount++;
                prunningPerDepth[MAX_DEPTH - depth]++;
                break;
            }
            // maxMove++;
        }
        return value;
    }

    // public Coords[] getPossibleMove(){
    //     return board.neighbourCellIndexSet.toArray(new Coords[0]);
    // }
}
