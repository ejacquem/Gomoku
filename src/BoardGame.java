// src/BoardGame.java

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.BiPredicate;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.util.Duration;

public class BoardGame {
    public final int BOARD_SIZE = GameSettings.BOARD_SIZE;
    public final int FIRST_PLAYER = GameSettings.FIRST_PLAYER;
    public final int WINNING_PIECES = 5;
    private Cell[][] board;
    public final int[][] DIRECTION = {
        {-1,  0}, // N
        {-1,  1}, // NE
        { 0,  1}, // E
        { 1,  1}, // SE
        { 1,  0}, // S
        { 1, -1}, // SW
        { 0, -1}, // W
        {-1, -1}  // NW
    };
    public final int[][] DIRECTION4 = {
        { 0,  1}, // E
        { 1,  0}, // S
        { 1,  1}, // SE
        {-1,  1}, // NE
    };

    public enum ActionType {
        ADD,
        REMOVE
    }

    public class BoardAction {
        int r;
        int c;
        int player;
        int link; // number of action to execute in a row
        ActionType type;

        BoardAction(ActionType type, int r, int c, int link){
            this.type = type;
            this.r = r;
            this.c = c;
            this.link = link;
        }
    }

    Stack<BoardAction> actions;

    public enum GameState {
        NOT_STARTED,
        STARTED,
        GAME_OVER
    }
    private GameState gameState = GameState.NOT_STARTED;

    private final IntegerProperty currentPlayer = new SimpleIntegerProperty(FIRST_PLAYER);  // player = 1 or player = 2
    private final IntegerProperty winner = new SimpleIntegerProperty(0);
    private final IntegerProperty player1CapturedPieces = new SimpleIntegerProperty(0);
    private final IntegerProperty player2CapturedPieces = new SimpleIntegerProperty(0);
    private final LongProperty player1Timer = new SimpleLongProperty(GameSettings.START_TIMER);
    private final LongProperty player2Timer = new SimpleLongProperty(GameSettings.START_TIMER);
    Timeline timeline1;
    Timeline timeline2;

    public IntegerProperty currentPlayerProperty() {
        return currentPlayer;
    }
    public IntegerProperty winnerProperty() {
        return winner;
    }
    public IntegerProperty player1CapturedPiecesProperty() {
        return player1CapturedPieces;
    }
    public IntegerProperty player2CapturedPiecesProperty() {
        return player2CapturedPieces;
    }
    public LongProperty player1TimerProperty() {
        return player1Timer;
    }
    public LongProperty player2TimerProperty() {
        return player2Timer;
    }

