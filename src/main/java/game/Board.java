package main.java.game;

import main.java.app.GameSettings;

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
    private int currentPlayer = GameSettings.FIRST_PLAYER; // 1 is white, 2 is black
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
        +y + x  // SE
    };

    public Board(){
        reset();
    }

    /* Core Action */

    private void addPieceAt(int index, int player){
        if (!isSpaceAt(index)){
            throw new IllegalStateException("No space at index " + index); 
        }
        pieceCount[player - 1]++;
        setPieceAt(index, player);
    }

    private void removePieceAt(int index){
        pieceCount[getPieceAt(index) - 1]--;
        setPieceAt(index, 0);
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
        while (peekHistory() < 0) { // add back captures
            addPieceAt(popHistory() * -1, currentPlayer);
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
        initBoard();
        moveCount = 0;
        historyIndex = 0;
        winner = 0;
        currentPlayer = GameSettings.FIRST_PLAYER;
        pieceCount[0] = 0;
        pieceCount[1] = 0;
    }

    private void initBoard(){
        final int b = BOARD_SIZE - 1;
        for (int y = 0; y < BOARD_SIZE; y++){
            for (int x = 0; x < BOARD_SIZE; x++){
                int value = (x == b || y == b || x == 0 || y == 0) ? -1 : 0;
                setPieceAt(x + y * BOARD_SIZE, value);
            }
        }
    }

    /* Board Utils */

    public void random(float density, BoardAnalyser analyser)
    {
        reset();
        for (int i = 0; i < BOARD_MAX_INDEX; i++) {
            if (getPieceAt(i) == 0 && Math.random() < density){
                placePieceAt(i);
                analyser.scanLastMove();
            }
        }
    }

    /* Game Logic */

    private void switchPlayer(){
        currentPlayer = getOpponent(currentPlayer);
    }

    private void checkCapturesAt(int index){
        if (getPieceAt(index) != currentPlayer)
            return;
        int opponent = getCurrentOpponent();
        for (int dir : DIRECTION8){
            if (getPieceAt(index + dir) == opponent && 
                getPieceAt(index + dir * 2) == opponent && 
                getPieceAt(index + dir * 3) == currentPlayer){
                capture(index + dir);
                capture(index + dir * 2);
                // addCaptureCount(currentPlayer);
            }
        }
    }

    private void checkWinnerAt(int index){
        if (getPieceAt(index) != currentPlayer)
            return;
        int temp, count;
        for (int dir : DIRECTION4){
            count = 1;
            temp = index + dir;
            while (getPieceAt(temp) == currentPlayer){
                count++;
                temp += dir;
            }
            temp = index - dir;
            while (getPieceAt(temp) == currentPlayer){
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

    public boolean isPlayerAt(int index){
        int piece = getPieceAt(index);
        return piece == 1 || piece == 2;
    }

    public boolean isSpaceAt(int index){
        int piece = getPieceAt(index);
        return piece == 0;
    }

    public boolean isWallAt(int index){
        int piece = getPieceAt(index);
        return piece == -1;
    }

    public int isFirst(int player){
        return player == GameSettings.FIRST_PLAYER ? 1 : 0;
    }

    /* setter */

    private void setPieceAt(int index, int value){
        board[index] = value;
    }

    /* getter */

    /* /!\ Use this carefully only to GET pieces on the board faster, never use for SET */
    public int[] getBoard(){
        return board;
    }

    public int getCaptureCount(int player){
        return ((moveCount + isFirst(getOpponent(player))) / 2) - pieceCount[getOpponent(player) - 1];
    }

    public int getPieceAt(int index){
        return board[index];
    }

    public int getPieceAt(int x, int y){
        return getPieceAt(x + y * BOARD_SIZE);
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

    // return 0 if 0, 1 if 1, -1 if 2
    public static int getPlayerSign(int player){
        if (player == 2)
            return -1;
        return player;
    }

    private int[] moves = new int[20]; // max possible is one piece + 16 captures
    // not thread safe
    // return an array of the last board action (rm/add) in the last executed move, 
    // /!\ first element is the number of moves
    public int[] getLastMoves(){
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
