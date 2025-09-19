package main.java.game;
// src/BoardGame.java

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.util.Duration;
import main.java.app.GameSettings;

public class BoardGame {
    public final int BOARD_SIZE = GameSettings.BOARD_SIZE;
    public final int FIRST_PLAYER = GameSettings.FIRST_PLAYER;
    public final int WINNING_PIECES = 5;
    public Board board;
    public GomokuAI AI;
    public BoardAnalyser boardAnalyser;

    public int bestMove = -1;

    public enum GameState {
        NOT_STARTED,
        STARTED,
        GAME_OVER
    }
    private GameState gameState = GameState.NOT_STARTED;

    private final IntegerProperty currentPlayer = new SimpleIntegerProperty(FIRST_PLAYER);  // player = 1 or player = 2
    private final IntegerProperty winner = new SimpleIntegerProperty(0);
    private final IntegerProperty moveCount = new SimpleIntegerProperty(0);
    private final IntegerProperty player1CapturedPieces = new SimpleIntegerProperty(0);
    private final IntegerProperty player2CapturedPieces = new SimpleIntegerProperty(0);
    private final LongProperty player1Timer = new SimpleLongProperty(GameSettings.START_TIMER);
    private final LongProperty player2Timer = new SimpleLongProperty(GameSettings.START_TIMER);
    Timeline timeline1;
    Timeline timeline2;

    public IntegerProperty currentPlayerProperty() { return currentPlayer; }
    public IntegerProperty winnerProperty() { return winner; }
    public IntegerProperty moveCountProperty() { return moveCount; }
    public IntegerProperty player1CapturedPiecesProperty() { return player1CapturedPieces; }
    public IntegerProperty player2CapturedPiecesProperty() { return player2CapturedPieces; }
    public LongProperty player1TimerProperty() { return player1Timer; }
    public LongProperty player2TimerProperty() { return player2Timer; }

    public BoardGame(GomokuAI AI, Board board) {
        this.board = board;
        this.AI = AI;
        this.boardAnalyser = AI.boardAnalyser;
        gameState = GameState.NOT_STARTED;
        timeline1 = createTimeline(player1Timer);
        timeline2 = createTimeline(player2Timer);
    }

    public void undo(){
        System.out.println("Undo Called");
        if (board.getMoveCount() == 0)
            return;
        if (gameState == GameState.GAME_OVER){
            System.out.println("Undo Game Over");
            gameState = GameState.STARTED;
            winner.set(0);
        }
        board.undo();
        tick();
    }

    public void startGame(){    
        System.out.println("Game started");
        reset();
        gameState = GameState.STARTED;
    }

    public void setWinner(int player){
        System.out.println("Winner is " + player);
        winner.set(player);
    }

    public void setGameOver(){
        System.out.println("Game Over");
        gameState = GameState.GAME_OVER;
        timeline1.pause();
        timeline2.pause();
    }

    public boolean isWinner() {
        return winner.get() != 0;
    }

    public void checkWinner(){
        if (board.getWinner() != 0){
            setWinner(board.getWinner());
        }

        if (player1CapturedPieces.get() >= GameSettings.WINNING_CAPTURED_PIECES)
            setWinner(1);
        if (player2CapturedPieces.get() >= GameSettings.WINNING_CAPTURED_PIECES)
            setWinner(2);
    }

    public void handleInput(Coords pos){
        System.out.println("Handle Input");
        Coords realPos = pos.add(GameSettings.BOARD_WALL_WIDTH);
        if (gameState == GameState.NOT_STARTED){
            startGame();
        }
        if (gameState == GameState.GAME_OVER){
            System.out.println("Can't place piece: Game Over");
            return;
        }
        System.out.println("Trying to place a piece at pos " + pos);
        placePieceAttempt(realPos);

        if (gameState == GameState.GAME_OVER){
            return;
        }
        bestMove = AI.getBestMove();
        // if (GameSettings.aiPlaysAutomatic){
        //     if (GameSettings.player1AI && board.getCurrentPlayer() == 1){
        //         placePieceAttempt(bestMove);
        //     }
        //     else if (GameSettings.player2AI && board.getCurrentPlayer() == 2){
        //         placePieceAttempt(bestMove);
        //     }
        // }
    }

    private void placePieceAttempt(Coords pos) {
        placePieceAttempt(pos.getId());
    }

    private void placePieceAttempt(int index) {
        if (!board.isInBound(index)){
            System.out.println("Can't place piece: Out of Bound");
            return;
        }

        if (board.getPieceAt(index) != 0) return;
        // if (cell.isDoubleFreeThree()) return;
        
        System.out.println("Placing a " + ((board.getCurrentPlayer() == 1) ? "white" : "black") + " piece at index: " + index);
        board.placePieceAt(index);
        tick();
    }

    public void tick(){
        player1CapturedPieces.set(board.getCaptureCount(1));
        player2CapturedPieces.set(board.getCaptureCount(2));
        switchPlayerTo(board.getCurrentPlayer());
        checkWinner();
        if (isWinner()) {
            setGameOver();
        }
        moveCount.set(board.getMoveCount());
        boardAnalyser.scanLastMove();
    }

    private void switchPlayerTo(int player){
        if (player == 1){
            currentPlayer.set(1);
            timeline1.pause();
            timeline2.play();
        }
        else{
            currentPlayer.set(2);
            timeline1.play();
            timeline2.pause();
        }
    }

    public void reset() 
    {
        AI.reset();
        bestMove = -1;
        board.reset();
        boardAnalyser.reset();
        gameState = GameState.NOT_STARTED;
        currentPlayer.set(FIRST_PLAYER);
        winner.set(0);
        player1Timer.set(GameSettings.START_TIMER);
        player2Timer.set(GameSettings.START_TIMER);
        timeline1.pause();
        timeline2.pause();
        player1CapturedPieces.set(0);
        player2CapturedPieces.set(0);
    }

    /* BOARD ACTION */

    // private void placePiece(int row, int col){
    //     System.out.println("Player " + getCurrentPlayer() + " placing piece at: " + row + ", " + col);
    //     int p = getCurrentPlayer();
    //     addPiece(row, col, p);
    //     actions.add(new BoardAction(ActionType.ADD, row, col, p));
    // }

    public int getOpponent(int player){
        return player == 1 ? 2 : 1;
    }

    // utils

    private Timeline createTimeline(LongProperty timer){
        Timeline timeline =  new Timeline(
            new KeyFrame(Duration.millis(100), e -> {
                timer.set(timer.get() - 100);
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

} 
