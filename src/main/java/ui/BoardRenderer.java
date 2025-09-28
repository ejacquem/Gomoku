package main.java.ui;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import main.java.app.GameSettings;
import main.java.game.BoardAnalyser.PosScore;
import main.java.game.BoardGame;
import main.java.game.Coords;
import main.java.game.GomokuAI;

public class BoardRenderer {
    public static final int TILE_SIZE = GameSettings.BOARD_PIXEL_SIZE / GameSettings.GAME_SIZE;
    public static final int MARGIN = TILE_SIZE;
    public Color[] playerColor= {GameSettings.PLAYER1_COLOR, GameSettings.PLAYER2_COLOR};

    private Canvas canvas;
    private Canvas overlayCanvas;
    private Canvas renderLoopCanvas;
    private BoardGame game;
    private GraphicsContext gc;
    private GraphicsContext overlayGc;
    private GraphicsContext renderLoopGc;
    private double canvasWidth;
    private double canvasHeight;

    public BoardRenderer(Canvas canvas, Canvas overlayCanvas, Canvas renderLoopCanvas, BoardGame game) {
        this.canvas = canvas;
        this.overlayCanvas = overlayCanvas;
        this.renderLoopCanvas = renderLoopCanvas;
        this.game = game;

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        gc = canvas.getGraphicsContext2D();
        overlayGc = overlayCanvas.getGraphicsContext2D();
        renderLoopGc = renderLoopCanvas.getGraphicsContext2D();
    }

    public void startRenderLoop() {
        Timeline loop = new Timeline(
            new KeyFrame(Duration.seconds(0.1), event -> {
                renderLoop();
            })
        );
        loop.setCycleCount(Timeline.INDEFINITE);
        loop.play();
    }

    private void renderLoop(){
        renderLoopGc.clearRect(0, 0, renderLoopCanvas.getWidth(), renderLoopCanvas.getHeight());
        if (GameSettings.drawCurrentSearchDepth) drawCurrentSearchDepth();
    }

    public void draw() {
        gc.setFill(GameSettings.BOARD_COLOR2);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawBoard();
    }

    private void drawBoard() {
        gc.translate(MARGIN, MARGIN);
        if (GameSettings.gomokuBoard) drawCheckBoard();
        if (GameSettings.chessBoard) drawChessBoard();
        if (GameSettings.gridToggle) drawGrid();
        if (GameSettings.labelToggle) drawLabel();
        drawPieces();
        if (GameSettings.showSymbolToggle) drawSymbols();
        highlightLastMove();
        drawCaptures();
        
        game.boardAnalyser.updateMoveCount();;
        if (GameSettings.drawIndexNumber) drawIndexNumber();
        if (GameSettings.drawBestMove) drawBestMove();
        if (GameSettings.drawEvaluatedPosition) drawEvaluatedPosition();
        if (GameSettings.drawSortedPosition) drawSortedPosition();
        if (GameSettings.drawScoreHeatmap) drawScoreHeatmap();
        if (GameSettings.drawScoreNumber) drawScoreNumber();
        if (GameSettings.drawBucketScoreNumber) drawBucketScoreNumber();
        if (GameSettings.drawScorePlayerNumber) drawScorePlayerNumber();
        gc.translate(-MARGIN, -MARGIN);
    }

    private void drawGrid() {
        int size = GameSettings.GAME_SIZE;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(GameSettings.gridWidth);
        double x = GameSettings.gridWidth / 2;
        double frac = x - Math.floor(x);
        for (int i = 0; i < size; i++)
        {
            double start = i * TILE_SIZE + frac;
            double length = (size - 1) * TILE_SIZE;
            gc.strokeLine(start, frac, start, length);
            gc.strokeLine(frac, start, length, start);
        }
    }

    private void drawLabel() {
        int size = GameSettings.GAME_SIZE;
        int labelxoffset = (GameSettings.labelXNumberBase ? 1 : 10);
        int labelxbase = (GameSettings.labelXNumberBase ? 10 : 36);
        int labelyoffset = (GameSettings.labelYNumberBase ? 1 : 10);
        int labelybase = (GameSettings.labelYNumberBase ? 10 : 36);
        for (int i = 0; i < size; i++)
        {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            int start = i * TILE_SIZE;
            int length = (size - 1) * TILE_SIZE;
            gc.setFill(Color.BLACK);
            String n;
            n = Integer.toString(i + labelxoffset, labelxbase).toUpperCase();
            int w = 8 * n.length(), h = 10, s2 = TILE_SIZE / 2; // 8 = size of a letter
            gc.fillText(n, start - w / 2, - s2);
            gc.fillText(n, start - w / 2, length + h + s2);
            n = Integer.toString(size - 1 - i + labelyoffset, labelybase).toUpperCase();
            w = 8 * n.length(); h = 10; // 8 = size of a letter 
            gc.fillText(n, -w - s2, start + h / 2);
            gc.fillText(n, length + s2, start + h / 2);
        }
    }

