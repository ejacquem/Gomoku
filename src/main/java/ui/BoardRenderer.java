package main.java.ui;
// src/BoardRenderer.java
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import main.java.GameSettings;
import main.java.game.BoardAnalyser;
import main.java.game.BoardGame;
import main.java.game.Coords;
// import main.java.game.Board.CellScore;

public class BoardRenderer {
    public static final int TILE_SIZE = GameSettings.BOARD_PIXEL_SIZE / GameSettings.GAME_SIZE;
    public static final int MARGIN = TILE_SIZE;
    public Color[] playerColor= {GameSettings.PLAYER1_COLOR, GameSettings.PLAYER2_COLOR};

    private Canvas canvas;
    private Canvas overlayCanvas;
    private BoardGame game;
    private GraphicsContext gc;
    private GraphicsContext overlayGc;
    private double canvasWidth;
    private double canvasHeight;

    public BoardRenderer(Canvas canvas, Canvas overlayCanvas, BoardGame game) {
        this.canvas = canvas;
        this.overlayCanvas = overlayCanvas;
        this.game = game;

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

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
        // drawSymbols();
        
        if (GameSettings.drawDebugNumber) drawDebugNumber();
        // if (GameSettings.drawBestMove) drawBestMove();
        // if (GameSettings.drawEvaluatedPosition) drawEvaluatedPosition();
        if (GameSettings.drawSortedPosition) drawSortedPosition();
        if (GameSettings.drawHeatmapScore) drawHeatmapScore();
        gc.translate(-MARGIN, -MARGIN);
    }

    private void drawGrid(){
        int size = GameSettings.GAME_SIZE;
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
        int size = GameSettings.GAME_SIZE;
        for (int i = 0; i < size; i++)
        {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            int start = i * TILE_SIZE;
            int length = (size - 1) * TILE_SIZE;
            gc.setFill(Color.BLACK);
            String n = Integer.toString(i + GameSettings.LABEL_X_OFFSET, GameSettings.LABEL_X_BASE);
            int w = 8 * n.length(), h = 10, s2 = TILE_SIZE / 2; // 8 = size of a letter
            gc.fillText(n, start - w / 2, - s2);
            gc.fillText(n, start - w / 2, length + h + s2);
            n = Integer.toString(i + GameSettings.LABEL_Y_OFFSET, GameSettings.LABEL_Y_BASE);
            w = 8 * n.length(); h = 10; // 8 = size of a letter 
            gc.fillText(n, -w - s2, start + h / 2);
            gc.fillText(n, length + s2, start + h / 2);
        }
    }

