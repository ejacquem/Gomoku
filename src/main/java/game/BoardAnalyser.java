package main.java.game;

import java.util.ArrayList;
import java.util.List;

import main.java.app.GameSettings;
import main.java.utils.ScoreBuckets;
/*
 * BoardAnalyser class
 * 
 * This class is responsible for analysing the board and assigning a rough score to a cell
 * For performance, it will only compute the score (scan) of pieces that have been impacted by the last move
 * each impacted is scanned and the number of consecutive pieces is stored in an array of int scoreGrid
 * each cell has 9 attributes, 1 for each direction (8), 1 for the final score
 * With this system, each Cell knows how many consecutive cells there are in any direction.
 * The number can go from 0 to 5, and if there is a capture in that square, the number is 9
 * The score is then computed based on the number of cell there are left and right.
 * 
 * The score is precomputed in a lookup table for performance.
 */
public class BoardAnalyser implements BoardListener {
    public final Board board;
    // public int[] scoreGrid = new int[Board.BOARD_MAX_INDEX];
    private static final int CELL_INFO_SIZE = 8 + 1 + 2;
    private static final int CELL_INFO_SCORE_INDEX = 8;
    private static final int CELL_INFO_PLAYER1_SCORE_INDEX = 8 + 1;
    private static final int CELL_INFO_PLAYER2_SCORE_INDEX = 8 + 2;
    private static final int SCOREGRID_LENGTH = Board.BOARD_MAX_INDEX * CELL_INFO_SIZE;
    private static final int MAX_HISTORY_LEN = 500;
    private int[][] scoreGridHistory = new int[MAX_HISTORY_LEN][SCOREGRID_LENGTH];
    private int[][] playerScoreHistory = new int[MAX_HISTORY_LEN][2];
    private int[] pieceBoard; // to avoid board.getPieceAt() call
    private int moveCount = 0;
    private int maxHistory = 0;

    public static final int MAX_SCORE = 100;
    public final ScoreBuckets scoreBuckets;

    public BoardAnalyser(Board board, ScoreBuckets scoreBuckets) {
        this.board = board;
        this.board.setListener(this);
        this.pieceBoard = board.getBoard();
        this.scoreBuckets = scoreBuckets;
    }

    public BoardAnalyser(Board board) {
        this.board = board;
        this.board.setListener(this);
        this.pieceBoard = board.getBoard();
        this.scoreBuckets = new ScoreBuckets(MAX_SCORE);
    }

    @Override
    public void onMovePlaced() {
        updateMoveCount();
        scoreBuckets.startMove();
        scanLastMove();
        maxHistory = moveCount;
        // scoreBuckets.printCurrentBuckets();
    }

    @Override
    public void onUndo() {
        updateMoveCount();
        scoreBuckets.undoMove();
    }

    @Override
    public void onRedo() {
        updateMoveCount();
        scoreBuckets.redoMove();
    }

    @Override
    public void onGoto() {
        updateMoveCount();
    }

    @Override
    public void onReset() {
        reset();
        updateMoveCount();
        scoreBuckets.reset();
    }

    public BoardAnalyser deepCopy() {
        Board boardCopy = this.board.deepCopy();
    
        BoardAnalyser copy = new BoardAnalyser(boardCopy, scoreBuckets.deepCopy());
    
        copy.scoreGridHistory = new int[MAX_HISTORY_LEN][SCOREGRID_LENGTH];
        for (int i = 0; i <= maxHistory; i++) {
            // copy.scoreGridHistory[i] = this.scoreGridHistory[i].clone();
            System.arraycopy(this.scoreGridHistory[i], 0, copy.scoreGridHistory[i], 0, SCOREGRID_LENGTH);
        }

        copy.moveCount = this.moveCount;
        copy.pieceBoard = boardCopy.getBoard();
        copy.maxHistory = this.maxHistory;

        return copy;
    }

    private int[] getCurrentScoreGrid() {
        return scoreGridHistory[moveCount];
    }

    public double getPlayer1Score() {
        return ((double) playerScoreHistory[moveCount][0]) / 8.;
    }

    public double getPlayer2Score() {
        return ((double) playerScoreHistory[moveCount][1]) / 8.;
    }

