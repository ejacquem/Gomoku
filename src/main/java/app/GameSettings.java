package main.java.app;
import javafx.scene.paint.Color;

public abstract class GameSettings {
    public static final Color BOARD_COLOR1 = Color.web("#995a38");  
    // public static final Color BOARD_COLOR2 = Color.web("#FFE0B5");
    // public static final Color BOARD_COLOR1 = Color.web("#F2F4F3");
    public static final Color BOARD_COLOR2 = Color.web("#a8693f");
    public static final Color BOARD_COLOR_INVERSE = Color.web("#3F7EA8");
    public static final Color UI_BACKGROUND_LIGHTER = Color.web("#363431");
    public static final Color UI_BACKGROUND = Color.web("#302e2b");
    public static final Color UI_BACKGROUND_DARKER = Color.web("#272523");
    public static final Color PLAYER1_COLOR = Color.web("#f9f9f9");
    public static final Color PLAYER2_COLOR = Color.web("#202020");
    public static final Color PIECE_BORDER_COLOR = Color.web("#101010");
    public static final Color ARROW_COLOR = Color.rgb(
        (int)(BOARD_COLOR_INVERSE.getRed() * 255), 
        (int)(BOARD_COLOR_INVERSE.getGreen() * 255), 
        (int)(BOARD_COLOR_INVERSE.getBlue() * 255), 0.5);

    public static final int GAME_SIZE = 19;
    public static final int BOARD_PIXEL_SIZE = 800;
    public static final int BOARD_WALL_WIDTH = 1;
    public static final int BOARD_SIZE = GAME_SIZE + 2 * BOARD_WALL_WIDTH;
    public static final int BOARD_MAX_INDEX_SIZE = BOARD_SIZE * BOARD_SIZE;
    public static final int FIRST_PLAYER = 2;
    public static final int WINNING_PIECES = 5;
    public static final int WINNING_CAPTURED_PIECES = 10;
    public static final int START_TIMER = 300 * 1000; // sec

    // public static final int LABEL_X_BASE = 10;
    // public static final int LABEL_Y_BASE = 10;
    // public static final int LABEL_X_OFFSET = 0;
    // public static final int LABEL_Y_OFFSET = 0;

    public static final float HEATMAP_ALPHA = 0.5f;
    public static final Color[] HEATMAP_COLOR = new Color[]{
        Color.rgb(0,0,255,0.0),
        Color.rgb(0,125,255,0.3),
        Color.rgb(0,125,125,0.3),
        Color.rgb(0,255,125,0.3),
        Color.rgb(0,255,0,0.3),
        Color.rgb(125,225,0,0.3),
        Color.rgb(255,255,0,0.3),
        Color.rgb(255,125,0,0.3),
        Color.rgb(255,0,0,0.3),
    };

    /* Board visual */

    public static boolean gridToggle = true;
    public static double gridWidth = 2f;
    public static boolean labelToggle = true;
    public static boolean labelXNumberBase = false;
    public static boolean labelYNumberBase = true;
    public static boolean chessBoard = false;
    public static boolean gomokuBoard = true;
    public static boolean showSymbolToggle = true;

    /* Board Debug */

    public static boolean drawIndexNumber = false;
    public static boolean drawBestMove = false;
    public static boolean drawEvaluatedPosition = false;
    public static boolean drawSortedPosition = false;
    public static boolean drawScoreHeatmap = false;
    public static boolean drawScoreNumber = false;
    public static boolean drawBucketScoreNumber = false;
    public static boolean drawDirScorePlayerNumber = false;
    public static boolean drawScorePlayerNumber = false;
    public static boolean drawCurrentSearchDepth = false;
    public static boolean drawCurrentBestEval = false;
    
    public static boolean analyseBoard = false;
    public static boolean player1AI = false;
    public static boolean player2AI = false;

    public static boolean drawMousePos = false;
    public static boolean drawMouseGridPos = false;
    public static boolean drawMouseCellPos = false;
    public static boolean drawSequenceDataOnMouse = false;

    /* AI */

    public static boolean launchAnalysis = false;
    public static int analysisDepth = 5;


    public static Color getHeatMapColor(int value, int maxValue) {
        float fvalue = Math.min((float)value / (float)maxValue, 1);
        fvalue *= (HEATMAP_COLOR.length - 1);
        int colorStartIndex = (int)Math.floor(fvalue);
        int colorEndIndex = (int)Math.ceil(fvalue);
        Color colorStart = HEATMAP_COLOR[Math.max(0, colorStartIndex)];
        Color colorEnd = HEATMAP_COLOR[Math.max(0, colorEndIndex)];
        return colorStart.interpolate(colorEnd, fvalue - colorStartIndex);
    }
}
