package main.java.game;

import java.util.ArrayList;
import java.util.List;

import main.java.GameSettings;

public class BoardAnalyser {
    private Board board;
    // public int[] scoreGrid = new int[Board.BOARD_MAX_INDEX];
    public static final int SCOREGRID_LENGTH = Board.BOARD_MAX_INDEX;
    public static final int MAX_HISTORY_LEN = 500;
    public int[][] scoreGridHistory = new int[MAX_HISTORY_LEN][SCOREGRID_LENGTH];

    public BoardAnalyser(Board board){
        this.board = board;
    }

    public int[] getCurrentScoreGrid(){
        return scoreGridHistory[board.getMoveCount()];
    }

    // go through every cell and calculate a rough score
    public void scanBoard() {
        int[] scoregrid = getCurrentScoreGrid();
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++){
            if (board.getPieceAt(i) != -1){
                scoregrid[i] = getScoreAt(i);
            }
        }
    }

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
    private SequenceData data = new SequenceData();
    public void scanLastMove(){
        if (board.getMoveCount() <= 0)
            return;
        // GomokuUtils.debugCaller();
        int[] scoregrid = getCurrentScoreGrid();
        copyLastHistory();
        int[] lastMove = board.getLastMove();
        for (int i = 0; i < lastMove[0]; i++){
            int move = lastMove[i + 1]; // +1 to skip first elem which is the length
            if (move < 0){ // don't need to recalculate the captured piece score
                move *= -1;
            }
            else { // assign score of 0 at the placed piece index
                scoregrid[move] = 0;
            }
            int index = move;
            for (int dir : Board.DIRECTION8) {
                pieceSequenceDataInDir(index + dir, dir, data);
                if (data.trailSpaceNumber > 0){
                    int spaceIndex = index + dir + dir * data.pieceNumber;
                    scoregrid[spaceIndex] = getScoreAt(spaceIndex);
                }
            }
        }
    }

    private void copyLastHistory(){
        if (board.getMoveCount() == 0)
            return;
        System.arraycopy(scoreGridHistory[board.getMoveCount() - 1], 0,
                 scoreGridHistory[board.getMoveCount()], 0,
                 Board.BOARD_MAX_INDEX);
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

    private float calculateScore(int pieceNumber, int trailSpaceNumber){
        if (pieceNumber == 0) return 0;
        if (trailSpaceNumber == 0){
            if (pieceNumber >= 4) return 10000 - (board.getMoveCount() * 10);
            else return 0;
        }
        else if (trailSpaceNumber == 1){
            if (pieceNumber == 1) return 1;
            else if (pieceNumber == 2) return 10;
            else if (pieceNumber == 3) return 100;
            else if (pieceNumber >= 4) return 10000 - (board.getMoveCount() * 10);
        }
        else if (trailSpaceNumber == 2){
            if (pieceNumber == 1) return 2;
            else if (pieceNumber == 2) return 20;
            else if (pieceNumber == 3) return 200;
            else if (pieceNumber >= 4) return 10000 - (board.getMoveCount() * 10);
        }
        return 0;
    }

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

    private CellInfo _info = new CellInfo();
    public int getScoreAt(int index){
        int piece = board.getPieceAt(index);
        if (piece == 1 || piece == 2)
            return 0;
        _info.resetScore();
        test++;
        for (int dir : Board.DIRECTION4){
            computeSequenceScore(index, dir, _info);
        }
        return (int)_info.getScore();
    }

    // sequence data stores info on the sequence
    // x 1 1 0 0 0 2 -> player = 1, pieceNumber = 2, trailSpaceNumber = 3, trailPiece = 2
    public class SequenceData{
        public int player = 0;
        public int pieceNumber = 0;
        public int trailSpaceNumber = 0;
        public int trailPiece = 0;

        public SequenceData(int player, int pieceNumber, int trailSpaceNumber, int trailPiece){
            this.player = player;
            this.pieceNumber = pieceNumber;
            this.trailSpaceNumber = trailSpaceNumber;
            this.trailPiece = trailPiece;
        }
        public SequenceData(){
        }
        public void reset(){
            this.player = 0;
            this.pieceNumber = 0;
            this.trailSpaceNumber = 0;
            this.trailPiece = 0;
        }
    }

    // store variable in class to avoid new allocation
    private SequenceData sequenceScoreRight = new SequenceData();
    private SequenceData sequenceScoreLeft = new SequenceData();

    private void computeSequenceScore(int pos, int dir, CellInfo total){
        SequenceData right = sequenceScoreRight;
        SequenceData left = sequenceScoreLeft;

        pieceSequenceDataInDir(pos + dir, dir, right);
        pieceSequenceDataInDir(pos - dir, -dir, left);
        
        if (right.pieceNumber == 2 && ((right.player == 1 && right.trailPiece == 2) || (right.player == 2 && right.trailPiece == 1))){
            total.addScore(100, right.player);
        }
        if (left.pieceNumber == 2 && ((left.player == 1 && left.trailPiece == 2) || (left.player == 2 && left.trailPiece == 1))){
            total.addScore(100, left.player);
        }
        // case ... p x p ...
        if ((right.player == 1 || right.player == 2) &&right.player == left.player){
            int trailSpaceNumber = (right.trailSpaceNumber > 0 ? 1 : 0) + (left.trailSpaceNumber > 0 ? 1 : 0);
            float score = calculateScore(right.pieceNumber + left.pieceNumber, trailSpaceNumber);
            total.addScore(score, right.player);
            return ;
        }
        // case ... x p ...
        if ((right.player == 1 || right.player == 2)){
            total.addScore(calculateScore(right.pieceNumber, 1 + isSpace(right.trailPiece)), right.player);
        }
        if ((left.player == 1 || left.player == 2)){
            total.addScore(calculateScore(left.pieceNumber, 1 + isSpace(left.trailPiece)), left.player);
        }
    }

    private int isSpace(int piece){
        return (piece == 0 ? 1 : 0);
    }

    public void pieceSequenceDataInDir(int pos, int dir, SequenceData data){
        data.reset();
        int currPiece = board.getPieceAt(pos);
        int next = currPiece;
        int count = 0;
        data.player = currPiece;
        if (currPiece == 1 || currPiece == 2){
            int player = currPiece;
            while (next == player){
                count++;
                next = board.getPieceAt(pos + dir * count);
            }
            data.pieceNumber = count;
        }
        while (next == 0){
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