    private static final int SCORE_OFFSET = 9;
    private static final int[][] SCORE_LOOK_TABLE = new int[19][19]; // -9 to 9

    static {
        for (int left = -9; left <= 9; left++){
            for (int right = -9; right <= 9; right++){
                SCORE_LOOK_TABLE[SCORE_OFFSET + left][SCORE_OFFSET + right] = computeScoreFromPieceNumber(left, right);
            }
        }
    }

    private void computeScoreAtPos(int index) {
        int[] grid = getCurrentScoreGrid();
        int score = 0;
        int p1Score = 0;
        int p2Score = 0;
        int cellInfoIndex = index * CELL_INFO_SIZE;
        for (int i = 0; i < 4; i++) {
            int left = grid[cellInfoIndex + i];
            int right = grid[cellInfoIndex + (7 - i)];
            // score += computeScoreFromPieceNumber(left, right);
            score += SCORE_LOOK_TABLE[SCORE_OFFSET + left][SCORE_OFFSET + right];
            if (Math.abs(left) == 9) left = -left;
            if (Math.abs(right) == 9) right = -right;
            if (left > 0) { p1Score += left * left; }
            else { p2Score += left * left; }
            if (right > 0) { p1Score += right * right; }
            else { p2Score += right * right; }
            // if (Math.abs(left) != 9 && Math.abs(right) != 9 && !isEnoughSpace(index, (Integer.signum(left) == 1 ? 1 : 2), Board.DIRECTION4[i])){
            //     score = 0;
            // }
        }

        playerScoreHistory[moveCount][0] += p1Score - scoreGridHistory[moveCount][index * CELL_INFO_SIZE + CELL_INFO_PLAYER1_SCORE_INDEX];
        playerScoreHistory[moveCount][1] += p2Score - scoreGridHistory[moveCount][index * CELL_INFO_SIZE + CELL_INFO_PLAYER2_SCORE_INDEX];
        scoreGridHistory[moveCount][index * CELL_INFO_SIZE + CELL_INFO_PLAYER1_SCORE_INDEX] = p1Score;
        scoreGridHistory[moveCount][index * CELL_INFO_SIZE + CELL_INFO_PLAYER2_SCORE_INDEX] = p2Score;
        setScoreAtPos(index, score);
    }

    private static int computeScoreFromPieceNumber(int left, int right){
        int score = 0;
        if (left != 0 && Integer.signum(left) == Integer.signum(right)) {
            // if capture or enough space add score
            score += getScoreFromDoublePieceNumber(Math.abs(left), Math.abs(right));
       }
       else{
            score += getScoreFromPieceNumber(Math.abs(left));
            score += getScoreFromPieceNumber(Math.abs(right));
       }
       return score;
    }

    private static int getScoreFromPieceNumber(int pieceNumber) {
        switch (pieceNumber) {
            case 1: return 1;
            case 2: return 3;
            case 3: return 20;
            case 4: return 100;
            case 9: return 50; // capture
        }
        return 0;
    }

    private static int getScoreFromDoublePieceNumber(int leftPieceNumber, int rightPieceNumber) {
        switch (leftPieceNumber + rightPieceNumber) {
            case 0: return 0;
            case 1: return 1;
            case 2: return 5;
            case 3: return 30;
            case 4: return 100;
            case 5: return 100;
            case 6: return 100;
            case 7: return 100;
            case 8: return 100;
            case 9: return 75; // capture
            case 10: return 90;
            default: return 100; // capture and more
        }
    }

    public boolean isEnoughSpace(int index, int player, int dir){
        // System.out.println("isEnoughSpace for " + player + " at index: " + index + " in dir " + dir);
        int count = 1;
        int left = index + dir;
        int curr = pieceBoard[left];
        while ((curr == 0 || curr == player) && count < 5){
            left += dir;
            curr = pieceBoard[left];
            count++;
        }
        int right = index - dir;
        curr = pieceBoard[right];
        while ((curr == 0 || curr == player) && count < 5){
            right -= dir;
            curr = pieceBoard[right];
            count++;
        }
        return count == 5;
    }

