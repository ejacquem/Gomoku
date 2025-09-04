package main.java;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class UtilsRenderer {

    public static double[][] calculateStarPoints(int centerX, int centerY, int radius) {
        double[] xPoints = new double[10];
        double[] yPoints = new double[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? radius : radius / 2;
            xPoints[i] = centerX + r * Math.cos(angle);
            yPoints[i] = centerY - r * Math.sin(angle);
        }
        return new double[][] { xPoints, yPoints };
    }

    public static void drawStarOutline(GraphicsContext gc, int centerX, int centerY, int radius, int thickness, Color color) {
        double[][] points = calculateStarPoints(centerX, centerY, radius);
        gc.setLineWidth(thickness);
        gc.setStroke(color);
        gc.strokePolygon(points[0], points[1], 10);
    }

    public static void drawStarFull(GraphicsContext gc, int centerX, int centerY, int radius, Color color) {
        double[][] points = calculateStarPoints(centerX, centerY, radius);
        gc.setFill(color);
        gc.fillPolygon(points[0], points[1], 10);
    }

    public static void drawPlus(GraphicsContext gc, int centerX, int centerY, int size, int thickness, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(thickness);
        int half = size / 2;
        gc.strokeLine(centerX - half, centerY, centerX + half, centerY);
        gc.strokeLine(centerX, centerY - half, centerX, centerY + half);
    }

    public static void drawX(GraphicsContext gc, int centerX, int centerY, int size, int thickness, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(thickness);
        int half = size / 2;
        gc.strokeLine(centerX - half, centerY - half, centerX + half, centerY + half);
        gc.strokeLine(centerX - half, centerY + half, centerX + half, centerY - half);
    }

    public static void drawMinus(GraphicsContext gc, int centerX, int centerY, int size, int thickness, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(thickness);
        int half = size / 2;
        gc.strokeLine(centerX - half, centerY, centerX + half, centerY);
        // gc.strokeLine(centerX, centerY - half, centerX, centerY + half);
    }
}
