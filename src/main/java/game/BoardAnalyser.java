package main.java.game;

import java.util.ArrayList;
import java.util.List;

import main.java.app.GameSettings;
/*
 * 
 * Todo :
 * rework the scanning
 * idea, place a piece and get the nearest space in all direction,
 *      if in range (4 max from current)
 *      if piece swap < 2           // if black is found, then white, the sequence is broken
 * calculate the score in the opposite direction // x110 then recompute only west
 * also compute east and multiply the result if they are the same color
 * 
 * special case for capture
 */
public class BoardAnalyser {
    private Board board;
    // public int[] scoreGrid = new int[Board.BOARD_MAX_INDEX];
    private static final int CELL_INFO_SIZE = 8 + 1;
    private static final int SCOREGRID_LENGTH = Board.BOARD_MAX_INDEX * CELL_INFO_SIZE;
    private static final int MAX_HISTORY_LEN = 500;
    private int[][] scoreGridHistory = new int[MAX_HISTORY_LEN][SCOREGRID_LENGTH];
    private int[] pieceBoard; // to avoid board.getPieceAt() call
    private int moveCount = 0;

    public BoardAnalyser(Board board){
        this.board = board;
        this.pieceBoard = board.getBoard();
    }

    private int[] getCurrentScoreGrid(){
        return scoreGridHistory[moveCount];
    }

    public int getScoreAtPos(int index){
        return scoreGridHistory[moveCount][index * CELL_INFO_SIZE + 8];
    }

