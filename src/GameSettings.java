import javafx.scene.paint.Color;

public abstract class GameSettings {
    static final Color BOARD_COLOR1 = Color.web("#995a38");  
    // static final Color BOARD_COLOR2 = Color.web("#FFE0B5");
    // static final Color BOARD_COLOR1 = Color.web("#F2F4F3");
    static final Color BOARD_COLOR2 = Color.web("#a8693f");
    static final Color UI_BACKGROUND = Color.web("#302e2b");
    // static final Color UI_BACKGROUND = Color.web("#373533");
    static final Color PLAYER1_COLOR = Color.web("#f9f9f9");
    static final Color PLAYER2_COLOR = Color.web("#202020");
    static final Color PIECE_BORDER_COLOR = Color.web("#101010");

    static final int BOARD_SIZE = 19;
    static final int FIRST_PLAYER = 1;
    static final int WINNING_PIECES = 5;
    static final int WINNING_CAPTURED_PIECES = 10;
    static final int START_TIMER = 300 * 1000; // sec
}
