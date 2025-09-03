// src/BoardGame.java

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.util.Duration;

public class BoardGame {
    private final int BOARD_SIZE = GameSettings.BOARD_SIZE;
    private final int FIRST_PLAYER = GameSettings.FIRST_PLAYER;
    private final int WINNING_PIECES = 5;
    private Cell[][] board;
    private final int[][] DIRECTION = {
        {-1,  0}, // N
        {-1,  1}, // NE
        { 0,  1}, // E
        { 1,  1}, // SE
        { 1,  0}, // S
        { 1, -1}, // SW
        { 0, -1}, // W
        {-1, -1}  // NW
    };

    public enum GameState {
        NOT_STARTED,
        STARTED,
        GAME_OVER
    }
    private GameState gameState = GameState.NOT_STARTED;

    private final IntegerProperty currentPlayer = new SimpleIntegerProperty(FIRST_PLAYER);  // player = 1 or player = 2
    private final IntegerProperty winner = new SimpleIntegerProperty(0);
    private final IntegerProperty player1Points = new SimpleIntegerProperty(0);
    private final IntegerProperty player2Points = new SimpleIntegerProperty(0);
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
    public IntegerProperty player1Property() {
        return player1Points;
    }
    public IntegerProperty player2Property() {
        return player2Points;
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

        gameState = GameState.NOT_STARTED;
        timeline1 = createTimeline(player1Timer);
        timeline2 = createTimeline(player2Timer);
    }

    public void startGame(){
        reset();
        System.out.println("Game started");
        gameState = GameState.STARTED;

        // startTime = System.currentTimeMillis();
        if (currentPlayer.get() == 1)
            timeline1.play();
        else
            timeline2.play();
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

    public void placePiece(int row, int col) {
        // Example: toggle between 0 and 1
        if (gameState == GameState.NOT_STARTED){
            System.out.println("Game Not Started");
            return;
        }
        if (gameState == GameState.GAME_OVER) return;
        if (!isInBound(row, col)) return;

        Cell cell = getCell(row, col);

        if (cell.has_piece()) return;
        if (cell.isDoubleFreeThree()) return;
        System.out.println("Player " + getCurrentPlayer() + " placing piece at: " + row + ", " + col);
        board[row][col].player = getCurrentPlayer(); // place piece
        checkCaptureSequence(row, col, getCurrentPlayer());
        switchPlayer();
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
        initBoard();
        gameState = GameState.NOT_STARTED;
        currentPlayer.set(FIRST_PLAYER);
        winner.set(0);
        player1Timer.set(GameSettings.START_TIMER);
        player2Timer.set(GameSettings.START_TIMER);
        timeline1.pause();
        timeline2.pause();
    }

    public void initBoard()
    {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c].reset();
            }
        }
    }

    private void checkWinner(){
        // System.out.println("Checking Winner");
        if (player1Points.get() >= GameSettings.WINNING_CAPTURED_PIECES){
            winner.set(1);
            return;
        }
        if (player2Points.get() >= GameSettings.WINNING_CAPTURED_PIECES){
            winner.set(2);
            return;
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (!board[r][c].has_piece())
                    continue;
                if (checkWinnerDirection(r, c, 0, 1) ||
                    checkWinnerDirection(r, c, 1, 0) ||
                    checkWinnerDirection(r, c, 1, 1) ||
                    checkWinnerDirection(r, c, -1, 1)){
                    winner.set(board[r][c].player);
                    System.out.println("Winner is " + winner.get());
                    return;
                }
            }
        }
        System.out.println("No Winner Found");
    }

    private boolean checkWinnerDirection(int r, int c, int deltaC, int deltaR){
        int winningPieces = loopOverBoard(r, c, deltaC, deltaR);
        if (winningPieces < WINNING_PIECES)
            return false;
        markWinningPieces(r, c, deltaC, deltaR, winningPieces);
        return true;
    }

    private int loopOverBoard(int r, int c, int deltaR, int deltaC){
        int i = 1;
        while (get_piece_at(r, c) == get_piece_at(r + i * deltaR, c + i * deltaC))
            i++;
        return i;
    }

    private void markWinningPieces(int r, int c, int deltaR, int deltaC, int number){
        for (int i = 0; i < number; i++)
            board[r + i * deltaR][c + i * deltaC].winning = true;
    }

    // return defaultValue if out of bound
    private int get_piece_at(int row, int col, int defaultValue){
        if (!isInBound(row, col))
            return defaultValue;
        return board[row][col].player;
    }

    // return 0 if out of bound
    private int get_piece_at(int row, int col){
        return get_piece_at(row, col, 0);
    }

    private boolean isInBound(int row, int col){
        return row >= 0 && col >= 0 && row < BOARD_SIZE && col < BOARD_SIZE;
    }

    private void checkFreeThree(){
        resetFreeThree();
        int p = currentPlayer.get();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                // checkFreeThreeHorizontal(r, c, currentPlayer.get());
                if (checkFreeSequence(r, c, 0, 1, p)) board[r][c].can_be_free3_h = true;
                if (checkFreeSequence(r, c, 1, 0, p)) board[r][c].can_be_free3_v = true;
                if (checkFreeSequence(r, c, 1, 1, p)) board[r][c].can_be_free3_p = true;
                if (checkFreeSequence(r, c, -1, 1, p)) board[r][c].can_be_free3_n = true;
            }
        }
    }

    // 0 1 2 3 4 5
    // the three case of free three
    // 0 1 0 1 1 0
    // 0 1 1 0 1 0
    // 0 1 1 1 0 
    // x is creating a free three if
    // 0 x 0 1 1 0
    // 0 x 1 0 1 0
    // 0 x 1 1 0 
    private boolean checkFreeSequence(int r, int c, int deltaR, int deltaC, int player){
        int x = 0;
        int p = player;
        int[][] patterns = {
            {6, 1, 0, x, 0, p, p, 0},
            {6, 1, 0, x, p, 0, p, 0},
            {5, 1, 0, x, p, p, 0},
            // reverse
            {6, 4, 0, p, p, 0, x, 0},
            {6, 4, 0, p, 0, p, x, 0},
            {5, 3, 0, p, p, x, 0},
        };
        for (int[] pattern : patterns){
            int len = pattern[0];
            int offset = pattern[1];
            int i;
            for (i = 0; i < len; i++){
                if (get_piece_at(r + (i - offset) * deltaR, c + (i - offset) * deltaC, -1) != pattern[i + 2])
                    break;
            }
            if (i == len)
                return true;
            // if (checkSequenceMatch(r, c, len, offset, ))
            //     return true;
        }
        return false;
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

    private boolean checkSequenceMatch(int r, int c, int len, int offset, int[] pattern, int[] dir){
        for (int i = 0; i < len; i++){
            int cell = get_piece_at(r + (i - offset) * dir[0], c + (i + offset) * dir[1], -1);
            if (pattern[i] != cell)
                return false;
        }
        return true;
    }

    private void capture(int r, int c, int[] dir){
        board[r + 1 * dir[0]][c + 1 * dir[1]].reset();
        board[r + 2 * dir[0]][c + 2 * dir[1]].reset();
        if (currentPlayer.get() == 1)
            player1Points.set(player1Points.get() + 2);
        else
            player2Points.set(player2Points.get() + 2);
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