    private void drawCheckBoard() {
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

    private void drawChessBoard() {
        int size = GameSettings.GAME_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(GameSettings.BOARD_COLOR1);
                } else {
                    gc.setFill(GameSettings.BOARD_COLOR2);
                }
                gc.fillRect(col * TILE_SIZE - TILE_SIZE / 2, row * TILE_SIZE - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawIndexNumber() {
        int size = GameSettings.GAME_SIZE;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                drawNumberAt(gc, x, y, 0f, 0f, (1 + y) * game.BOARD_SIZE + 1 + x, Color.ORANGE);
            }
        }
    }

    private void drawPieces() {
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

    private void drawPieceAt(int x, int y, Color color) {
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

    private void drawBestMove() {
        if (!game.board.isInBound(game.bestMove))
            return;
        float radius = TILE_SIZE * 0.8f / 2f;
        Coords pos = Coords.getCoordsById(game.bestMove).add(-1);
        int px = pos.x * TILE_SIZE;
        int py = pos.y * TILE_SIZE;
        gc.setFill(Color.rgb(255, 255, 255, 0.5f));
        gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);
        //outline
        gc.setStroke(Color.RED);
        gc.setLineWidth(2); // thickness 
        gc.strokeOval(px - radius, py - radius, radius * 2, radius * 2);
    }

    private void drawEvaluatedPosition() {
        final int w = 8, h = 8; // size of a letter
        for (GomokuAI.EvaluatedPosition evalpos : game.AI.evaluatedPos) {
            float radius = TILE_SIZE * 0.8f / 2f;
            Coords pos = evalpos.pos.add(-1, -1);
            int px = pos.x * TILE_SIZE;
            int py = pos.y * TILE_SIZE;
            gc.setFill(Color.rgb(0, 0, 0, 0.3f));
            gc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);

            String n = Integer.toString(evalpos.score);
            // if (Math.abs(evalpos.score) >= 10000) {
            //     n = Double.toString(Math.round(Math.log(Math.abs(evalpos.score))) * Math.signum(evalpos.score));
            // }
            gc.setFill(Color.WHITE);
            gc.fillText(n, px - w * n.length() / 2, py + h / 2);
        }
    }

    private void drawCurrentSearchDepth() {
        final int w = 8, h = 8; // size of a letter
        for (PosScore posScore : game.AI.getCurrentSearchDepth()) {
            float radius = TILE_SIZE * 0.8f / 2f;
            Coords pos = Coords.getCoordsById(posScore.index).add(0);
            int px = pos.x * TILE_SIZE;
            int py = pos.y * TILE_SIZE;
            renderLoopGc.setFill(Color.rgb(0, 0, 0, 0.3f));
            renderLoopGc.fillOval(px - radius, py - radius, radius * 2f, radius * 2f);

            String n = Integer.toString(posScore.score);
            renderLoopGc.setFill(Color.WHITE);
            renderLoopGc.fillText(n, px - w * n.length() / 2, py + h / 2);
        }
    }

    private void drawSortedPosition() {
        List<PosScore> sortedPos = game.boardAnalyser.getSortedPositions();
        // final int w = 8, h = 8; // size of a letter
        int i = 1;
        for (PosScore posScore : sortedPos) {
            // float radius = TILE_SIZE * 0.8f / 2f;
            Coords pos = Coords.getCoordsById(posScore.index, GameSettings.BOARD_SIZE).add(-1);
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

    private void drawScoreHeatmap() {
        int size = GameSettings.GAME_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                int score = game.boardAnalyser.getScoreAtPos(pos.add(1).getId());
                Color color = GameSettings.getHeatMapColor(score, 20);
                gc.setFill(color);
                gc.fillRect(col * TILE_SIZE - TILE_SIZE / 2, row * TILE_SIZE - TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawScoreNumber() {
        int size = GameSettings.GAME_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                int score = game.boardAnalyser.getScoreAtPos(pos.add(1).getId());
                if (score != 0)
                    drawNumberAt(pos, 0, 0, Math.abs(score), Color.ORANGE);
            }
        }
    }

    private void drawBucketScoreNumber() {
        for (int score = game.boardAnalyser.scoreBuckets.buckets.length - 1; score > 0; score--) {
            for (int index : game.boardAnalyser.scoreBuckets.buckets[score]){
                Coords pos = Coords.getCoordsById(index, GameSettings.BOARD_SIZE).add(-1);
                drawNumberAt(pos, 0, 2, Math.abs(score), Color.ORANGE);
            }
        }
    }

    private void drawScorePlayerNumber() {
        int size = GameSettings.GAME_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                int dirIndex = 0;
                while (dirIndex < 8) {
                    Coords dirCoords = Coords.DIRECTION8[7 - dirIndex].multiply(2);
                    int index = pos.add(1).getId();
                    int score = game.boardAnalyser.getScoreAtPosAtDir(index, dirIndex);
                    Color color = score > 0 ? GameSettings.PLAYER1_COLOR.darker() : GameSettings.PLAYER2_COLOR.darker();
                    // if (Math.abs(score) > 5) {
                    //     color = Color.RED;
                    // }
                    if (score != 0)
                        drawNumberAt(pos, dirCoords.x, dirCoords.y, score, color);
                    dirIndex++;
                }
            }
        }
    }