    private void drawCheckBoard(){
        int size = GameSettings.GAME_SIZE;
        int half = GameSettings.GAME_SIZE / 2;
        for (int row = 0; row < size - 1; row++) {
            for (int col = 0; col < size - 1; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(GameSettings.BOARD_COLOR1);
                } else {
                    gc.setFill(GameSettings.BOARD_COLOR2);
                }
                gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                // draw dots on board
                int dx = Math.abs(row - half); // symmetry around the center
                int dy = Math.abs(col - half);
                if (dx < half && dy < half && (dx % 6 == 0) && (dy % 6 == 0))
                {
                    int radius = TILE_SIZE / 10;
                    gc.setFill(Color.BLACK);
                    gc.fillOval(col * TILE_SIZE - radius, row * TILE_SIZE - radius, radius * 2f, radius * 2f);
                }
            }
        }
    }

    private void drawDebugNumber(){
        int size = GameSettings.GAME_SIZE;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                drawNumberAt(gc, x, y, 0f, 0f, (1 + y) * game.BOARD_SIZE + 1 + x, Color.ORANGE);
            }
        }
    }

    private void drawPieces(){
        int size = GameSettings.GAME_SIZE;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int piece = game.board.getPieceAt( 1 + x, 1 + y);
                if (piece != 0) {
                    drawPieceAt(x * TILE_SIZE, y * TILE_SIZE, getPieceColor(piece));
                }
            }
        }
    }

    private void drawPieceAt(int x, int y, Color color){
        final float radius = TILE_SIZE * 0.8f / 2f;
        //shadow
        gc.setFill(color.darker());
        gc.fillOval(x - radius, y - radius, radius * 2f, radius * 2f);
        //fill
        int offset = -2;
        float r = radius * 0.88f;
        gc.setFill(color);
        gc.fillOval(x - r + offset, y - r + offset, r * 2f, r * 2f);
        //outline
        gc.setStroke(GameSettings.PIECE_BORDER_COLOR);
        gc.setLineWidth(2); // thickness 
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    // private void drawBestMove(){
    //     if (!game.board.isInBound(game.bestMove))
    //         return;
    //     float radius = TILE_SIZE * 0.8f / 2f;
    //     int px = game.bestMove.x * TILE_SIZE;
    //     int py = game.bestMove.y * TILE_SIZE;
    //     gc.setFill(Color.rgb(255, 255, 255, 0.5f));
    //     gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);
    //     //outline
    //     gc.setStroke(Color.RED);
    //     gc.setLineWidth(2); // thickness 
    //     gc.strokeOval(px - radius, py - radius, radius * 2, radius * 2);
    // }

    // private void drawEvaluatedPosition(){
    //     final int w = 8, h = 8; // size of a letter
    //     for (EvaluatedPosition evalpos : game.AI.evaluatedPos){
    //         float radius = TILE_SIZE * 0.8f / 2f;
    //         int px = evalpos.pos.x * TILE_SIZE;
    //         int py = evalpos.pos.y * TILE_SIZE;
    //         gc.setFill(Color.rgb(0, 0, 0, 0.3f));
    //         gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);

    //         String n = Integer.toString(evalpos.score);
    //         gc.setFill(Color.WHITE);
    //         gc.fillText(n, px - w * n.length() / 2, py + h / 2);
    //     }
    // }

    private void drawSortedPosition(){
        int[] sortedIndices = game.boardAnalyser.getSortedIndices();

        // final int w = 8, h = 8; // size of a letter
        int i = 1;
        for (int index : sortedIndices){
            // float radius = TILE_SIZE * 0.8f / 2f;
            Coords pos = Coords.getCoordsById(index, GameSettings.BOARD_SIZE).add(-1);
            // int px = pos.x * TILE_SIZE;
            // int py = pos.y * TILE_SIZE;
            // gc.setFill(Color.rgb(0, 0, 0, 0.3f));
            // gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);

            drawNumberAt(pos, 1, 2, i, Color.RED);
            i++;
            // String n = Integer.toString(i);
            // gc.setFill(Color.WHITE);
            // gc.fillText(n, px - w * n.length() / 2, py + h / 2);
        }
    }

    private void drawHeatmapScore(){
        int size = GameSettings.GAME_SIZE;
        int[] scoregrid = game.boardAnalyser.getCurrentScoreGrid();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                int score = scoregrid[pos.add(1).getId()]; 
                Color color = GameSettings.getHeatMapColor(score, 20);
                gc.setFill(color);
                gc.fillRect(col * TILE_SIZE - TILE_SIZE / 2, row * TILE_SIZE - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);

                if (GameSettings.drawScoreNumber && score != 0)
                    drawNumberAt(pos, 0, 0, score, Color.ORANGE);
    
                // if (GameSettings.drawScorePlayerNumber){
                //     drawNumberAt(pos, 0, 1.5f, (int)score.getPlayerScore(1), Color.WHITE);
                //     drawNumberAt(pos, 0, -1.5f, (int)score.getPlayerScore(2), Color.BLACK);
                // }
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

    private void drawNumberAt(GraphicsContext gc, int x, int y, float anchorx, float anchory, int number, Color color){
        final int w = 8, h = 10; // size of a letter
        String str = Integer.toString(number);
        gc.setFill(color);
        int halfHeight = h / 2;
        int halfWidth = w * str.length() / 2;
        gc.fillText(str, 
        x * TILE_SIZE - halfWidth + anchorx * halfWidth, 
        y * TILE_SIZE + halfHeight + -(anchory * halfHeight)
        );
    }
    
    // private void drawSymbols(){
    //     int size = game.BOARD_SIZE;
    //     for (int row = 0; row < size; row++) {
    //         for (int col = 0; col < size; col++) {
    //             Cell cell = game.board.getCellAt(new Coords(col, row));
    //             int px = col * TILE_SIZE;
    //             int py = row * TILE_SIZE;
    //             if (cell.isDoubleFreeThree())
    //                 UtilsRenderer.drawX(gc, px, py, (int)(TILE_SIZE * 0.4), 5, Color.RED);
    //             else if (cell.isFreeThree())
    //                 UtilsRenderer.drawPlus(gc, px, py, (int)(TILE_SIZE * 0.5), 5, Color.GREEN);
    //             if (cell.can_be_captured){
    //                 UtilsRenderer.drawMinus(gc, px, py, (int)(TILE_SIZE * 0.4), 5, cell.player == 1 ? GameSettings.PLAYER2_COLOR : GameSettings.PLAYER1_COLOR);
    //             }
    //             if (cell.winning)
    //             {
    //                 UtilsRenderer.drawStarFull(gc, px, py, (int)(TILE_SIZE * 0.25), Color.YELLOW);
    //                 UtilsRenderer.drawStarOutline(gc, px, py, (int)(TILE_SIZE * 0.25), 1, Color.BLACK);
    //             }
    //         }
    //     }
    // }

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
        if (!isInBoardBound(mouseX, mouseY))
            return;
        Coords pos = pixelPosToCoords(mouseX, mouseY);
        BoardAnalyser.SequenceData data = game.boardAnalyser.new SequenceData();
        System.out.println("drawSequenceDataOnMouse");
        for (Coords dir : Coords.DIRECTION8){
            System.out.println("dir: " + dir + ", id: " + dir.getId());
            Coords temp = pos.add(dir);
            if (game.board.getPieceAt(temp.add(1).getId()) == -1){
                data.reset();
            }
            else{
                game.boardAnalyser.pieceSequenceDataInDir(temp.add(1).getId(), dir.getId(), data);
            }

            overlayGc.setFill(Color.rgb(125, 125, 0, 0.5));
            if (data.player == 1 || data.player == 2)
                overlayGc.setFill(getPieceColor(data.player, 0.5));
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
                overlayGc.setFill(getPieceColor(data.trailPiece, 0.5));
            drawCenterTileAt(overlayGc, temp);
        }
    }

    public void drawTileAt(GraphicsContext g, Coords pos){
        g.fillRect(pos.x * TILE_SIZE, pos.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    public void drawCenterTileAt(GraphicsContext g, Coords pos){
        g.fillRect(pos.x * TILE_SIZE + TILE_SIZE / 2, pos.y * TILE_SIZE + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
    }

    /* Utils */

    public Color getPieceColor(int player){
        if (player == -1){
            return Color.GRAY;
        }
        if (player != 1 && player != 2){
            return Color.PURPLE;
        }
        return playerColor[player - 1];
    }

    public Color getPieceColor(int player, double alpha){
        Color color = getPieceColor(player);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public Coords pixelPosToCoords(double x, double y){
        int row = (int)((y - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        int col = (int)((x - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        return new Coords(col, row);
    }

    public boolean isInBoardBound(double x, double y){
        return x >= MARGIN / 2 && y >= MARGIN / 2 && x < (canvasWidth - MARGIN / 2) && y < (canvasHeight - MARGIN / 2);
    }

    public boolean isInGridBound(double x, double y){
        return x >= MARGIN && y >= MARGIN && x < (canvasWidth - MARGIN) && y < (canvasHeight - MARGIN);
    }

}
