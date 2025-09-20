package main.java.game;

import java.util.ArrayList;
import java.util.List;

import main.java.app.GameSettings;
import main.java.utils.TimeLogger;
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
    private static final int SCOREGRID_LENGTH = Board.BOARD_MAX_INDEX * 8;
    private static final int MAX_HISTORY_LEN = 500;
    private int[][] scoreGridHistory = new int[MAX_HISTORY_LEN][SCOREGRID_LENGTH];

    public BoardAnalyser(Board board){
        this.board = board;
    }

    private int[] getCurrentScoreGrid(){
        return scoreGridHistory[board.getMoveCount()];
    }

    public int getScoreAtPos(int index){
        int[] grid = getCurrentScoreGrid();
        index *= 8;
        int score = 0;
        for (int i = 0; i < 8; i++){
            if (Math.abs(grid[index + i]) > 0)
                System.out.println("test");
            else System.out.println("0");
            score += Math.abs(grid[index + i]);
        }
        return score;
    }

    public int getScoreAtPosAtDir(int index, int dirIndex){
        // if (dirIndex < 0 || dirIndex >= 8) throw new IllegalStateException("Invalid dirIndex " + index); // comment later for efficiency 
        return getCurrentScoreGrid()[index * 8 + dirIndex];
    }

    public void setScoreAtPosAtDir(int index, int dirIndex, int score){
        // System.out.printf("scanLastMove setScoreAtPosAtDir index: %d, dirInded: %d, score: %d\n", index, dirIndex, score);
        getCurrentScoreGrid()[index * 8 + dirIndex] = score;
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
        int[] scoregrid = getCurrentScoreGrid();
        for (int i = 0; i < SCOREGRID_LENGTH; i++) {
            if (scoregrid[i] >= 1) filteredCell.add(i);
        }
        filteredCell.sort((a, b) -> Integer.compare(scoregrid[b], scoregrid[a]));
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
        TimeLogger.time("copyLastHistory", () -> copyLastHistory());
        // copyLastHistory();
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
                }
            }
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
        int curr = board.getPieceAt(pos);
        if (curr == 0 || curr == -1)
            return 0;
        int count = 1;
        while (curr == board.getPieceAt(pos + dir * count) && count < 4){
            count++;
        }
        return count * (curr == 2 ? -1 : 1);
    }

    private int getScoreFromPieceNumber(int pieceNumber){
        switch (pieceNumber) {
            case 1: return 1;
            case 2: return 10;
            case 3: return 100;
            case 4: return 1000;
        }
        return 0;
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

    // private float calculateScore(int pieceNumber, int trailSpaceNumber){
    //     if (pieceNumber == 0) return 0;
    //     if (trailSpaceNumber == 0){
    //         if (pieceNumber >= 4) return 10000 - (board.getMoveCount() * 10);
    //         else return 0;
    //     }
    //     else if (trailSpaceNumber == 1){
    //         if (pieceNumber == 1) return 1;
    //         else if (pieceNumber == 2) return 10;
    //         else if (pieceNumber == 3) return 100;
    //         else if (pieceNumber >= 4) return 10000 - (board.getMoveCount() * 10);
    //     }
    //     else if (trailSpaceNumber == 2){
    //         if (pieceNumber == 1) return 2;
    //         else if (pieceNumber == 2) return 20;
    //         else if (pieceNumber == 3) return 200;
    //         else if (pieceNumber >= 4) return 10000 - (board.getMoveCount() * 10);
    //     }
    //     return 0;
    // }

    int test = 0;

    // public void getCellInfoAt(int index, CellInfo info){
    //     int piece = board.getPieceAt(index);
    //     info.resetScore();
    //     if (piece == 1 || piece == 2)
    //         return ;
    //     // if (piece.getNeighbourNumber() == 0)
    //     //     return score;
    //     test++;
    //     for (int dir : Board.DIRECTION4){
    //         // System.out.println("dir: " + dir);
    //         computeSequenceScore(index, dir, info);
    //     }
    // }

    // public int getScoreAt(int index){
    //     int piece = board.getPieceAt(index);
    //     if (piece == 1 || piece == 2)
    //         return 0;
    //     test++;
    //     int score = 0;
    //     for (int dir : Board.DIRECTION4){
    //         score += computeSequenceScore(index, dir);
    //     }
    //     return score;
    // }

    // sequence data stores info on the sequence
    // x 1 1 0 0 0 2 -> player = 1, pieceNumber = 2, trailSpaceNumber = 3, trailPiece = 2
    public class SequenceData{
        public int leadSpaceNumber = 0;
        public int pieceNumber = 0;
        public int trailSpaceNumber = 0;
        public int player = 0;
        public int trailPiece = 0;

        public SequenceData(){
        }
        public void reset(){
            this.leadSpaceNumber = 0;
            this.pieceNumber = 0;
            this.trailSpaceNumber = 0;
            this.player = 0;
            this.trailPiece = 0;
        }

        public int getAvailableSpace(){
            return leadSpaceNumber + pieceNumber + trailSpaceNumber;
        }
    }

    // store variable in class to avoid new allocation
    private SequenceData sequenceScoreRight = new SequenceData();
    private SequenceData sequenceScoreLeft = new SequenceData();

    private int computeSequenceScore(int pos, int dir){
        SequenceData right = sequenceScoreRight;
        SequenceData left = sequenceScoreLeft;

        pieceSequenceDataInDir(pos + dir, dir, right);
        pieceSequenceDataInDir(pos - dir, -dir, left);
        
        int score = 1;
        // check if capture
        if (isSequenceCapture(right)){
            score += 100;
        }
        if (isSequenceCapture(left)){
            score += 100;
        }
        // // case ... p x p ...
        if (isPlayer(right.player) && right.player == left.player){
            if (right.getAvailableSpace() + left.getAvailableSpace() < 4){
                return score;
            }
            if (right.leadSpaceNumber == 0 && left.leadSpaceNumber == 0){
                int pieceNumber = (right.pieceNumber + left.pieceNumber);
                switch (pieceNumber) {
                    case 0: return 0;
                    case 1: return 1;
                    case 2: return 10;
                    case 3: return 100;
                    case 4: return 1000;
                }
            }
            return score + 2 * (right.pieceNumber + left.pieceNumber) - (right.leadSpaceNumber + left.leadSpaceNumber);
        }
        // // case ... x p ...
        if ((right.player == 1 || right.player == 2)){
            score += computeSingleSequenceScore(right);
        }
        if ((left.player == 1 || left.player == 2)){
            score += computeSingleSequenceScore(left);
        }
        return score;
    }

    private int computeSingleSequenceScore(SequenceData data){
        if (data.getAvailableSpace() < 4){
            return 0;
        }
        if (data.leadSpaceNumber == 0){
            switch (data.pieceNumber) {
                case 0: return 0;
                case 1: return 1;
                case 2: return 10;
                case 3: return 100;
                case 4: return 1000;
            }
        }
        return 0;
    }

    private boolean isSequenceCapture(SequenceData data){
        return data.leadSpaceNumber == 0 && 
               data.pieceNumber == 2 && 
             ((data.player + data.trailPiece == 3));
            //  ((data.player == 1 && data.trailPiece == 2) || (data.player == 2 && data.trailPiece == 1));
    }

    private int isSpace(int piece){
        return (piece == 0 ? 1 : 0);
    }

    private boolean isPlayer(int piece){
        return piece == 1 || piece == 2;
    }

    public void pieceSequenceDataInDir(int pos, int dir, SequenceData data){
        data.reset();
        int currPiece = board.getPieceAt(pos);
        if (currPiece == -1)
            return ;
        int next = currPiece;
        int count = 0;
        while (next == 0 && count < 4){
            data.leadSpaceNumber++;
            count++;
            next = board.getPieceAt(pos + dir * count);
        }
        data.player = next;
        while ((next == 1 || next == 2) && count < 4){
            data.pieceNumber++;
            count++;
            next = board.getPieceAt(pos + dir * count);
        }
        while (next == 0 && count < 4){
            data.trailSpaceNumber++;
            count++;
            next = board.getPieceAt(pos + dir * count);
        }
        data.trailPiece = next;  
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
