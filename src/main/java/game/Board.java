package main.java.game;

import main.java.GameSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiPredicate;

public class Board {
    
    public final int BOARD_SIZE = GameSettings.BOARD_SIZE;
    public final int WINNING_PIECES = 5;
    private Cell[][] board;
    public Set<Coords> neighbourCellIndexSet;
    private Stack<Move> moves;
    private int moveCount = 0;
    private int player1PiecesCount = 0;
    private int player2PiecesCount = 0;
    private int player1CaptureCount = 0;
    private int player2CaptureCount = 0;
    private int currentPlayer = GameSettings.FIRST_PLAYER;
    private int firstPlayer = GameSettings.FIRST_PLAYER;

    private int winner = 0;

    public final Coords[] DIRECTION8 = {
        new Coords(0,  -1), // N
        new Coords(1,  -1), // NE
        new Coords( 1,  0), // E
        new Coords( 1,  1), // SE
        new Coords( 0,  1), // S
        new Coords( -1, 1), // SW
        new Coords( -1, 0), // W
        new Coords(-1, -1)  // NW
    };

    public final Coords[] DIRECTION4 = {
        new Coords( 1,  0), // E
        new Coords( 0,  1), // S
        new Coords( 1,  1), // SE
        new Coords(1,  -1)   // NE
    };

