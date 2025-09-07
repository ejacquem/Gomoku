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
    private int currentPlayer = GameSettings.FIRST_PLAYER;

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
        new Coords(1,  -1),   // NE
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
    
    public int getPlayer1CapturesCount(){ 
        return (moveCount + GameSettings.isPlayer2First()) / 2 - player2PiecesCount;
    }

    public int getPlayer2CapturesCount(){ 
        return (moveCount + GameSettings.isPlayer1First()) / 2 - player1PiecesCount;
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
        resetWinner();
        switchPlayer();
    }

    private void switchPlayer(){
        currentPlayer = currentPlayer == 1 ? 2 : 1;
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
        moves.clear();
        resetWinner();
        neighbourCellIndexSet.clear();
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

    public void analyse() {
        // resetCapture();
        // resetFreeThree();
        // resetWinner();
        // famAllFreeThreeForPlayer(currentPlayer);
        // famAllCaptureForPlayer(currentPlayer);
        // famWinner();
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

    private void famWinner(){
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                famWinnerAt(new Coords(x, y));
            }
        }
    }

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

    public int getCellScoreAt(Coords pos){
        float score;
        score = 0;
        if (getCellAt(pos).has_piece())
            return 0;
        for (Coords dir : DIRECTION4){
            score += calculateCellScoreAtDir(pos, dir);
        }
        return (int)score;
    }

    public float calculateCellScoreAtDir(Coords pos, Coords dir){
        float score = 0;
        Cell right = getCellAtDir(pos, dir, 1);
        Cell left = getCellAtDir(pos, dir, -1);
        float scoreRight = pieceSequenceScoreInDir(pos.add(dir), dir);
        float scoreLeft = pieceSequenceScoreInDir(pos.add(dir.negate()), dir.negate());
        if (right.has_piece() && right.player == left.player){
            return (float)Math.pow(scoreRight + scoreLeft, 2);
        }
        score += Math.pow(scoreRight, 2);
        score += Math.pow(scoreLeft, 2);
        return score;
    }

    // piece sequence score is the number of the same player of pieces in a direction, + 0.5 if tail is empty
    public float pieceSequenceScoreInDir(Coords pos, Coords dir){
        int count = 1;
        if (!getCellAt(pos, -1).has_piece())
            return 0;
        int player = getCellAt(pos, -1).player;
        Cell next = getCellAtDir(pos, dir, 1);
        while (player == next.player){
            count++;
            next = getCellAtDir(pos, dir, count);
        }
        return (float)count + (next.empty() ? 0.5f : 0);
    }

}