    public int getScoreAtPosAtDir(int index, int dirIndex) {
        // if (dirIndex < 0 || dirIndex >= 8) throw new IllegalStateException("Invalid dirIndex " + index); // comment later for efficiency 
        return scoreGridHistory[board.getMoveCount()][index * CELL_INFO_SIZE + dirIndex];
    }

    private void setScoreAtPosAtDir(int posIndex, int flagIndex, int score) {
        scoreGridHistory[moveCount][posIndex * CELL_INFO_SIZE + flagIndex] = score;
    }

    private void setScoreAtPos(int posIndex, int score) {
        // System.out.printf("scanLastMove setScoreAtPosAtDir posIndex: %d, dirInded: %d, score: %d\n", posIndex, flagIndex, score);
        if (score > MAX_SCORE) score = MAX_SCORE;
        scoreBuckets.update(posIndex, getScoreAtPos(posIndex), score);
        scoreGridHistory[moveCount][posIndex * CELL_INFO_SIZE + CELL_INFO_SCORE_INDEX] = score;
    }

    public int getScoreAtPos(int index) {
        return scoreGridHistory[moveCount][index * CELL_INFO_SIZE + CELL_INFO_SCORE_INDEX];
    }

    // go through every cell and calculate a rough score
    // public void scanBoard() {
    //     int[] scoregrid = getCurrentScoreGrid();
    //     for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
    //         if (board.getPieceAt(i) != -1) {
    //             scoregrid[i] = getScoreAt(i);
    //         }
    //     }
    // }

    public static class PosScore {
        public final int index;
        public final int score;
        public PosScore(int index, int score) {
            this.index = index;
            this.score = score;
        }
    }

    public List<PosScore> getSortedPositions() {
        updateMoveCount();

        if (moveCount == 0){
            return List.of(new PosScore(Board.BOARD_MAX_INDEX / 2, 0));
        }

        List<PosScore>filteredCell = new ArrayList<>();
    
        // Build list of PosScore with score computed once
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            int score = getScoreAtPos(i);
            if (score >= 1) {
                filteredCell.add(new PosScore(i, score));
            }
        }
    
        // Sort by precomputed score
        filteredCell.sort((a, b) -> Integer.compare(b.score, a.score));
    
