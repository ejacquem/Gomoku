package main.java.game;

import main.java.GameSettings;

public class Board {
    public static final int BOARD_SIZE = GameSettings.BOARD_SIZE;
    public static final int BOARD_MAX_INDEX = GameSettings.BOARD_SIZE * GameSettings.BOARD_SIZE;
    private int[] board = new int[BOARD_MAX_INDEX];
    // private Stack<Integer> history = new Stack();

    /*
     * history is a stack of int, the int is the index (position) of the piece that was placed, the int is negative if it's a capture
     * if the move are 1, 0, 2, 3 which give the position 0XX0 and creates a capture 0..0
     * the history is 1, 0, 2, 3, -1, -2
    */
    private int[] history = new int[BOARD_MAX_INDEX * 10]; // safe buffer size
    private int historyIndex = 0;
    private int maxHistoryIndex = 0;
    private int currentPlayer = GameSettings.FIRST_PLAYER;
    private int moveCount = 0;
    private int winner = 0;
    private int[] pieceCount = {0,0};

    private static final int x = 1;
    private static final int y = BOARD_SIZE;
    public static final int[] DIRECTION8 = {
        -y - x, -y, -y + x,
           - x,        + x,
        +y - x, +y, +y + x
    };
    public static final int[] DIRECTION4 = {
        -y,     // N
        -y + x, // NE
        + x,    // E
        +y + x  // SW
    };

    /* Core Action */

    private void addPieceAt(int index, int player){
        pieceCount[player - 1]++;
        board[index] = currentPlayer;
    }

    private void removePieceAt(int index){
        pieceCount[board[index] - 1]--;
        board[index] = 0;
    }

    /* Board Action */

    public void placePieceAt(int index){
        moveCount++;
        maxHistoryIndex = moveCount;
        addPieceAt(index, currentPlayer);
        addHistory(index);
        checkCapturesAt(index);
        checkWinnerAt(index);
        switchPlayer();
    }

    public void undo(){
        if (moveCount == 0){
            return ;
        }
        winner = 0;
        moveCount--;
        int opponent = getCurrentOpponent();
        while (peekHistory() < 0) { // add back captures
            addPieceAt(popHistory() * -1, opponent);
        }
        removePieceAt(popHistory()); // remove the placed piece
        switchPlayer();
    }

    public void redo(){
        if (moveCount == maxHistoryIndex){
            return;
        }
        addPieceAt(pipHistory(), currentPlayer);
        while (peekHistory() < 0) {
            removePieceAt(Math.abs(pipHistory()));
        }
        switchPlayer();
    }

    public void reset(){
        board = new int[BOARD_MAX_INDEX];
        moveCount = 0;
        historyIndex = 0;
        winner = 0;
        currentPlayer = GameSettings.FIRST_PLAYER;
        pieceCount[0] = 0;
        pieceCount[1] = 0;
    }

    /* Board Utils */

    public void random(float density)
    {
        reset();
        for (int i = 0; i < BOARD_MAX_INDEX; i++) {
            if (Math.random() < density){
                addPieceAt(i, (int)(Math.random() * 2) + 1);
            }
        }
    }

    /* Game Logic */

    private void switchPlayer(){
        currentPlayer = getOpponent(currentPlayer);
    }

    private void checkCapturesAt(int index){
        if (getSafePieceAt(index) != currentPlayer)
            return;
        int opponent = getCurrentOpponent();
        for (int dir : DIRECTION8){
            if (getSafePieceAt(index + dir) == opponent && 
                getSafePieceAt(index + dir * 2) == opponent && 
                getSafePieceAt(index + dir * 3) == currentPlayer){
                capture(index + dir);
                capture(index + dir * 2);
                // addCaptureCount(currentPlayer);
            }
        }
    }

    private void checkWinnerAt(int index){
        if (getSafePieceAt(index) != currentPlayer)
            return;
        int temp, count;
        for (int dir : DIRECTION4){
            count = 1;
            temp = index + dir;
            while (getSafePieceAt(temp) == currentPlayer){
                count++;
                temp += dir;
            }
            temp = index - dir;
            while (getSafePieceAt(temp) == currentPlayer){
                count++;
                temp -= dir;
            }
            if (count >= 5){
                winner = currentPlayer;
                return;
            }
        }
    }

    private void capture(int index){
        addHistory(-index);
        removePieceAt(index);
    }

    public boolean isInBound(int index){
        return index >= 0 && index < BOARD_MAX_INDEX;
    }

    /* getter */

    //Todo
    public int getCaptureCount(int player){
        // return pieceCount[getOpponent(player)];
        return 0;
    }

    public int getPieceAt(int index){
        return board[index];
    }

    public int getPieceAt(int x, int y){
        return board[x + y * BOARD_SIZE];
    }

    // protected getter
    public int getSafePieceAt(int index, int defaultValue){
        if (!isInBound(index))
            return defaultValue;
        return board[index];
    }

    // protected getter
    public int getSafePieceAt(int index){
        if (!isInBound(index))
            return -1;
        return board[index];
    }

    public int getMoveCount(){
        return moveCount;
    }

    public int getCurrentPlayer(){
        return currentPlayer;
    }

    public int getCurrentOpponent(){
        return (currentPlayer == 1) ? 2 : 1;
    }

    public int getOpponent(int player){
        return (player == 1) ? 2 : 1;
    }

    public int getWinner(){
        return winner;
    }

    private int[] moves = new int[20]; // max possible is one piece + 16 captures
    // not thread safe
    // return an array of the last board action (rm/add) in the last executed move, first element is the number of moves
    public int[] getLastMove(){
        int count = 1;
        while (history[historyIndex - count] < 0) {
            moves[count] = history[historyIndex - count];
            count++;
        }
        moves[count] = history[historyIndex - count];
        moves[0] = count;
        return moves;
    }

    // public int getCaptureCount(int player){
    //     return captureCount[player - 1];
    // }

    /* Setters */

    // public void addCaptureCount(int player){
    //     captureCount[player - 1] += 2;
    // }

    /* History */

    private void addHistory(int move){
        history[historyIndex] = move;
        historyIndex++;
    }

    private int popHistory(){
        // if (historyIndex == 0)
        //     return 0;
        historyIndex--;
        return history[historyIndex];
    }

    private int pipHistory(){
        historyIndex++;
        return history[historyIndex - 1];
    }
    
    // cut the history at current location
    // private void pruneHistory(){
    //     history[historyIndex + 1] = 0;
    // }

    private int peekHistory(){
        return history[historyIndex - 1];
    }
}
