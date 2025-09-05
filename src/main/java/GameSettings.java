package main.java;
import javafx.scene.paint.Color;

public abstract class GameSettings {
    public static final Color BOARD_COLOR1 = Color.web("#995a38");  
    // public static final Color BOARD_COLOR2 = Color.web("#FFE0B5");
    // public static final Color BOARD_COLOR1 = Color.web("#F2F4F3");
    public static final Color BOARD_COLOR2 = Color.web("#a8693f");
    public static final Color UI_BACKGROUND = Color.web("#302e2b");
    // public static final Color UI_BACKGROUND = Color.web("#373533");
    public static final Color PLAYER1_COLOR = Color.web("#f9f9f9");
    public static final Color PLAYER2_COLOR = Color.web("#202020");
    public static final Color PIECE_BORDER_COLOR = Color.web("#101010");

    public static final int BOARD_PIXEL_SIZE = 19 * 40;
    public static final int BOARD_SIZE = 19;
    public static final int FIRST_PLAYER = 1;
    public static final int WINNING_PIECES = 5;
    public static final int WINNING_CAPTURED_PIECES = 10;
    public static final int START_TIMER = 300 * 1000; // sec

    public static final int LABEL_X_BASE = 10;
    public static final int LABEL_Y_BASE = 10;
    public static final int LABEL_X_OFFSET = 0;
    public static final int LABEL_Y_OFFSET = 0;

    public static int isPlayer1First(){
        return FIRST_PLAYER == 1 ? 1 : 0;
    }
    public static int isPlayer2First(){
        return FIRST_PLAYER == 2 ? 1 : 0;
    }
}
