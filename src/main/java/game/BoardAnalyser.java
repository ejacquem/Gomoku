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
    private int[] pieceBoard;

    public BoardAnalyser(Board board){
        this.board = board;
        this.pieceBoard = board.getBoard();
    }

    private int[] getCurrentScoreGrid(){
        return scoreGridHistory[board.getMoveCount()];
    }

    public int getScoreAtPos(int index){
        return getScoreAtPosAtDir(index, 8);
    }

    private void computeScoreAtPos(int index){
        int[] grid = getCurrentScoreGrid();
        int score = 0;
        for (int i = 0; i < 8; i++){
            score += Math.abs((float)grid[index * CELL_INFO_SIZE + i] * 1.9f);
        }
        setScoreAtPosAtDir(index, 8, score);
    }

    public int getScoreAtPosAtDir(int index, int dirIndex){
        // if (dirIndex < 0 || dirIndex >= 8) throw new IllegalStateException("Invalid dirIndex " + index); // comment later for efficiency 
        return getCurrentScoreGrid()[index * CELL_INFO_SIZE + dirIndex];
    }

    public void setScoreAtPosAtDir(int posIndex, int flagIndex, int score){
        // System.out.printf("scanLastMove setScoreAtPosAtDir posIndex: %d, dirInded: %d, score: %d\n", posIndex, flagIndex, score);
        getCurrentScoreGrid()[posIndex * CELL_INFO_SIZE + flagIndex] = score;
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

    List<Integer> filteredCell = new ArrayList<>();
    public int[] getSortedIndices() {
        filteredCell.clear();
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++) {
            if (getScoreAtPos(i) >= 1) filteredCell.add(i);
        }
        // filteredCell.sort((a, b) -> Integer.compare(scoregrid[b], scoregrid[a]));
        filteredCell.sort((a, b) -> Integer.compare(getScoreAtPos(b), getScoreAtPos(a)));
        return filteredCell.stream().mapToInt(Integer::intValue).toArray();
    }

    // private Integer[] indices = new Integer[SCOREGRID_LENGTH];
    // public int[] getSortedIndices() {
    //     int[] scoregrid = getCurrentScoreGrid();
    //     for (int i = 0; i < SCOREGRID_LENGTH; i++) {
    //         indices[i] = i;
    //     }

    //     Arrays.sort(indices, (a, b) -> Integer.compare(scoregrid[b], scoregrid[a]));

    //     return Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
    // }

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
        if (board.getMoveCount() <= 0)
            return;
        copyLastHistory();
        int[] lastMove = board.getLastMove();
        for (int i = 0; i < lastMove[0]; i++){
            int move = lastMove[i + 1]; // +1 to skip first elem which is the length
            int index = Math.abs(move);
            int placedPieceSign = Board.getPlayerSign(board.getPieceAt(index));
            // System.out.println("scanLastMove: " + move);
            for (int dirIndex = 0; dirIndex < 8; dirIndex++) {
                int dir = Board.DIRECTION8[dirIndex];
                dirCount[dirIndex] = countSamePiecesInDir(index + dir, dir);
            }
            for (int dirIndex = 0; dirIndex < 8; dirIndex++) {
                int dir = Board.DIRECTION8[dirIndex];
                int right = dirCount[dirIndex];
                int left = dirCount[7 - dirIndex]; // opposite dir
                int endIndex = index + (1 + Math.abs(right)) * dir;
                setScoreAtPosAtDir(index, dirIndex, (move > 0 ? 0 : left));
                if (board.getPieceAt(endIndex) == 0){
                    int score = computeCountDir(right, placedPieceSign, left);
                    setScoreAtPosAtDir(endIndex, dirIndex, score);
                    computeScoreAtPos(endIndex);
                }
            }
            computeScoreAtPos(index);
        }
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
        if (board.getMoveCount() == 0)
            return;
        System.arraycopy(scoreGridHistory[board.getMoveCount() - 1], 0,
                 scoreGridHistory[board.getMoveCount()], 0,
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