    public Board(){
        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x] = new Cell();
            }
        }

        moves = new Stack<>();
        neighbourCellIndexSet = new HashSet<>();
    }

    /* ----- GETTERS ----- */

    public int getPlayer1PiecesCount(){ return player1PiecesCount; }
    public int getPlayer2PiecesCount(){ return player2PiecesCount; }
    public int getCurrentPlayer(){ return currentPlayer; }
    public int getWinner(){ return winner; }
    public int getMoveCount(){ return moveCount; }
    
    public int isPlayer1First(){ return firstPlayer == 1 ? 1 : 0; }
    public int isPlayer2First(){ return firstPlayer == 2 ? 1 : 0; }

    // public int getPlayer1CapturesCount(){ 
    //     return ((moveCount + isPlayer2First()) / 2) - player2PiecesCount;
    // }

    // public int getPlayer2CapturesCount(){ 
    //     return ((moveCount + isPlayer1First()) / 2) - player1PiecesCount;
    // }
    public int getPlayer1CapturesCount(){ 
        return player1CaptureCount;
    }

    public int getPlayer2CapturesCount(){ 
        return player2CaptureCount;
    }

    public Cell getCellAt(Coords pos){
        if (!isInBound(pos))
            return null;
        return board[pos.y][pos.x];
    }

    public Cell getCellAt(Coords pos, int defaultValue){
        if (!isInBound(pos))
            return new Cell(defaultValue);
        return board[pos.y][pos.x];
    }

    public boolean isInBound(Coords pos){
        return pos.y >= 0 && pos.x >= 0 && pos.y < BOARD_SIZE && pos.x < BOARD_SIZE;
    }

    private int getOpponent(int player){
        return player == 1 ? 2 : 1;
    }

    private void countPieces(int amount, int player){
        if (player == 1)
            player1PiecesCount += amount;
        else if (player == 2)
            player2PiecesCount += amount;
    }

    /* ----- ACTIONS ----- */

    public void undoLastMove(){
        moveCount--;
        Move m = moves.pop();
        removePiece(m.coords);
        int opponent = getOpponent(m.player);
        for (Coords pos : m.capturesCoords){
            addPiece(pos, opponent);
        }
        if (m.player == 1) player1CaptureCount -= m.capturesCoords.size();
        if (m.player == 2) player2CaptureCount -= m.capturesCoords.size();
        if (winner != 0)
            resetWinner();
        switchPlayer();
    }

    private void switchPlayer(){
        currentPlayer = (currentPlayer == 1 ? 2 : 1);
    }

    public void placePiece(Coords pos){
        moveCount++;
        int player = currentPlayer;
        if (getCellAt(pos).has_piece()){
            throw new IllegalCallerException("REPLACING EXISTING PIECE");
        }
        addPiece(pos, player);
        List<Coords> captures = capturePieces(pos, player);
        moves.add(new Move(player, pos, captures));
        checkWinnerAt(pos);
        checkWinnerCaptures();
        switchPlayer();
    }

    private void addPiece(Coords pos, int player){
        // System.out.println("Place " + player + " at " + pos);
        countPieces(1, player);
        board[pos.y][pos.x].player = player;
        neighbourCellIndexSet.remove(pos);
        markCellNeighbours(pos, 1);
    }

    private void removePiece(Coords pos){
        Cell cell = getCellAt(pos);
        // System.out.println("remove " + cell.player + " at " + pos);
        countPieces(-1, cell.player);
        cell.player = 0;
        if (cell.isNeighbour())
            neighbourCellIndexSet.add(pos);
        markCellNeighbours(pos, 0);
    }

    private List<Coords> capturePieces(Coords pos, int player){
        List<Coords> captures = new ArrayList<>();
        int opponent = getOpponent(player);
        int[] pattern = new int[]{player, opponent, opponent, player};
        for (Coords dir : DIRECTION8){
            if (checkSequenceMatch(pos, dir, pattern, (p, cell) -> p == cell.player)){
                if (player == 1) player1CaptureCount += 2;
                if (player == 2) player2CaptureCount += 2;
                Coords[] pair = capture(pos.add(dir), pos.add(dir.multiply(2)));
                Collections.addAll(captures, pair);
            }
        }
        return captures;
    }

    private Coords[] capture(Coords pos1, Coords pos2){
        removePiece(pos1);
        removePiece(pos2);
        return new Coords[]{pos1, pos2};
    }

    private void markCellNeighbours(Coords pos, int bit){
        for (int i = 0; i < 9; i++){
            Coords relativePos = Coords.getCoordsById(i,3).subtract(1, 1); //
            Coords neighbourPos = pos.add(relativePos);
            if (!isInBound(neighbourPos)){
                continue;
            }
            Cell c = getCellAt(neighbourPos);
            c.setNeighbour(8 - i, bit);
            if (bit == 0 && !c.isNeighbour())
                neighbourCellIndexSet.remove(neighbourPos);
            else if (bit == 1 && !c.has_piece())
                neighbourCellIndexSet.add(neighbourPos);
        }
    }

    public void reset()
    {
        moveCount = 0;
        player1PiecesCount = 0;
        player2PiecesCount = 0;
        player1CaptureCount = 0;
        player2CaptureCount = 0;
        moves.clear();
        resetWinner();
        neighbourCellIndexSet.clear();
        currentPlayer = GameSettings.FIRST_PLAYER;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].reset();
            }
        }
    }

    public void random(float density)
    {
        reset();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (Math.random() < density){
                    addPiece(new Coords(x, y), (int)(Math.random() * 2) + 1);
                }
            }
        }
    }

    public void resetFreeThree(){
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].resetFreeThree();
            }
        }
    }

    public void resetCapture(){
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].can_be_captured = false;
            }
        }
    }

    public void resetWinner(){
        winner = 0;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].winning = false;
            }
        }
    }

    public void checkWinnerCaptures(){
        if (player1CaptureCount >= 10)
            winner = 1;
        if (player2CaptureCount >= 10)
            winner = 2;
    }


    // CHECKER fam = Find And Mark

    public void checkWinnerAt(Coords pos){
        Cell cell = getCellAt(pos);
        Coords p;
        for (Coords dir : DIRECTION4){
            int front = 0, back = 0;
            p = pos.add(dir);
            while (cell.player == getCellAt(p, -1).player){
                p.addTo(dir);
                front++;
            }
            p = pos.subtract(dir);
            while (cell.player == getCellAt(p, -1).player){
                p.subtractFrom(dir);
                back++;
            }
            if (1 + front + back >= GameSettings.WINNING_PIECES){
                winner = cell.player;
                for (int i = -back; i <= front; i++){
                    getCellAt(pos.add(dir.multiply(i))).winning = true;
                }
                // return;  
            }
        }
    }

    public void famAllCaptureForPlayer(int player) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                famCaptureForPlayerAt(new Coords(x, y), player);
            }
        }
    }

    public void famCaptureForPlayerAt(Coords pos, int player) {
        int opponent = getOpponent(player);
        final int[] pattern = {player, opponent, opponent, 0};
        for (Coords dir : DIRECTION8) {
            if (checkSequenceMatch(pos, dir, pattern.length, 0, pattern, (c, cell) -> c == cell.player, -1)){
                getCellAt(pos.add(dir)).can_be_captured = true;
                getCellAt(pos.add(dir.multiply(2))).can_be_captured = true;
            }
        }
    }

    public void famAllFreeThreeForPlayer(int player) {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                famFreeThreeForPlayerAt(new Coords(x, y), player);
            }
        }
    }

    public void famFreeThreeForPlayerAt(Coords pos, int player) {
        int a = 0, p = player;
        final int[][] patterns = {
            {0, a, p, p, 0},
            {0, a, 0, p, p, 0},
            {0, a, p, 0, p, 0},
        };
        for (Coords dir : DIRECTION8) {
            for (int[] pat : patterns){
                if (checkSequenceMatch(pos, dir, pat.length, -1, pat, (c, cell) -> c == cell.player, -1)){
                    getCellAt(pos).setFreeThree(dir);
                    break;
                }
            }
        }
    }

    // finding winner
    
    public void famWinnerAt(Coords pos){
        final int[][] patterns = {
            {1, 1, 1, 1, 1},
            {2, 2, 2, 2, 2}
        };
        for (Coords dir : DIRECTION4) {
            for (int[] pat : patterns){
                if (checkSequenceMatch(pos, dir, pat.length, 0, pat, (c, cell) -> c == cell.player && !cell.can_be_captured, -1)){
                    winner = pat[0];
                    for (int i = 0; i < WINNING_PIECES; i++)
                        getCellAt(pos.add(dir.multiply(i))).winning = true;
                    // return;
                }
            }
        }
    }

    // private void famWinner(){
    //     for (int y = 0; y < BOARD_SIZE; y++) {
    //         for (int x = 0; x < BOARD_SIZE; x++) {
    //             famWinnerAt(new Coords(x, y));
    //         }
    //     }
    // }

    public boolean checkSequenceMatch(Coords pos, Coords dir, int len, int offset, int[] pattern, BiPredicate<Integer, Cell> compare, int defaultValue) {
        for (int i = 0; i < len; i++) {
            Cell cell = getCellAt(pos.add(dir.multiply(i + offset)), defaultValue);
            if (!compare.test(pattern[i], cell)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean checkSequenceMatch(Coords pos, Coords dir, int[] pattern, BiPredicate<Integer, Cell> compare) {
        return checkSequenceMatch(pos, dir, pattern.length, 0, pattern, compare, -1);
    }

    public Cell getCellAtDir(Coords start, Coords dir, int distance){
        return getCellAt(start.add(dir.multiply(distance)), -1);
    }

    public class CellScore{
        private float[] playerScore = new float[]{0, 0};

        CellScore(){}

        public void setScore(float score, int player) {
            playerScore[player - 1] = score;
        }

        public void addScore(float score, int player) {
            playerScore[player - 1] += score;
        }

        public float getPlayerScore(int player) {
            return playerScore[player - 1];
        }

        public float getScore(){
            return playerScore[0] + playerScore[1];
        }

        public void addTo(CellScore other) {
            this.playerScore[0] += other.playerScore[0];
            this.playerScore[1] += other.playerScore[1];
        }
    }

    // pieceNumber: 0, trailSpaceNumber: 0 -> score 0
    // pieceNumber: 1, trailSpaceNumber: 0 -> score 0
    // pieceNumber: 2, trailSpaceNumber: 0 -> score 0
    // pieceNumber: 3, trailSpaceNumber: 0 -> score 0
    // pieceNumber: 4, trailSpaceNumber: 0 -> score 10000

    // pieceNumber: 0, trailSpaceNumber: 1 -> score 0
    // pieceNumber: 1, trailSpaceNumber: 1 -> score 1
    // pieceNumber: 2, trailSpaceNumber: 1 -> score 5
    // pieceNumber: 3, trailSpaceNumber: 1 -> score 100
    // pieceNumber: 4, trailSpaceNumber: 1 -> score 10000

    // pieceNumber: 0, trailSpaceNumber: 2 -> score 0
    // pieceNumber: 1, trailSpaceNumber: 2 -> score 2
    // pieceNumber: 2, trailSpaceNumber: 2 -> score 10
    // pieceNumber: 3, trailSpaceNumber: 2 -> score 1000
    // pieceNumber: 4, trailSpaceNumber: 2 -> score 10000

    private float calculateScore(int pieceNumber, int trailSpaceNumber){
        if (pieceNumber == 0) return 0;
        if (trailSpaceNumber == 0){
            if (pieceNumber >= 4) return 10000;
            else return 0;
        }
        else if (trailSpaceNumber == 1){
            if (pieceNumber == 1) return 1;
            else if (pieceNumber == 2) return 5;
            else if (pieceNumber == 3) return 25;
            else if (pieceNumber >= 4) return 10000;
        }
        else if (trailSpaceNumber == 2){
            if (pieceNumber == 1) return 2;
            else if (pieceNumber == 2) return 10;
            else if (pieceNumber == 3) return 200;
            else if (pieceNumber >= 4) return 10000;
        }
        return 0;
    }

    int test = 0;

    public CellScore getCellScoreAt(Coords pos){
        CellScore score = new CellScore();
        if (getCellAt(pos).has_piece())
            return score;
        if (getCellAt(pos).getNeighbourNumber() == 0)
            return score;
        test++;
        for (Coords dir : DIRECTION4){
            // System.out.println("dir: " + dir);
            sequenceScoreRight.reset();
            sequenceScoreLeft.reset();
            addCellScoreAtDir(pos, dir, score);
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

    private void computeSequenceScore(CellScore total){
        SequenceData right = sequenceScoreRight;
        SequenceData left = sequenceScoreLeft;
        // case ... p x p ...
        if ((right.player == 1 || right.player == 2) &&right.player == left.player){
            int trailSpaceNumber = (right.trailPiece == 0 ? 1 : 0) + (left.trailPiece == 0 ? 1 : 0);
            float score = calculateScore(right.pieceNumber + left.pieceNumber, trailSpaceNumber);
            total.addScore(score, right.player);
            return ;
        }
        // case ... x p ...
        if ((right.player == 1 || right.player == 2)){
            total.addScore(calculateScore(right.pieceNumber, 1 + right.trailPiece), right.player);
        }
        if ((left.player == 1 || left.player == 2)){
            total.addScore(calculateScore(left.pieceNumber, 1 + left.trailPiece), left.player);
        }
    }

    public void addCellScoreAtDir(Coords pos, Coords dir, CellScore total) {
        pieceSequenceScoreInDir(pos.add(dir), dir, sequenceScoreRight);
        dir.multiplyBy(-1); // reverse dir
        pieceSequenceScoreInDir(pos.add(dir), dir, sequenceScoreLeft);

        computeSequenceScore(total);
    }

    public void pieceSequenceScoreInDir(Coords pos, Coords dir, SequenceData data){
        Cell curr = getCellAt(pos, -1);
        Cell next = curr;
        int count = 0;
        data.player = curr.player;
        if (curr.player == 1 || curr.player == 2){
            int player = curr.player;
            while (next.player == player){
                count++;
                next = getCellAtDir(pos, dir, count);
            }
            data.pieceNumber = count;
        }
        while (next.empty()){
            data.trailSpaceNumber++;
            count++;
            next = getCellAtDir(pos, dir, count);
        }
        data.trailPiece = next.player;  
    }

}
