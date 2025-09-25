package main.java.game;

import java.util.ArrayList;
import java.util.List;

import main.java.app.GameSettings;
import main.java.utils.GomokuUtils;

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
    private int[] history = new int[BOARD_MAX_INDEX * 2]; // safe buffer size
    private int historyIndex = 0;
    private int maxHistoryIndex = 0;
    private int currentPlayer = GameSettings.FIRST_PLAYER; // 1 is white, 2 is black
    private int moveCount = 0;
    private int maxMove = 0;
    private int winner = 0;
    private int[] pieceCount = {0,0};
    private boolean[] EGChistory = new boolean[BOARD_MAX_INDEX * 2]; // contains a flag per move if the move introduced an EGC
    // private 
    public List<Integer> endGameCapture = new ArrayList<Integer>();

    private static final int _x = 1;
    private static final int _y = BOARD_SIZE;
    public static final int[] DIRECTION8 = {
        -_y - _x, -_y, -_y + _x,
           - _x,        + _x,
        +_y - _x, +_y, +_y + _x
    };
    public static final int[] DIRECTION4 = {
        -_y - _x, // NW 
            -_y, // N
        -_y + _x, // NE
           - _x, // W
    };

    public Board() {
        reset();
    }

    public Board deepCopy() {
        Board copy = new Board();
    
        copy.board = this.board.clone();
    
        copy.history = this.history.clone();
        copy.historyIndex = this.historyIndex;
        copy.maxHistoryIndex = this.maxHistoryIndex;
    
        copy.currentPlayer = this.currentPlayer;
        copy.moveCount = this.moveCount;
        copy.winner = this.winner;
    
        copy.pieceCount = this.pieceCount.clone();
    
        copy.EGChistory = this.EGChistory.clone();
    
        copy.endGameCapture = new ArrayList<>(this.endGameCapture);
    
        return copy;
    }

    /* Core Action */

    private void addPieceAt(int index, int player) {
        if (!isSpaceAt(index)) {
            throw new IllegalStateException("No space at index " + index); 
        }
        pieceCount[player - 1]++;
        setPieceAt(index, player);
    }

    private void removePieceAt(int index) {
        pieceCount[getPieceAt(index) - 1]--;
        setPieceAt(index, 0);
    }

    /* Board Action */

    public void placePieceAt(int index) {
        moveCount++;
        addPieceAt(index, currentPlayer);
        addHistory(index);
        maxHistoryIndex = historyIndex;
        checkCapturesAt(index);
        checkWinnerEndGameCapture();
        if (getWinner() == 0) { // only check winner if EGC failed
            checkWinnerAt(index, currentPlayer);
            checkWinnerCapture();
        }
        switchPlayer();
    }

    public void undo() {
        if (moveCount == 0 || historyIndex == 0) {
            return ;
        }
        setWinner(0);
        EGChistory[moveCount] = false; // remove egc flag of old move
        moveCount--;
        while (peekHistory() < 0) { // add back captures
            addPieceAt(popHistory() * -1, currentPlayer);
        }
        removePieceAt(popHistory()); // remove the placed piece
        if (EGChistory[moveCount]) { // if the flag was set for this move, recalculate the egc for this. better solution would be to store the RGC index per move
            checkWinnerAt(peekLastPlacedMoveHistory(), getCurrentPlayer());
        }
        else {
            endGameCapture.clear();
        }
        switchPlayer();
    }

    public void redo() {
        if (moveCount >= maxMove) {
            return;
        }
        moveCount++;
        addPieceAt(Math.abs(pipHistory()), currentPlayer);
        while (peekFuture() < 0) {
            removePieceAt(Math.abs(pipHistory()));
        }
        switchPlayer();
    }

    public void goToMove(int move){
        move = Math.max(0, Math.min(move, maxMove));
        int dir = Integer.signum(move - moveCount);
        while (move != moveCount){
            if (dir < 0){
                undo();
            }
            else{
                redo();
            }
            if (moveCount == 0 || moveCount == maxMove)
                break;
        }
    }

    public void reset() {
        initBoard();
        moveCount = 0;
        maxMove = 0;
        historyIndex = 0;
        maxHistoryIndex = 0;
        setWinner(0);
        currentPlayer = GameSettings.FIRST_PLAYER;
        pieceCount[0] = 0;
        pieceCount[1] = 0;
    }

    private void initBoard() {
        final int b = BOARD_SIZE - 1;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
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
            if (getPieceAt(i) == 0 && Math.random() < density) {
                placePieceAt(i);
                analyser.scanLastMove();
            }
        }
    }

    // import position of this format 
    // 19 /////////8wbb8///////// w 0 0
    public void importPosition(String boardString){
        reset();
        System.out.println("Importing board: " + boardString);
        String[] parts = boardString.trim().split("\\s+");

        String boardSizeString = parts[0];
        String boardPosString = parts[1];
        String playerTurnString = parts[2];
        String blackCaptureString = parts[3];
        String whiteCaptureString = parts[4];

        // System.out.println("boardSizeString: " + boardSizeString);
        // System.out.println("boardPosString: " + boardPosString);
        // System.out.println("playerTurnString: " + playerTurnString);
        // System.out.println("blackCaptureString: " + blackCaptureString);
        // System.out.println("whiteCaptureString: " + whiteCaptureString);

        int boardSize = Integer.parseInt(boardSizeString);
        String[] position = boardPosString.split("/");
        int pieceCount = 0;
        for (int y = 0; y < position.length; y++){
            // System.out.println("row " + y + ": " + position[y]);
            if (position[y].isEmpty()) { // skip empty row
                continue;
            }
            String[] row = position[y].split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)|(?<=\\D)(?=\\D)");
            int x = 0;
            for (int i = 0; i < row.length; i++){
                char c = row[i].charAt(0);
                // System.out.printf("row[%d]: %s\n",i , row[i]);
                if (c == 'w' || c == 'b') {
                    int player = c == 'w' ? 1 : 2;
                    // System.out.printf("add piece at x:%d, y:%d, index:%d\n",x, y, (x + 1) + ((y + 1) * boardSize));
                    addPieceAt((x + 1) + ((y + 1) * (boardSize + GameSettings.BOARD_WALL_WIDTH * 2)), player);
                    x++;
                    pieceCount++;
                }
                else {
                    x += Integer.parseInt(row[i]);
                }
            }
        }

        currentPlayer = playerTurnString.charAt(0) == 'w' ? 1 : 2;
        moveCount = pieceCount + Integer.parseInt(blackCaptureString) + Integer.parseInt(whiteCaptureString);

        // System.out.println("boardSize: " + boardSize);
        // System.out.println("player turn: " + currentPlayer);
        // System.out.println("moveCount: " + moveCount);
        // System.out.println("blackCaptureString: " + blackCaptureString);
        // System.out.println("whiteCaptureString: " + whiteCaptureString);
    }

    public String exportPosition(){

        String boardSizeString = "19";
        String boardPosString;
        String playerTurnString = currentPlayer == 1 ? "w" : "b";
        String blackCaptureString = Integer.toString(getCaptureCount(2));
        String whiteCaptureString = Integer.toString(getCaptureCount(1));

        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < BOARD_MAX_INDEX;){
            while (i < BOARD_MAX_INDEX && getPieceAt(i) == -1) { // skip wall
                i++;
            }
            if (i >= BOARD_MAX_INDEX) {
                break;
            }
            int spaceCount = 0;
            while (getPieceAt(i) == 0) { // count space
                spaceCount++;
                i++;
            }
            if (getPieceAt(i) == 1 || getPieceAt(i) == 2) {
                if (spaceCount > 0){
                    strBuilder.append(Integer.toString(spaceCount));
                }
                strBuilder.append(getPieceAt(i) == 1 ? "w" : "b");
                i++;
            }
            if (getPieceAt(i) == -1) {
                strBuilder.append("/");
            }
        }
        strBuilder.deleteCharAt(strBuilder.length() - 1);
        boardPosString = strBuilder.toString();

        return boardSizeString + " " + boardPosString + " " + playerTurnString + " " + blackCaptureString + " " + whiteCaptureString;
    }

    public String exportGame(){
        int _moveCount = 1;
        boolean first;
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < maxHistoryIndex;){
            strBuilder.append(String.format("%d. %s", _moveCount, GomokuUtils.indexToString(history[i])));
            i++;
            if (history[i] < 0){
                strBuilder.append(" x");
            }
            first = true;
            while(history[i] < 0){
                if (!first){
                    strBuilder.append(",");
                }
                first = false;
                strBuilder.append(String.format("%s", GomokuUtils.indexToString(-history[i])));
                i++;
            }
            _moveCount++;
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }
    
    /*
        1. J10
        2. K10
        3. I10
        4. H10 xI10,J10
    */
    public void importGame(String gameString){
        reset();
        String[] moves = gameString.trim().split("\n");

        for (String move : moves){
            String[] parts = move.trim().split("\\s+");
            int moveIndex = GomokuUtils.stringToIndex(parts[1]);
            placePieceAt(moveIndex);
        }
    }

    /* Game Logic */

    private void switchPlayer() {
        currentPlayer = getOpponent(currentPlayer);
    }

    /* checks if the piece at index creates a capture */
    private void checkCapturesAt(int index) {
        if (getPieceAt(index) != currentPlayer)
            return;
        int opponent = getCurrentOpponent();
        for (int dir : DIRECTION8) {
            if (getPieceAt(index + dir) == opponent && 
                getPieceAt(index + dir * 2) == opponent && 
                getPieceAt(index + dir * 3) == currentPlayer) {
                capture(index + dir);
                capture(index + dir * 2);
                // addCaptureCount(currentPlayer);
            }
        }
    }

    /* checks if the current piece can be captured this turn*/
    public boolean canBeCapturedAt(int index, int capturingPlayer) {
        int opponent = getOpponent(capturingPlayer);
        if (getPieceAt(index) != opponent)
            return false;
        for (int dir : DIRECTION8) {
            int left = getPieceAt(index - dir); 
            int right = getPieceAt(index + dir);
            if (left == capturingPlayer) { // 2x10
                if (right == opponent && getPieceAt(index + dir * 2) == 0) {
                    return true;
                } else continue;
            } 
            if (right == capturingPlayer) { // 01x2
                if (left == opponent && getPieceAt(index - dir * 2) == 0) {
                    return true;
                } else continue;
            } 
            if (right == 0) { // 21x0
                if (left == opponent && getPieceAt(index - dir * 2) == capturingPlayer) {
                    return true;
                } else continue;
            } 
            if (left == 0) { // 0x12
                if (right == opponent && getPieceAt(index + dir * 2) == capturingPlayer) {
                    return true;
                } else continue;
            } 
        }
        return false;
    }

    // private void checkPotentialWinner() {
    //     if (winner != 0)
    //         return ;
    //     for (int index : potentialWinner) {
    //         checkWinnerAt(index, getCurrentOpponent());
    //     }
    // }

    private void checkWinnerCapture() {
        if (getCaptureCount(1) >= 10) {
            setWinner(1);
        }
        if (getCaptureCount(2) >= 10) {
            setWinner(2);
        }
    }

    private void checkWinnerEndGameCapture() {
        for (int index : endGameCapture) {
            if (getPieceAt(index) != 0) {
                checkWinnerAt(index, getCurrentOpponent());
            }
        }
        if (getWinner() == 0) { // no winner, which means a piece has been captured
            endGameCapture.clear();
        }
    }

    private void checkWinnerAt(int index, int player) {
        EGChistory[moveCount] = false;
        if (getPieceAt(index) != player)
            return;
        int right, left, count;
        for (int dir : DIRECTION4) {
            count = 1;
            right = index + dir;
            while (getPieceAt(right) == player) {
                count++;
                right += dir;
            }
            left = index - dir;
            while (getPieceAt(left) == player) {
                count++;
                left -= dir;
            }
            if (count >= 5) {
                left += dir;
                int opponent = getOpponent(currentPlayer); // check only for currentPlayer and not player
                int maxChain = 0;
                while (count > 0) {
                    count--;
                    maxChain++;
                    if (canBeCapturedAt(left, opponent)) {
                        EGChistory[moveCount] = true;
                        endGameCapture.add(left);
                        maxChain = 0;
                    }
                    left += dir;
                }
                if (maxChain >= 5) {
                    setWinner(player);
                }
            }
        }
    }

    private void capture(int index) {
        addHistory(-index);
        removePieceAt(index);
    }

    public boolean isInBound(int index) {
        return index >= 0 && index < BOARD_MAX_INDEX;
    }

    public boolean isPlayerAt(int index) {
        int piece = getPieceAt(index);
        return piece == 1 || piece == 2;
    }

    public boolean isSpaceAt(int index) {
        int piece = getPieceAt(index);
        return piece == 0;
    }

    public boolean isWallAt(int index) {
        int piece = getPieceAt(index);
        return piece == -1;
    }

    public int isFirst(int player) {
        return player == GameSettings.FIRST_PLAYER ? 1 : 0;
    }

    /* setter */

    private void setPieceAt(int index, int value) {
        board[index] = value;
    }

    private void setWinner(int player) {
        winner = player;
    }

    /* getter */

    /* /!\ Use this carefully only to GET pieces on the board faster, never use for SET */
    public int[] getBoard() {
        return board;
    }

    public int getCaptureCount(int player) {
        return ((moveCount + isFirst(getOpponent(player))) / 2) - pieceCount[getOpponent(player) - 1];
    }

    public int getPieceCount(int player) {
        return pieceCount[player - 1];
    }

    public int getPieceAt(int index) {
        return board[index];
    }

    public int getPieceAt(int x, int y) {
        return getPieceAt(x + y * BOARD_SIZE);
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentOpponent() {
        return (currentPlayer == 1) ? 2 : 1;
    }

    public int getOpponent(int player) {
        return (player == 1) ? 2 : 1;
    }

    public int getWinner() {
        return winner;
    }

    // return 0 if 0, 1 if 1, -1 if 2
    public static int getPlayerSign(int player) {
        if (player == 2)
            return -1;
        return player;
    }

    private int[] moves = new int[20]; // max possible is one piece + 16 captures
    // not thread safe
    // return an array of the last board action (rm/add) in the last executed move, 
    // /!\ first element is the number of moves
    public int[] getLastMoves() {
        int count = 1;
        if (historyIndex == 0)
            return null;
        while (history[historyIndex - count] < 0) {
            moves[count] = history[historyIndex - count];
            count++;
        }
        moves[count] = history[historyIndex - count];
        moves[0] = count;
        return moves;
    }
    /* History */

    private void addHistory(int move) {
        history[historyIndex] = move;
        history[historyIndex + 1] = 0; // cut history
        maxMove = moveCount;
        historyIndex++;
    }

    private int popHistory() {
        // if (historyIndex == 0)
        //     return 0;
        historyIndex--;
        return history[historyIndex];
    }

    private int pipHistory() {
        historyIndex++;
        return history[historyIndex - 1];
    }
    
    // cut the history at current location
    // private void pruneHistory() {
    //     history[historyIndex + 1] = 0;
    // }

    private int peekHistory() {
        return history[historyIndex - 1];
    }

    private int peekFuture() {
        return history[historyIndex];
    }

    private int peekLastPlacedMoveHistory() {
        int count = 1;
        while (history[historyIndex - count] < 0) {
            count++;
        }
        return history[historyIndex - count];
    }

    /* Print */

    // print current state of the board
    public void printBoard(){
        System.out.println("Print Board Move " + moveCount + ": ");
        System.out.println(toString());
    }

    public String toString(){
        int size = GameSettings.GAME_SIZE;
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                int index = pos.add(1).getId();
                int piece = getPieceAt(index);
                switch (piece) {
                    case 0: s.append("."); break;
                    case 1: s.append("1"); break;
                    case 2: s.append("2"); break;
                    default: s.append("?"); break;
                }
            }
            s.append("\n");
        }
        return s.toString();
    }

    /* This will mess up the board, only call when it crashes */
    public void printAll(){
        System.out.println("Print Board: ");
        while (moveCount > 0){
            EGChistory[moveCount] = false;
            printBoard();
            undo();
        }
    }
}