    public void getPlayerScore(int[] playerScore){
        playerScore[0] = 0;
        playerScore[1] = 0;
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            if (Math.abs(getScoreAtPos(i)) != 0){
                for (int dirIndex = 0; dirIndex < 8; dirIndex++) {
                    int score = getScoreAtPosAtDir(i, dirIndex);
                    int index = score > 0 ? 0 : 1;
                    playerScore[index] += Math.abs(score);
                }
            }
        }
    }

    private void computeScoreAtPos(int index){
        int[] grid = getCurrentScoreGrid();
        int score = 0;
        for (int i = 0; i < 4; i++){
            int left = Math.abs(grid[index * CELL_INFO_SIZE + i]);
            int right = Math.abs(grid[index * CELL_INFO_SIZE + (7 - i)]);
            if (left != 0 && Integer.signum(left) == Integer.signum(right)){
                score += getScoreFromDoublePieceNumber(Math.abs(left), Math.abs(right));
            }
            else{
                score += getScoreFromPieceNumber(Math.abs(left));
                score += getScoreFromPieceNumber(Math.abs(right));
            }
        }
        if (score < 0)
            System.out.println("??????????????????");
        setScoreAtPosAtDir(index, 8, score);
    }

    private int getScoreFromPieceNumber(int pieceNumber){
        switch (pieceNumber) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 20;
            case 4: return 1000;
            case 9: return 100; // capture
        }
        return 0;
    }

    private int getScoreFromDoublePieceNumber(int leftPieceNumber, int rightPieceNumber){
        switch (leftPieceNumber + rightPieceNumber) {
            case 0: return 0;
            case 1: return 1;
            case 2: return 5;
            case 3: return 50;
            case 4: return 1000;
            case 5: return 1000;
            case 6: return 1000;
            case 7: return 1000;
            case 8: return 1000;
            default: return 150; // capture
        }
    }

    public int getScoreAtPosAtDir(int index, int dirIndex){
        // if (dirIndex < 0 || dirIndex >= 8) throw new IllegalStateException("Invalid dirIndex " + index); // comment later for efficiency 
        return scoreGridHistory[board.getMoveCount()][index * CELL_INFO_SIZE + dirIndex];
    }

    private void setScoreAtPosAtDir(int posIndex, int flagIndex, int score){
        // System.out.printf("scanLastMove setScoreAtPosAtDir posIndex: %d, dirInded: %d, score: %d\n", posIndex, flagIndex, score);
        scoreGridHistory[moveCount][posIndex * CELL_INFO_SIZE + flagIndex] = score;
    }

    // go through every cell and calculate a rough score
    // public void scanBoard() {
    //     int[] scoregrid = getCurrentScoreGrid();
    //     for (int i = 0; i < Board.BOARD_MAX_INDEX; i++){
    //         if (board.getPieceAt(i) != -1){
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

    List<Integer> filteredCell = new ArrayList<>();
    public int[] getSortedIndices() {
        updateMoveCount();
        filteredCell.clear();
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            if (getScoreAtPos(i) >= 1) filteredCell.add(i);
        }
        // filteredCell.sort((a, b) -> Integer.compare(scoregrid[b], scoregrid[a]));
        filteredCell.sort((a, b) -> Integer.compare(getScoreAtPos(b), getScoreAtPos(a)));
        return filteredCell.stream().mapToInt(Integer::intValue).toArray();
    }

    public List<PosScore> getSortedPositions() {
        updateMoveCount();
        filteredCell.clear();
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            if (getScoreAtPos(i) >= 1) filteredCell.add(i);
        }
    
        filteredCell.sort((a, b) -> Integer.compare(getScoreAtPos(b), getScoreAtPos(a)));
    
        // map to PosScore list
        return filteredCell.stream()
            .map(i -> new PosScore(i, getScoreAtPos(i)))
            .toList();  // Java 16+, else use .collect(Collectors.toList())
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
    private int[] dirCount = new int[8];
    public void scanLastMove(){
        updateMoveCount();
        if (moveCount <= 0)
            return;
        copyLastHistory();
        int[] lastMove = board.getLastMoves();
        for (int i = 0; i < lastMove[0]; i++){
            scanMove(lastMove[i + 1]); // +1 to skip first elem which is the length
        }
    }

    public void updateMoveCount(){
        this.moveCount = board.getMoveCount();
    }

    private void scanBoard(){
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            scanMove(board.getPieceAt(i) == 0 ? -i : i);
        }
    }

    // scan the position at the current move, the move is the position where a piece a modified
    // positive if placed
    // negative if removed
    private void scanMove(int move){
        // System.out.println("\nScan move: " + move);
        int index = Math.abs(move);
        int placedPieceSign = Board.getPlayerSign(pieceBoard[index]);
        // System.out.println("scanLastMove: " + move);
        for (int dirIndex = 0; dirIndex < 8; dirIndex++) {
            int dir = Board.DIRECTION8[dirIndex];
            dirCount[dirIndex] = countSamePiecesInDir(index + dir, dir);
        }
        for (int dirIndex = 0; dirIndex < 8; dirIndex++) {
            int dir = Board.DIRECTION8[dirIndex];
            int right = dirCount[dirIndex];
            int left = dirCount[7 - dirIndex]; // opposite dir
            int rightEndIndex = index + (1 + Math.abs(right)) * dir;
            int score = 0;
            int capturesign;
            if (move < 0){
                score = left;
                capturesign = checkCaptureFromRunLength(left, index, dir);
                if (capturesign != 0){
                    score = 9 * capturesign;
                }
            }
            setScoreAtPosAtDir(index, dirIndex, score); // compute the score of the current cell, set to 0 if placed
            if (pieceBoard[rightEndIndex] == 0){ // if cell is empty at the end of sequence, compute the score
                score = computeCountDir(right, placedPieceSign, left);
                capturesign = checkCaptureFromRunLength(score, rightEndIndex, dir);
                if (capturesign != 0){
                    score = 9 * capturesign;
                }
                setScoreAtPosAtDir(rightEndIndex, dirIndex, score);
                computeScoreAtPos(rightEndIndex);
            }
        }
        computeScoreAtPos(index);
    }

    private int checkCaptureFromRunLength(Integer runLength, int endIndex, int dir){
        if (Math.abs(runLength) == 2){
            // System.out.println("endIndex: " + endIndex);
            // System.out.println("dir: " + dir);
            // System.out.println("endIndex - dir * 3: " + (endIndex - dir * 3));
            // System.out.println("board.getPieceAt(endIndex - dir * 3): " + board.getPieceAt(endIndex - dir * 3));
            int potentialCapturePiece = pieceBoard[endIndex - dir * 3];
            if (runLength == -2 && potentialCapturePiece == 1 || runLength == 2 && potentialCapturePiece == 2){ // can't check sign here, pcp could be a wall
                return Board.getPlayerSign(potentialCapturePiece);
            }
        }
        return 0;
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
    private int computeCountDir(int left, int placedPieceSign, int right){
        if (placedPieceSign == 0)
            return left;
        int score = left;
        if (left == 0 || (Integer.signum(left) == placedPieceSign)){
            score += placedPieceSign;
            if (placedPieceSign == Integer.signum(right))
                score += right;
        }
        return score;
    }

    // return the length of the sequence of equal pieces in a direction
    // return a positive number for white, negative for black
    private int countSamePiecesInDir(int pos, int dir){
        // int curr = board.getPieceAt(pos);
        int curr = pieceBoard[pos];
        if (curr == 0 || curr == -1)
            return 0;
        int count = 1;
        while (curr == pieceBoard[pos + dir * count] && count < 4){
            count++;
        }
        return count * (curr == 2 ? -1 : 1);
    }

    private void copyLastHistory(){
        if (moveCount == 0)
            return;
        System.arraycopy(scoreGridHistory[moveCount - 1], 0,
                 scoreGridHistory[moveCount], 0,
                 SCOREGRID_LENGTH);
    }

    public void inDepthAnalyse(){
        
    }

    public void reset(){
        for (int i = 0; i < MAX_HISTORY_LEN; i++){
            for (int j = 0; j < SCOREGRID_LENGTH; j++){
                scoreGridHistory[i][j] = 0;
            }
        }
    }

    public void printHistory(){
        for (int i = 0; i < board.getMoveCount(); i++){
            System.out.println("History " + i + ": ");
            for (int j = 0; j < SCOREGRID_LENGTH; j++){
                System.out.print(scoreGridHistory[i][j]);
                if (j % GameSettings.BOARD_SIZE == GameSettings.BOARD_SIZE - 1)
                    System.out.println();
            }
        }
    }
}