    public BoardGame() {
        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = new Cell();
            }
        }

        actions = new Stack<>();

        gameState = GameState.NOT_STARTED;
        timeline1 = createTimeline(player1Timer);
        timeline2 = createTimeline(player2Timer);
    }

    public void undo(){
        System.out.println("Move: " + actions.size());
        if (actions.size() == 0)
            return;
        for (int i = actions.peek().link; i > 0; i--) {
            reverseAction(actions.pop());           
        }
        switchPlayer();
    }

    private void reverseAction(BoardAction action){
        if (action.type == ActionType.ADD){
            removePiece(action.r, action.c);
        }
        if (action.type == ActionType.REMOVE){
            addPiece(action.r, action.c, action.player);
        }
    }

    public void startGame(){
        if (gameState == GameState.STARTED){
            System.out.println("Game already started");
            return;
        }
        System.out.println("Game started");
        reset();
        gameState = GameState.STARTED;

        // startTime = System.currentTimeMillis();
        if (currentPlayer.get() == 1)
            timeline1.play();
        else
            timeline2.play();
    }

    public void setWinner(int player){
        System.out.println("Winner is " + player);
        winner.set(player);
        gameState = GameState.GAME_OVER;
    }

    private Timeline createTimeline(LongProperty timer){
        Timeline timeline =  new Timeline(
            new KeyFrame(Duration.millis(100), e -> {
                timer.set(timer.get() - 100);
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public int getTileState(int row, int col) {
        return board[row][col].player;
    }

    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    // return defaultValue if out of bound
    private int getPieceAt(int row, int col, int defaultValue){
        if (!isInBound(row, col))
            return defaultValue;
        return board[row][col].player;
    }

    // return 0 if out of bound
    private int getPieceAt(int row, int col){
        return getPieceAt(row, col, 0);
    }

    // return 0 if out of bound
    private Cell getCellAt(int row, int col, int defaultValue){
        if (!isInBound(row, col)){
            return new Cell(defaultValue);
        }
        return board[row][col];
    }

    public void placePieceAttempt(int row, int col) {
        // Example: toggle between 0 and 1
        if (gameState == GameState.NOT_STARTED){
            startGame();
            // System.out.println("Game Not Started");
            // return;
        }
        if (gameState == GameState.GAME_OVER) return;
        if (!isInBound(row, col)) return;

        Cell cell = getCell(row, col);

        if (cell.has_piece()) return;
        if (cell.isDoubleFreeThree()) return;
        placePiece(row, col);
        checkCaptureSequence(row, col, getCurrentPlayer());
        switchPlayer();
        checkBoard();
    }

    public void checkBoard(){
        checkCanCapture();
        checkFreeThree();
        checkWinner();
    }

    private void switchPlayer(){
        if (currentPlayer.get() == 1){
            currentPlayer.set(2);
            timeline1.pause();
            timeline2.play();
        }
        else{
            currentPlayer.set(1);
            timeline1.play();
            timeline2.pause();
        }
    }

    public void reset() 
    {
        resetBoard();
        actions.clear();
        gameState = GameState.NOT_STARTED;
        currentPlayer.set(FIRST_PLAYER);
        winner.set(0);
        player1Timer.set(GameSettings.START_TIMER);
        player2Timer.set(GameSettings.START_TIMER);
        timeline1.pause();
        timeline2.pause();
    }

    public void resetBoard()
    {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c].reset();
            }
        }
    }

    public void randomBoard(float density)
    {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c].reset();
                if (Math.random() < density)
                    board[r][c].player = (int)(Math.random() * 2) + 1;
            }
        }
    }

    private void markWinningPieces(int r, int c, int[] dir, int number){
        for (int i = 0; i < number; i++)
            board[r + i * dir[0]][c + i * dir[1]].winning = true;
    }

    private boolean isInBound(int row, int col){
        return row >= 0 && col >= 0 && row < BOARD_SIZE && col < BOARD_SIZE;
    }

    /* ---------- CHECKER ---------- */

    private void checkWinByCapture(){
        if (player1CapturedPieces.get() >= GameSettings.WINNING_CAPTURED_PIECES) winner.set(1);
        if (player2CapturedPieces.get() >= GameSettings.WINNING_CAPTURED_PIECES) winner.set(2);
    }

    private void checkWinner(){
        checkWinByCapture();
        // System.out.println("Checking Winner");
        final int[] pattern1 = new int[]{1,1,1,1,1};
        final int[] pattern2 = new int[]{2,2,2,2,2};
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                for (int[] dir : DIRECTION4){
                    if (checkSequenceMatch(r, c, WINNING_PIECES, 0, pattern1, dir, (p, cell) -> p == cell.player && !cell.can_be_captured) ||
                        checkSequenceMatch(r, c, WINNING_PIECES, 0, pattern2, dir, (p, cell) -> p == cell.player && !cell.can_be_captured)) {
                    // if (checkSequenceMatch(r, c, WINNING_PIECES, 0, pattern1, dir) || checkSequenceMatch(r, c, WINNING_PIECES, 0, pattern2, dir)){
                        markWinningPieces(r, c, dir, WINNING_PIECES);
                        setWinner(board[r][c].player);
                        return;
                    }
                }
            }
        }
    }

    private void checkFreeThree(){
        resetFreeThree();
        int x = 0;
        int p = currentPlayer.get();
        int[][] patterns = {
            {0, x, 0, p, p, 0},
            {0, x, p, 0, p, 0},
            {0, x, p, p, 0},
        };
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                for (int[] pattern : patterns){
                    if (checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{0, 1}) || 
                        checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{0, -1})){
                        board[r][c].can_be_free3_h = true;
                    }
                    if (checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{1, 0}) || 
                        checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{-1, 0})){
                        board[r][c].can_be_free3_v = true;
                    }
                    if (checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{-1, 1}) || 
                        checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{1, -1})){
                        board[r][c].can_be_free3_p = true;
                    }
                    if (checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{1, 1}) || 
                        checkSequenceMatch(r, c, pattern.length, -1, pattern, new int[]{-1, -1})){
                        board[r][c].can_be_free3_n = true;
                    }
                }
            }
        }
    }

    private boolean checkCaptureSequence(int r, int c, int player){
        int opponent = getOpponent(player);
        int[] pattern = {player, opponent, opponent, player};
        for (int [] dir : DIRECTION){
            if (checkSequenceMatch(r, c, 4, 0, pattern, dir)){
                capture(r, c, dir);
                // markCapture(r, c, dir);
            }
        }
        return false;
    }

    private boolean checkCanCaptureSequence(int r, int c, int player){
        int opponent = getOpponent(player);
        int[] pattern = {player, opponent, opponent, 0};
        for (int [] dir : DIRECTION){
            if (checkSequenceMatch(r, c, 4, 0, pattern, dir)){
                markCapture(r, c, dir);
            }
        }
        return false;
    }

    private void checkCanCapture(){
        resetCapture();
        int p = currentPlayer.get();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                checkCanCaptureSequence(r, c, p);
            }
        }
    }

    public boolean checkSequenceMatch(int r, int c, int len, int offset, int[] pattern, int[] dir){
        for (int i = 0; i < len; i++){
            int cell = getPieceAt(r + (i + offset) * dir[0], c + (i + offset) * dir[1], -1);
            if (pattern[i] != cell)
                return false;
        }
        return true;
    }

    public boolean checkSequenceMatch(int r, int c, int len, int offset, int[] pattern, int[] dir, BiPredicate<Integer, Cell> compare) {
        for (int i = 0; i < len; i++) {
            Cell cell = getCellAt(r + (i + offset) * dir[0], c + (i + offset) * dir[1], -1);
            if (!compare.test(pattern[i], cell)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkSequenceMatch(int r, int c, int len, int[] pattern, int[] dir, int defaultValue){
        for (int i = 0; i < len; i++){
            int cell = getPieceAt(r + i * dir[0], c + i * dir[1], defaultValue);
            if (pattern[i] != cell)
                return false;
        }
        return true;
    }

    /* BOARD ACTION */

    private void capture(int r, int c, int[] dir){
        removePiece(r + 1 * dir[0], c + 1 * dir[1]);
        removePiece(r + 2 * dir[0], c + 2 * dir[1]);
        actions.add(new BoardAction(ActionType.REMOVE, r + 1 * dir[0], c + 1 * dir[1], 1));
        actions.add(new BoardAction(ActionType.REMOVE, r + 2 * dir[0], c + 2 * dir[1], 2));
        if (currentPlayer.get() == 1)
            player1CapturedPieces.set(player1CapturedPieces.get() + 2);
        else
            player2CapturedPieces.set(player2CapturedPieces.get() + 2);
    }

    private void placePiece(int row, int col){
        System.out.println("Player " + getCurrentPlayer() + " placing piece at: " + row + ", " + col);
        addPiece(row, col, getCurrentPlayer());
        actions.add(new BoardAction(ActionType.ADD, row, col, 1));
    }

    private void removePiece(int r, int c)
    {
        board[r][c].reset();
    }

    private void addPiece(int r, int c, int player)
    {
        board[r][c].player = player;
    }

    private void markCapture(int r, int c, int[] dir){
        board[r + 1 * dir[0]][c + 1 * dir[1]].can_be_captured = true;
        board[r + 2 * dir[0]][c + 2 * dir[1]].can_be_captured = true;
    }

    public void resetFreeThree(){
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c].resetFreeThree();
            }
        }
    }

    public void resetCapture(){
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c].can_be_captured = false;
            }
        }
    }

    // return 1 or 2
    public int getCurrentPlayer(){
        return currentPlayer.get();
    }

    public int getOpponent(int player){
        return player == 1 ? 2 : 1;
    }
} 
