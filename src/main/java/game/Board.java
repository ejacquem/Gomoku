package main.java.game;

import main.java.GameSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.BiPredicate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Board {
    
    public final int BOARD_SIZE = GameSettings.BOARD_SIZE;
    public final int WINNING_PIECES = 5;
    private Cell[][] board;
    private Stack<Move> moves;
    private IntegerProperty moveCount = new SimpleIntegerProperty(0);
    private int player1PiecesCount = 0;
    private int player2PiecesCount = 0;

    public IntegerProperty moveCountProperty() { return moveCount; }

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

    Board(){
        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x] = new Cell();
            }
        }

        moves = new Stack<>();
    }

    public int getPlayer1PiecesCount(){ return player1PiecesCount; }
    public int getPlayer2PiecesCount(){ return player2PiecesCount; }
    
    public int getPlayer1CapturesCount(){ 
        return (moveCount.get() + GameSettings.isPlayer2First()) / 2 - player2PiecesCount;
    }

    public int getPlayer2CapturesCount(){ 
        return (moveCount.get() + GameSettings.isPlayer1First()) / 2 - player1PiecesCount;
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

    public void undoLastMove(){
        moveCount.set(moveCount.get() - 1);
        Move m = moves.pop();
        removePiece(m.coords);
        int opponent = getOpponent(m.player);
        for (Coords pos : m.capturesCoords){
            addPiece(pos, opponent);
        }
    }

    public void placePiece(Coords pos, int player){
        moveCount.set(moveCount.get() + 1);
        addPiece(pos, player);
        List<Coords> captures = capturePieces(pos, player);
        moves.add(new Move(player, pos, captures));
    }

    private void addPiece(Coords pos, int player){
        countPieces(1, player);
        board[pos.y][pos.x].player = player;
    }

    private void removePiece(Coords pos){
        countPieces(-1, board[pos.y][pos.x].player);
        board[pos.y][pos.x].player = 0;
    }

    private void countPieces(int amount, int player){
        if (player == 1)
            player1PiecesCount += amount;
        else if (player == 2)
            player2PiecesCount += amount;
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

    private int getOpponent(int player){
        return player == 1 ? 2 : 1;
    }

    public void reset()
    {
        moveCount.set(0);
        player1PiecesCount = 0;
        player2PiecesCount = 0;
        moves.clear();
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].reset();
            }
        }
    }

    public void random(float density)
    {
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].reset();
                if (Math.random() < density)
                    board[y][x].player = (int)(Math.random() * 2) + 1;
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
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                board[y][x].winning = false;
            }
        }
    }

    public void analyse(int player) {
        resetCapture();
        resetFreeThree();
        resetWinner();
        famAllFreeThreeForPlayer(player);
        famAllCaptureForPlayer(player);
        famWinner();
    }

    // CHECKER fam = Find And Mark

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

}