        return filteredCell;
    }

    // go through the cell impacted by the last move and calculate a rough score
    // an impacted cell it the closest space between the current cell in a dir, can be blocked by a sequence of 1 type of piece
    // in the exemple below, only 7 impacted square are scanned
    // 0 scanned square, x current, 1/2 players
    // .......
    // ...O.O.
    // ..O12..
    // ..Ox1O.
    // ..1OO..
    // .2.1...
    // .......
    // private SequenceData data = new SequenceData();
    public void scanLastMove() {
        // updateMoveCount();
        if (moveCount <= 0){
            return;
        }
        int[] lastMove = board.getLastMoves();
        if (lastMove == null){
            scanBoard();
            return;
        }
        copyLastHistory();
        for (int i = 0; i < lastMove[0]; i++) {
            scanMove(lastMove[i + 1]); // +1 to skip first elem which is the length
        }
        System.out.println("player1score: " + getPlayer1Score());
        System.out.println("player2score: " + getPlayer2Score());
    }

    public void updateMoveCount() {
        this.moveCount = board.getMoveCount();
    }

    public void scanBoard() {
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            int piece = board.getPieceAt(i);
            if (piece != -1){
                scanMove(piece == 0 ? -i : i);
            }
        }
    }

    // scan the position at the current move, the move is the position where a piece a modified
    // positive if placed
    // negative if removed
    // private int[] dirCount = new int[8];
    private void scanMove(int move) {
        int index = Math.abs(move);
        int placedPieceSign = Board.getPlayerSign(pieceBoard[index]);
        for (int dirIndex = 0; dirIndex < 4; dirIndex++) {
            int dir = Board.DIRECTION4[dirIndex];
            int right = countSamePiecesInDir(index + dir, dir);
            int left = countSamePiecesInDir(index - dir, -dir);
            processScan(left, right, index, dir, dirIndex, move, placedPieceSign);
            processScan(right, left, index, -dir, 7 - dirIndex, move, placedPieceSign);
        }
        computeScoreAtPos(index);
    }

    private void processScan(int left, int right, int index, int dir, int dirIndex, int move, int placedPieceSign){
        int rightEndIndex = index + (1 + Math.abs(right)) * dir;
        int score = 0;
        if (move < 0) {
            score = checkCaptureFromRunLength(left, index, dir);
        }
        setScoreAtPosAtDir(index, dirIndex, score); // compute the score of the current cell, set to 0 if placed
        if (pieceBoard[rightEndIndex] == 0) { // if cell is empty at the end of sequence, compute the score
            score = computeCountDir(right, placedPieceSign, left);
            score = checkCaptureFromRunLength(score, rightEndIndex, dir);
            setScoreAtPosAtDir(rightEndIndex, dirIndex, score);
            computeScoreAtPos(rightEndIndex);
        }
    }

    // return 9 or -9 if there is a capture, otherwise return runLength
    private int checkCaptureFromRunLength(int runLength, int endIndex, int dir) {
        if (Math.abs(runLength) == 2) {
            // System.out.println("endIndex: " + endIndex);
            // System.out.println("dir: " + dir);
            // System.out.println("endIndex - dir * 3: " + (endIndex - dir * 3));
            // System.out.println("board.getPieceAt(endIndex - dir * 3): " + board.getPieceAt(endIndex - dir * 3));
            int potentialCapturePiece = pieceBoard[endIndex - dir * 3];
            if ((runLength == -2 && potentialCapturePiece == 1) || (runLength == 2 && potentialCapturePiece == 2)) { // can't check sign here, pcp could be a wall
                return 9 * -Board.getPlayerSign(potentialCapturePiece);
            }
        }
        return runLength;
    }

    // p1p ppp : left + 1 + right
    // p1n ppn : left + 1
    // 01p 0pp : left + 1 + right
    // 01n 0pn : left + 1
    // n1n npn : left
    // n1p npp : left
    // p2p pnp : left
    // p2n pnn : left
    // 02p 0np : left + 1
    // 02n 0nn : left + 1 + right
    // n2p nnp : left + 1
    // n2n nnn : left + 1 + right
    private static int computeCountDir(int left, int placedPieceSign, int right) {
        if (placedPieceSign == 0)
            return left;
        int score = left;
        if (left == 0 || (Integer.signum(left) == placedPieceSign)) {
            score += placedPieceSign;
            if (placedPieceSign == Integer.signum(right))
                score += right;
        }
        return score;
    }

    // return the length of the sequence of equal pieces in a direction
    // return a positive number for white, negative for black
    private int countSamePiecesInDir(int pos, int dir) {
        // int curr = board.getPieceAt(pos);
        int curr = pieceBoard[pos];
        if (curr == 0 || curr == -1)
            return 0;
        int count = 1;
        while (curr == pieceBoard[pos + dir * count] && count < 4) {
            count++;
        }
        return count * (curr == 2 ? -1 : 1);
    }

    private void copyLastHistory() {
        playerScoreHistory[moveCount][0] = playerScoreHistory[moveCount - 1][0];
        playerScoreHistory[moveCount][1] = playerScoreHistory[moveCount - 1][1];
        System.arraycopy(scoreGridHistory[moveCount - 1], 0,
                 scoreGridHistory[moveCount], 0,
                 SCOREGRID_LENGTH);
    }

    public void inDepthAnalyse() {
        
    }

    public void reset() {
        for (int i = 0; i < MAX_HISTORY_LEN; i++) {
            for (int j = 0; j < SCOREGRID_LENGTH; j++) {
                scoreGridHistory[i][j] = 0;
            }
        }
    }

    public void printHistory() {
        for (int i = 0; i < board.getMoveCount(); i++) {
            System.out.println("History " + i + ": ");
            for (int j = 0; j < SCOREGRID_LENGTH; j++) {
                System.out.print(scoreGridHistory[i][j]);
                if (j % GameSettings.BOARD_SIZE == GameSettings.BOARD_SIZE - 1)
                    System.out.println();
            }
        }
    }
}
