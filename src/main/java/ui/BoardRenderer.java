package main.java.ui;
// src/BoardRenderer.java
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import main.java.GameSettings;
import main.java.GomokuAI.EvaluatedPosition;
import main.java.game.BoardGame;
import main.java.game.Cell;
import main.java.game.Coords;

public class BoardRenderer {
    public static final int TILE_SIZE = GameSettings.BOARD_PIXEL_SIZE / GameSettings.BOARD_SIZE;
    public static final int MARGIN = TILE_SIZE;
    public Color[] playerColor= {GameSettings.PLAYER1_COLOR, GameSettings.PLAYER2_COLOR};

    private Canvas canvas;
    private BoardGame game;
    private GraphicsContext gc;

    public BoardRenderer(Canvas canvas, BoardGame game) {
        this.canvas = canvas;
        this.game = game;

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleClick(e));

        gc = canvas.getGraphicsContext2D();
    }

    public void draw() {
        gc.setFill(GameSettings.BOARD_COLOR2);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawBoard();
    }

    private void drawBoard(){
        gc.translate(MARGIN, MARGIN);
        drawCheckBoard();
        drawGrid();
        drawLabel();
        drawPieces();
        drawSymbols();
        
        if (GameSettings.drawDebugNumber) drawDebugNumber();
        if (GameSettings.drawBestMove) drawBestMove();
        if (GameSettings.drawNeighbour) drawNeighbour();
        if (GameSettings.drawEvaluatedPosition) drawEvaluatedPosition();
        if (GameSettings.drawHeatmapNeighbour) drawHeatmapNeighbour();
        if (GameSettings.drawHeatmapScore) drawHeatmapScore();
        gc.translate(-MARGIN, -MARGIN);
    }

    private void drawGrid(){
        int size = game.BOARD_SIZE;
        for (int i = 0; i < size; i++)
        {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            int start = i * TILE_SIZE;
            int length = (size - 1) * TILE_SIZE;
            gc.strokeLine(start, 0, start, length);
            gc.strokeLine(0, start, length, start);
        }
    }

    private void drawLabel(){
        int size = game.BOARD_SIZE;
        for (int i = 0; i < size; i++)
        {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            int start = i * TILE_SIZE;
            int length = (size - 1) * TILE_SIZE;
            gc.setFill(Color.BLACK);
            String n = Integer.toString(i + GameSettings.LABEL_X_OFFSET, GameSettings.LABEL_X_BASE);
            int w = 8 * n.length(), h = 8, s2 = TILE_SIZE / 2; // 8 = size of a letter
            gc.fillText(n, start - w / 2, - s2);
            gc.fillText(n, start - w / 2, length + h + s2);
            n = Integer.toString(i + GameSettings.LABEL_Y_OFFSET, GameSettings.LABEL_Y_BASE);
            w = 8 * n.length(); h = 8; // 8 = size of a letter 
            gc.fillText(n, -w - s2, start + h / 2);
            gc.fillText(n, length + s2, start + h / 2);
        }
    }

    private void drawCheckBoard(){
        int size = game.BOARD_SIZE;
        int half = game.BOARD_SIZE / 2;
        for (int row = 0; row < size - 1; row++) {
            for (int col = 0; col < size - 1; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(GameSettings.BOARD_COLOR1);
                } else {
                    gc.setFill(GameSettings.BOARD_COLOR2);
                }
                gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                // draw dots on board
                int x = Math.abs(row - half); // symmetry around the center
                int y = Math.abs(col - half);
                if (x < half && y < half && (x % 6 == 0) && (y % 6 == 0))
                {
                    int radius = TILE_SIZE / 10;
                    gc.setFill(Color.BLACK);
                    gc.fillOval(col * TILE_SIZE - radius, row * TILE_SIZE - radius, radius * 2f, radius * 2f);
                }
            }
        }
    }

    private void drawDebugNumber(){
        int size = game.BOARD_SIZE;
        final int w = 8, h = 8; // size of a letter
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                
                Cell cell = game.board.getCellAt(new Coords(col, row));
                if (cell.has_piece()){
                    // gc.setFill(Color.color(0.5f, 0, 0, 0.5f));
                    // gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    String n = Integer.toString(row * game.BOARD_SIZE + col);
                    gc.setFill(Color.ORANGE);
                    gc.fillText(n, col * TILE_SIZE - w * n.length() / 2, row * TILE_SIZE + h / 2);
                }
            }
        }
    }

    private void drawPieces(){
        int size = game.BOARD_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Draw pieces
                int state = game.board.getCellAt(new Coords(col, row)).player;
                float radius = TILE_SIZE * 0.8f / 2f;
                int px = col * TILE_SIZE;
                int py = row * TILE_SIZE;
                if (state != 0) {
                    //shadow
                    gc.setFill(getPlayerColor(state).darker());
                    gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);
                    //fill
                    int offset = -2;
                    float r = radius * 0.88f;
                    gc.setFill(getPlayerColor(state));
                    gc.fillOval(px - r + offset, py - r + offset, r * 2f, r * 2f);
                    //outline
                    gc.setStroke(GameSettings.PIECE_BORDER_COLOR);
                    gc.setLineWidth(2); // thickness 
                    gc.strokeOval(px - radius, py - radius, radius * 2, radius * 2);
                }
            }
        }
    }

    private void drawBestMove(){
        if (!game.board.isInBound(game.bestMove))
            return;
        float radius = TILE_SIZE * 0.8f / 2f;
        int px = game.bestMove.x * TILE_SIZE;
        int py = game.bestMove.y * TILE_SIZE;
        gc.setFill(Color.rgb(255, 255, 255, 0.5f));
        gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);
        //outline
        gc.setStroke(Color.RED);
        gc.setLineWidth(2); // thickness 
        gc.strokeOval(px - radius, py - radius, radius * 2, radius * 2);
    }

    private void drawNeighbour(){
        for (Coords pos : game.board.neighbourCellIndexSet){
            Cell cell = game.board.getCellAt(pos);
            float radius = TILE_SIZE * 0.2f / 2f;
            if (cell.isNeighbour()) {
                gc.setFill(Color.BLUE);
                gc.fillOval(pos.x * TILE_SIZE - radius, pos.y * TILE_SIZE - radius, radius * 2f, radius * 2f);
            }
        }
    }

    private void drawEvaluatedPosition(){
        final int w = 8, h = 8; // size of a letter
        for (EvaluatedPosition evalpos : game.AI.evaluatedPos){
            float radius = TILE_SIZE * 0.8f / 2f;
            int px = evalpos.pos.x * TILE_SIZE;
            int py = evalpos.pos.y * TILE_SIZE;
            gc.setFill(Color.rgb(0, 0, 0, 0.3f));
            gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);

            String n = Integer.toString(evalpos.score);
            gc.setFill(Color.WHITE);
            gc.fillText(n, px - w * n.length() / 2, py + h / 2);
        }
    }

    private void drawHeatmapNeighbour(){
        int size = game.BOARD_SIZE;
        // int half = game.BOARD_SIZE / 2;
        // final int w = 8, h = 8; // size of a letter
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Cell cell = game.board.getCellAt(new Coords(col, row));
                Color color = GameSettings.HEATMAP_COLOR[cell.getNeighbourNumber()];
                gc.setFill(color);
                gc.fillRect(col * TILE_SIZE - TILE_SIZE / 2, row * TILE_SIZE - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);

                // String n = Integer.toString(cell.getNeighbourNumber());
                // gc.setFill(Color.ORANGE);
                // gc.fillText(n, col * TILE_SIZE - w * n.length() / 2, row * TILE_SIZE + h / 2);
            }
        }
    }

    private void drawHeatmapScore(){
        int size = game.BOARD_SIZE;
        int half = game.BOARD_SIZE / 2;
        final int w = 8, h = 8; // size of a letter
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Cell cell = game.board.getCellAt(new Coords(col, row));
                int score = game.board.getCellScoreAt(new Coords(col, row));
                Color color = GameSettings.getHeatMapColor(score, 20);
                gc.setFill(color);
                gc.fillRect(col * TILE_SIZE - TILE_SIZE / 2, row * TILE_SIZE - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);

                String n = Integer.toString(score);
                gc.setFill(Color.ORANGE);
                gc.fillText(n, col * TILE_SIZE - w * n.length() / 2, row * TILE_SIZE + h / 2);
            }
        }
    }
    
    private void drawSymbols(){
        int size = game.BOARD_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Cell cell = game.board.getCellAt(new Coords(col, row));
                int px = col * TILE_SIZE;
                int py = row * TILE_SIZE;
                if (cell.isDoubleFreeThree())
                    UtilsRenderer.drawX(gc, px, py, (int)(TILE_SIZE * 0.4), 5, Color.RED);
                else if (cell.isFreeThree())
                    UtilsRenderer.drawPlus(gc, px, py, (int)(TILE_SIZE * 0.5), 5, Color.GREEN);
                if (cell.can_be_captured){
                    UtilsRenderer.drawMinus(gc, px, py, (int)(TILE_SIZE * 0.4), 5, cell.player == 1 ? GameSettings.PLAYER2_COLOR : GameSettings.PLAYER1_COLOR);
                }
                if (cell.winning)
                {
                    UtilsRenderer.drawStarFull(gc, px, py, (int)(TILE_SIZE * 0.25), Color.YELLOW);
                    UtilsRenderer.drawStarOutline(gc, px, py, (int)(TILE_SIZE * 0.25), 1, Color.BLACK);
                }
            }
        }
    }

    public Color getPlayerColor(int player){
        return playerColor[player - 1];
    }

    private void handleClick(MouseEvent e) {
        int row = (int)((e.getY() - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        int col = (int)((e.getX() - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        game.placePieceAttempt(row, col);

        // System.out.println("row: " + row + ", col: " + col);
        // System.out.println("e.getX(): " + e.getX() + ", e.getY(): " + e.getY());
        draw();
    }
}