    private void drawNumberAt(Coords gridPos, float anchorx, float anchory, int number, Color color) {
        drawNumberAt(gc, gridPos, anchorx, anchory, number, color);
    }

    private void drawNumberAt(GraphicsContext gc, Coords gridPos, float anchorx, float anchory, int number, Color color) {
        drawNumberAt(gc, gridPos.x, gridPos.y, anchorx, anchory, number, color);
    }

    private void drawNumberAt(GraphicsContext gc, int x, int y, float anchorx, float anchory, int number, Color color) {
        final int w = 8, h = 10; // size of a letter
        String str = Integer.toString(number);
        gc.setFill(color);
        int halfHeight = h / 2;
        int halfWidth = w * str.length() / 2;
        gc.fillText(str, 
        x * TILE_SIZE - halfWidth + anchorx * halfWidth, 
        y * TILE_SIZE + halfHeight + anchory * halfHeight
        );
    }
    
    private void drawSymbols() {
        int size = GameSettings.GAME_SIZE;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Coords pos = new Coords(col, row);
                int index = pos.add(1).getId();
                int piece = game.board.getPieceAt(index);
                int px = col * TILE_SIZE;
                int py = row * TILE_SIZE;
                // if (cell.isDoubleFreeThree())
                //     UtilsRenderer.drawX(gc, px, py, (int)(TILE_SIZE * 0.4), 5, Color.RED);
                // else if (cell.isFreeThree())
                //     UtilsRenderer.drawPlus(gc, px, py, (int)(TILE_SIZE * 0.5), 5, Color.GREEN);
                if (game.board.canBeCapturedAt(index, game.board.getCurrentPlayer())) {
                    Color color;
                    if (game.board.endGameCapture.contains(index)) {
                        color = Color.RED;
                    } else {
                        color = getPieceColor(game.board.getOpponent(piece));
                    }
                    UtilsRenderer.drawMinus(gc, px, py, (int)(TILE_SIZE * 0.4), 5, color);
                }
                // if (cell.winning)
                // {
                //     UtilsRenderer.drawStarFull(gc, px, py, (int)(TILE_SIZE * 0.25), Color.YELLOW);
                //     UtilsRenderer.drawStarOutline(gc, px, py, (int)(TILE_SIZE * 0.25), 1, Color.BLACK);
                // }
            }
        }
    }

    private void highlightLastMove(){
        int[] lastMove = game.board.getLastMoves();
        if (lastMove == null) {
            return;
        }
        int lastMovesLength = lastMove[0];
        gc.setFill(GameSettings.BOARD_COLOR_INVERSE);
        int index = Math.abs(lastMove[lastMovesLength]); // +1 to skip first elem which is the length
        Coords pos = Coords.getCoordsById(index, GameSettings.BOARD_SIZE).add(-1);
        double len = TILE_SIZE / 5.;
        gc.fillRect(pos.x * TILE_SIZE - len / 2, pos.y * TILE_SIZE - len / 2, len, len);

    }

    private void drawCaptures(){
        int[] lastMove = game.board.getLastMoves();
        if (lastMove == null) {
            return;
        }
        int lastMovesLength = lastMove[0];
        int index = lastMove[lastMovesLength];
        Coords origin = Coords.getCoordsById(index, GameSettings.BOARD_SIZE).add(-1);
        for (int i = 1; i < lastMove[0]; i += 2) {
            index = Math.abs(lastMove[i]);
            Coords pos = Coords.getCoordsById(index, GameSettings.BOARD_SIZE).add(-1);
            Coords dir = Coords.getDirection(origin, pos);
            Coords dest = origin.add(dir.multiply(3));

            Coords p2 = origin.multiply(TILE_SIZE);
            Coords p1 = dest.multiply(TILE_SIZE);

            UtilsRenderer.drawArrow(gc, p1.x, p1.y, p2.x, p2.y, TILE_SIZE / 3, GameSettings.ARROW_COLOR, 30);
        }
    }

    public double mouseX = 0;
    public double mouseY = 0;

    public void drawOverlay() {
        overlayGc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        
        if (GameSettings.drawMousePos) drawMousePos(mouseX, mouseY);
        if (GameSettings.drawMouseGridPos) drawMouseGridPos();
        if (GameSettings.drawMouseCellPos) drawMouseCellPos();
        // if (GameSettings.drawSequenceDataOnMouse) drawSequenceDataOnMouse();
    }

    public void drawMousePos(double x, double y) {
        String strx = Integer.toString((int)x);
        String stry = Integer.toString((int)y);
        overlayGc.setFill(Color.WHITE);
        overlayGc.fillText("x: " + strx, x, y);
        overlayGc.fillText("y: " + stry, x, y - 16);
    }

    public void drawMouseGridPos() {
        Coords pos = pixelPosToCoords(mouseX, mouseY);
        String strx = Integer.toString((int)pos.x);
        String stry = Integer.toString((int)pos.y);
        overlayGc.setFill(Color.WHITE);
        overlayGc.fillText("x: " + strx, mouseX, mouseY - 32);
        overlayGc.fillText("y: " + stry, mouseX, mouseY - 48);
    }

    public void drawMouseCellPos() {
        Coords pos = pixelPosToCoords(mouseX, mouseY);
        overlayGc.setFill(Color.rgb(0, 125, 125, 0.2));
        overlayGc.fillRect(pos.x * TILE_SIZE + TILE_SIZE / 2, pos.y * TILE_SIZE + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
    }

    // public void drawSequenceDataOnMouse() {
    //     if (!isInBoardBound(mouseX, mouseY))
    //         return;
    //     Coords pos = pixelPosToCoords(mouseX, mouseY);
    //     BoardAnalyser.SequenceData data = game.boardAnalyser.new SequenceData();
    //     // System.out.println("drawSequenceDataOnMouse");
    //     for (Coords dir : Coords.DIRECTION8) {
    //         Coords temp = pos.add(dir);
    //         if (game.board.getPieceAt(temp.add(1).getId()) == -1) {
    //             data.reset();
    //         }
    //         else{
    //             game.boardAnalyser.pieceSequenceDataInDir(temp.add(1).getId(), dir.getId(), data);
    //         }

    //         overlayGc.setFill(Color.rgb(0, 125, 0, 0.5));
    //         for (int i = 0; i < data.leadSpaceNumber; i++) {
    //             drawCenterTileAt(overlayGc, temp);
    //             temp.addTo(dir);
    //         }
    //         overlayGc.setFill(getPieceColor(data.player, 0.5));
    //         for (int i = 0; i < data.pieceNumber; i++) {
    //             drawCenterTileAt(overlayGc, temp);
    //             temp.addTo(dir);
    //         }
    //         overlayGc.setFill(Color.rgb(0, 125, 0, 0.5));
    //         for (int i = 0; i < data.trailSpaceNumber; i++) {
    //             drawCenterTileAt(overlayGc, temp);
    //             temp.addTo(dir);
    //         }
    //         overlayGc.setFill(Color.rgb(125, 0, 0, 0.5));
    //         if (data.trailPiece == 1 || data.trailPiece == 2)
    //             overlayGc.setFill(getPieceColor(data.trailPiece, 0.5));
    //         drawCenterTileAt(overlayGc, temp);
    //     }
    // }

    public void drawTileAt(GraphicsContext g, Coords pos) {
        g.fillRect(pos.x * TILE_SIZE, pos.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    public void drawCenterTileAt(GraphicsContext g, Coords pos) {
        g.fillRect(pos.x * TILE_SIZE + TILE_SIZE / 2, pos.y * TILE_SIZE + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE);
    }

    /* Utils */

    public Color getPieceColor(int player) {
        if (player == -1) {
            return Color.GRAY;
        }
        if (player != 1 && player != 2) {
            return Color.PURPLE;
        }
        return playerColor[player - 1];
    }

    public Color getPieceColor(int player, double alpha) {
        Color color = getPieceColor(player);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public Coords pixelPosToCoords(double x, double y) {
        int row = (int)((y - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        int col = (int)((x - MARGIN + TILE_SIZE / 2) / TILE_SIZE);
        return new Coords(col, row);
    }

    public boolean isInBoardBound(double x, double y) {
        return x >= MARGIN / 2 && y >= MARGIN / 2 && x < (canvasWidth - MARGIN / 2) && y < (canvasHeight - MARGIN / 2);
    }

    public boolean isInGridBound(double x, double y) {
        return x >= MARGIN && y >= MARGIN && x < (canvasWidth - MARGIN) && y < (canvasHeight - MARGIN);
    }

}
