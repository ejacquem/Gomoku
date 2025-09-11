package main.java.ui;
// src/BoardRenderer.java
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import main.java.GameSettings;
import main.java.GomokuAI.EvaluatedPosition;
import main.java.game.Board;
import main.java.game.BoardGame;
import main.java.game.Cell;
import main.java.game.Coords;
import main.java.game.Board.CellScore;
import main.java.game.Board.SequenceData;

public class BoardRenderer {
    public static final int TILE_SIZE = GameSettings.BOARD_PIXEL_SIZE / GameSettings.BOARD_SIZE;
    public static final int MARGIN = TILE_SIZE;
    public Color[] playerColor= {GameSettings.PLAYER1_COLOR, GameSettings.PLAYER2_COLOR};

    private Canvas canvas;
    private Canvas overlayCanvas;
    private BoardGame game;
    private GraphicsContext gc;
    private GraphicsContext overlayGc;

    public BoardRenderer(Canvas canvas, Canvas overlayCanvas, BoardGame game) {
        this.canvas = canvas;
        this.overlayCanvas = overlayCanvas;
        this.game = game;

        gc = canvas.getGraphicsContext2D();
        overlayGc = overlayCanvas.getGraphicsContext2D();
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
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                CellScore score = game.board.getCellScoreAt(pos); 
                Color color = GameSettings.getHeatMapColor((int)score.getScore(), 20);
                gc.setFill(color);
                gc.fillRect(col * TILE_SIZE - TILE_SIZE / 2, row * TILE_SIZE - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);

                if (GameSettings.drawScoreNumber)
                    drawNumberAt(pos, 0, 0, (int)score.getScore(), Color.ORANGE);
                
                if (GameSettings.drawScorePlayerNumber){
                    drawNumberAt(pos, 0, 1.5f, (int)score.getPlayerScore(1), Color.WHITE);
                    drawNumberAt(pos, 0, -1.5f, (int)score.getPlayerScore(2), Color.BLACK);
                }
            }
        }
    }

    private void drawNumberAt(Coords gridPos, float anchorx, float anchory, int number, Color color){
        drawNumberAt(gc, gridPos, anchorx, anchory, number, color);
    }

    private void drawNumberAt(GraphicsContext gc, Coords gridPos, float anchorx, float anchory, int number, Color color){
        final int w = 8, h = 10; // size of a letter
        String str = Integer.toString(number);
        gc.setFill(color);
        int halfHeight = h / 2;
        int halfWidth = w * str.length() / 2;
        gc.fillText(str, 
        gridPos.x * TILE_SIZE - halfWidth + anchorx * halfWidth, 
        gridPos.y * TILE_SIZE + halfHeight + -(anchory * halfHeight)
        );
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

    public double mouseX = 0;
    public double mouseY = 0;

    public void drawOverlay(){
        overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        
        if (GameSettings.drawMousePos) drawMousePos(mouseX, mouseY);
        if (GameSettings.drawMouseGridPos) drawMouseGridPos();
        if (GameSettings.drawMouseCellPos) drawMouseCellPos();
        if (GameSettings.drawSequenceDataOnMouse) drawSequenceDataOnMouse();
    }

    public void drawMousePos(double x, double y){
        String strx = Integer.toString((int)x);
        String stry = Integer.toString((int)y);
        overlayGc.setFill(Color.WHITE);
        overlayGc.fillText("x: " + strx, x, y);
        overlayGc.fillText("y: " + stry, x, y - 16);
    }

    public void drawMouseGridPos(){
        Coords pos = pixelPosToCoords(mouseX, mouseY);
        String strx = Integer.toString((int)pos.x);
        String stry = Integer.toString((int)pos.y);
        overlayGc.setFill(Color.WHITE);
        overlayGc.fillText("x: " + strx, mouseX, mouseY - 32);
        overlayGc.fillText("y: " + stry, mouseX, mouseY - 48);
    }

    public void drawMouseCellPos(){
        Coords pos = pixelPosToCoords(mouseX, mouseY);
        overlayGc.setFill(Color.rgb(0, 125, 125, 0.2));
        overlayGc.fillRect(pos.x * TILE_SIZE + TILE_SIZE / 2, pos.y * TILE_SIZE + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
    }

    public void drawSequenceDataOnMouse(){
        Coords pos = pixelPosToCoords(mouseX, mouseY);
        Board.SequenceData data = game.board.new SequenceData();
        for (Coords dir : game.board.DIRECTION8){
            Coords temp = pos.add(dir);
            game.board.pieceSequenceScoreInDir(temp, dir, data);

            overlayGc.setFill(Color.rgb(125, 125, 0, 0.5));
            if (data.player == 1 || data.player == 2)
                overlayGc.setFill(getPlayerColor(data.player, 0.5));
            for (int i = 0; i < data.pieceNumber; i++){
                drawCenterTileAt(overlayGc, temp);
                temp.addTo(dir);
            }
            overlayGc.setFill(Color.rgb(0, 125, 0, 0.5));
            for (int i = 0; i < data.trailSpaceNumber; i++){
                drawCenterTileAt(overlayGc, temp);
                temp.addTo(dir);
            }
            overlayGc.setFill(Color.rgb(125, 0, 0, 0.5));
            if (data.trailPiece == 1 || data.trailPiece == 2)
                overlayGc.setFill(getPlayerColor(data.trailPiece, 0.5));
            drawCenterTileAt(overlayGc, temp);
        }
    }

    public void drawTileAt(GraphicsContext g, Coords pos){
        g.fillRect(pos.x * TILE_SIZE, pos.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    public void drawCenterTileAt(GraphicsContext g, Coords pos){
        g.fillRect(pos.x * TILE_SIZE + TILE_SIZE / 2, pos.y * TILE_SIZE + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
    }

    public Color getPlayerColor(int player){
        return playerColor[player - 1];
    }

    public Color getPlayerColor(int player, double alpha){
        Color color = playerColor[player - 1];
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public Coords pixelPosToCoords(double x, double y){
        int row = (int)((y - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        int col = (int)((x - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        return new Coords(col, row);
    }

}
