// src/BoardGame.java

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class BoardGame {
    private final int BOARD_SIZE = 19;
    private final int FIRST_PLAYER = 1;
    private final int WINNING_PIECES = 5;
    private int[][] board;

    private final IntegerProperty currentPlayer = new SimpleIntegerProperty(FIRST_PLAYER);  // player = 1 or player = 2
    private final IntegerProperty winner = new SimpleIntegerProperty(0);

    public IntegerProperty currentPlayerProperty() {
        return currentPlayer;
    }
    public IntegerProperty winnerProperty() {
        return winner;
    }

    public BoardGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        // initialize board to 0
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = 0;
            }
        }
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public int getTileState(int row, int col) {
        return board[row][col];
    }

    public void placePiece(int row, int col) {
        // Example: toggle between 0 and 1
        if (row < 0 || col < 0 || row >= BOARD_SIZE || col >= BOARD_SIZE || board[row][col] != 0)
            return;
        if (winner.get() != 0)
            return;
        System.out.println("Player " + getCurrentPlayer() + " placing piece at: " + row + ", " + col);
        board[row][col] = getCurrentPlayer();
        switchPlayer();
        int win = checkWinner();
        if (win != 0) {
            winner.set(win);
            System.out.println("Winner is " + winner.get());
        }
    }

    private void switchPlayer(){
        currentPlayer.set((currentPlayer.get() == 1 ? 2 : 1));
    }

    public void reset() 
    {
        initBoard();
        currentPlayer.set(FIRST_PLAYER);
        winner.set(0);
    }

    public void initBoard()
    {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = 0;
            }
        }
    }
    
    private int checkWinner(){
        System.out.println("Checking Winner");
        for (int r = 0; r < BOARD_SIZE - WINNING_PIECES; r++) {
            for (int c = 0; c < BOARD_SIZE - WINNING_PIECES; c++) {
                int i = 1;
                if (board[r][c] == 0)
                    continue;
                while (i < WINNING_PIECES && board[r][c] == board[r][c + i])
                    i++;
                if (i >= WINNING_PIECES)
                    return board[r][c];
                i = 1;
                while (i < WINNING_PIECES && board[r][c] == board[r + i][c])
                    i++;
                if (i >= WINNING_PIECES)
                    return board[r][c];
            }
        }
        System.out.println("No Winner Found");
        return 0;
    }

    // return 1 or 2
    public int getCurrentPlayer(){
        return currentPlayer.get();
    }
} 
