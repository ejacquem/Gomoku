package main.java.ui;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

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

    public static void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2, double thickness, Color color, double arrowHeadSize) {
        gc.setStroke(color);
        gc.setFill(color);
        gc.setLineWidth(thickness);
        gc.setLineCap(StrokeLineCap.BUTT);

        // double percent = .86;
        // gc.strokeLine(x1, y1, x1 + (x2 - x1) * percent, y1 + (y2 - y1) * percent);

        // Arrow direction
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowAngle = Math.toRadians(30);

        // Calculate points for arrowhead
        double x3 = x2 - arrowHeadSize * Math.cos(angle - arrowAngle);
        double y3 = y2 - arrowHeadSize * Math.sin(angle - arrowAngle);

        double x4 = x2 - arrowHeadSize * Math.cos(angle + arrowAngle);
        double y4 = y2 - arrowHeadSize * Math.sin(angle + arrowAngle);

        gc.strokeLine(x1, y1, (x3 + x4) / 2., (y3 + y4) / 2.);

        // Draw arrowhead lines
        // gc.strokeLine(x2, y2, x3, y3);
        // gc.strokeLine(x2, y2, x4, y4);
        gc.fillPolygon(new double[]{x2, x3, x4}, new double[]{y2, y3, y4}, 3);
    }
}
