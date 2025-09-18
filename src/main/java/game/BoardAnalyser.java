package main.java.game;

import java.util.Arrays;

public class BoardAnalyser {
    private Board board;
    public int[] scoreGrid = new int[Board.BOARD_MAX_INDEX];

    public BoardAnalyser(Board board){
        this.board = board;
    }

    // go through every cell and calculate a rough score
    public void scanBoard() {
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++){
            scoreGrid[i] = (int)getCellInfoAt(i).getScore();
        }
    }

    private Integer[] indices = new Integer[scoreGrid.length];
    public int[] getSortedIndices() {
        for (int i = 0; i < scoreGrid.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, (a, b) -> Integer.compare(scoreGrid[b], scoreGrid[a]));

        return Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
    }

    // go through the cell impacted by the last move and calculate a rough score
    // an impacted cell it the closest space between the current cell in a dir, can be blocked by a sequence of 1 type of piece
    // in the exemple below, only 7 impacted square are scanned
    // 0 scanned square, x current, 1/2 players
    // ..O.O
    // .O12.
    // .Ox1O
    // .1OO.
    // 2.1..
    private SequenceData data = new SequenceData();
    public void scanLastMove(){
        if (board.getMoveCount() <= 0)
            return;
        int[] lastMove = board.getLastMove();
        for (int move : lastMove){
            if (move < 0){ // don't need to recalculate the captured piece score
                move *= -1;
            }
            else { // assign score of 0 at the placed piece index
                scoreGrid[move] = 0;
            }
            int index = Math.abs(move);
            for (int dir : Board.DIRECTION8) {
                pieceSequenceDataInDir(index + dir, dir, data);
                if (data.trailSpaceNumber > 0){
                    int i = index + dir + dir * data.pieceNumber;
                    scoreGrid[i] = (int)getCellInfoAt(i).getScore();
                }
            }
        }
    }

    public void inDepthAnalyse(){
        
    }

    public void reset(){
        for (int i = 0; i < Board.BOARD_MAX_INDEX; i++){
            scoreGrid[i] = 0;
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

    public CellInfo getCellInfoAt(int index){
        CellInfo score = new CellInfo();
        int piece = board.getPieceAt(index);
        if (piece == 1 || piece == 2)
            return score;
        // if (piece.getNeighbourNumber() == 0)
        //     return score;
        test++;
        for (int dir : board.DIRECTION4){
            // System.out.println("dir: " + dir);
            computeSequenceScore(index, dir, score);
        }
        return score;
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

        pieceSequenceDataInDir(pos + dir, dir, sequenceScoreRight);
        pieceSequenceDataInDir(pos - dir, -dir, sequenceScoreLeft);
        
        if (right.pieceNumber == 2 && ((right.player == 1 && right.trailPiece == 2) || (right.player == 2 && right.trailPiece == 1))){
            total.addScore(100, right.player);
        }
        if (left.pieceNumber == 2 && ((left.player == 1 && left.trailPiece == 2) || (left.player == 2 && left.trailPiece == 1))){
            total.addScore(100, left.player);
        }
        // case ... p x p ...
        if ((right.player == 1 || right.player == 2) &&right.player == left.player){
            int trailSpaceNumber = isSpace(right.trailPiece) + isSpace(left.trailPiece);
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
        int currPiece = board.getSafePieceAt(pos, -1);
        int next = currPiece;
        int count = 0;
        data.player = currPiece;
        if (currPiece == 1 || currPiece == 2){
            int player = currPiece;
            while (next == player){
                count++;
                next = board.getSafePieceAt(pos + dir * count, -1);
            }
            data.pieceNumber = count;
        }
        while (next == 0){
            data.trailSpaceNumber++;
            count++;
            next = board.getSafePieceAt(pos + dir * count, -1);
        }
        data.trailPiece = next;  
    }
}
