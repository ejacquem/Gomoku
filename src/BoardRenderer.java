// src/BoardRenderer.java
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
// import javafx.scene.text.Font;

public class BoardRenderer {
    public static final int TILE_SIZE = 40;
    public static final int MARGIN = 80;
    public Color[] playerColor= {Color.WHITE, Color.BLACK};

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
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawBoard();
    }

    private void drawBoard(){
        gc.translate(MARGIN, MARGIN);
        drawCheckBoard();
        drawGrid();
        drawPieces();
        gc.translate(-MARGIN, -MARGIN);
    }

    private void drawGrid(){
        int size = game.getBoardSize();
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

    private void drawCheckBoard(){
        int size = game.getBoardSize();
        for (int row = 0; row < size - 1; row++) {
            for (int col = 0; col < size - 1; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Color.WHITESMOKE);
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(){
        int size = game.getBoardSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Draw pieces
                int state = game.getTileState(row, col);
                float radius = TILE_SIZE * 0.8f / 2f;
                if (state != 0) {
                    gc.setFill(Color.BLACK);
                    gc.fillOval(col * TILE_SIZE - radius, row * TILE_SIZE - radius, radius * 2f, radius * 2f);
                    radius *= 0.8;
                    gc.setFill(getPlayerColor(game.getTileState(row, col)));
                    gc.fillOval(col * TILE_SIZE - radius, row * TILE_SIZE - radius, radius * 2f, radius * 2f);
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
        game.placePiece(row, col);
        System.out.println("row: " + row + ", col: " + col);
        System.out.println("e.getX(): " + e.getX() + ", e.getY(): " + e.getY());
        draw();
    }
}
