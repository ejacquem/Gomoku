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

    public static final float HEATMAP_ALPHA = 0.5f;
    public static final Color[] HEATMAP_COLOR = new Color[]{
        Color.rgb(0,0,255,0.3),
        Color.rgb(0,125,255,0.3),
        Color.rgb(0,125,125,0.3),
        Color.rgb(0,255,125,0.3),
        Color.rgb(0,255,0,0.3),
        Color.rgb(125,225,0,0.3),
        Color.rgb(255,255,0,0.3),
        Color.rgb(255,125,0,0.3),
        Color.rgb(255,0,0,0.3),
    };

    public static boolean drawDebugNumber = false;
    public static boolean drawBestMove = false;
    public static boolean drawNeighbour = false;
    public static boolean drawEvaluatedPosition = false;
    public static boolean drawHeatmapNeighbour = false;
    public static boolean drawHeatmapScore = false;
    public static boolean aiPlaysAutomatic = false;
    public static boolean player1AI = false;
    public static boolean player2AI = false;

    public static Color getHeatMapColor(int value, int maxValue){
        float fvalue = Math.min((float)value / (float)maxValue, 1);
        fvalue *= (HEATMAP_COLOR.length - 1);
        int colorStartIndex = (int)Math.floor(fvalue);
        int colorEndIndex = (int)Math.ceil(fvalue);
        Color colorStart = HEATMAP_COLOR[Math.max(0, colorStartIndex)];
        Color colorEnd = HEATMAP_COLOR[Math.max(0, colorEndIndex)];
        return colorStart.interpolate(colorEnd, fvalue - colorStartIndex);
    }
}
