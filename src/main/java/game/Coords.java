package main.java.game;

import main.java.app.GameSettings;

public class Coords {
    public int x;
    public int y;
    
    // Constructors
    public Coords() {
        this.x = 0;
        this.y = 0;
    }
    
    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Coords(Coords other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    // Addition operations
    public Coords add(Coords other) {
        return new Coords(this.x + other.x, this.y + other.y);
    }
    
    public Coords add(int x, int y) {
        return new Coords(this.x + x, this.y + y);
    }

    public Coords add(int val) {
        return new Coords(this.x + val, this.y + val);
    }
    
    public void addTo(Coords other) {
        this.x += other.x;
        this.y += other.y;
    }
    
    public void addTo(int x, int y) {
        this.x += x;
        this.y += y;
    }
    
    // Subtraction operations
    public Coords subtract(Coords other) {
        return new Coords(this.x - other.x, this.y - other.y);
    }
    
    public Coords subtract(int x, int y) {
        return new Coords(this.x - x, this.y - y);
    }
    
    public void subtractFrom(Coords other) {
        this.x -= other.x;
        this.y -= other.y;
    }
    
    public void subtractFrom(int x, int y) {
        this.x -= x;
        this.y -= y;
    }
    
    // Multiplication operations
    public Coords multiply(int scalar) {
        return new Coords(this.x * scalar, this.y * scalar);
    }
    
    public Coords multiply(Coords other) {
        return new Coords(this.x * other.x, this.y * other.y);
    }
    
    public void multiplyBy(int scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }
    
    public void multiplyBy(Coords other) {
        this.x *= other.x;
        this.y *= other.y;
    }
    
    // Division operations
    public Coords divide(int scalar) {
        if (scalar == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Coords(this.x / scalar, this.y / scalar);
    }
    
    public Coords divide(Coords other) {
        if (other.x == 0 || other.y == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Coords(this.x / other.x, this.y / other.y);
    }
    
    public void divideBy(int scalar) {
        if (scalar == 0) {
            throw new ArithmeticException("Division by zero");
        }
        this.x /= scalar;
        this.y /= scalar;
    }
    
    // Distance calculations
    public double distanceTo(Coords other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public int manhattanDistance(Coords other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    // Utility methods
    public Coords negate() {
        return new Coords(-this.x, -this.y);
    }
    
    public Coords abs() {
        return new Coords(Math.abs(this.x), Math.abs(this.y));
    }
    
    public int dot(Coords other) {
        return this.x * other.x + this.y * other.y;
    }
    
    public boolean isOrigin() {
        return x == 0 && y == 0;
    }
    
    public Coords[] getNeighbors4() {
        return new Coords[] {
            new Coords(x, y - 1), // North
            new Coords(x + 1, y), // East
            new Coords(x, y + 1), // South
            new Coords(x - 1, y)  // West
        };
    }
    
    public Coords[] getNeighbors8() {
        return new Coords[] {
            new Coords(x - 1, y - 1), new Coords(x, y - 1), new Coords(x + 1, y - 1),
            new Coords(x - 1, y),                            new Coords(x + 1, y),
            new Coords(x - 1, y + 1), new Coords(x, y + 1), new Coords(x + 1, y + 1)
        };
    }
    
    // Object methods
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coords coords = (Coords) obj;
        return x == coords.x && y == coords.y;
    }
    
    @Override
    public int hashCode() {
        return x + 31 * y;
    }

    public int getId() {
        return x + GameSettings.BOARD_SIZE * y;
    }

    public static Coords getCoordsById(int id) {
        return new Coords(id % GameSettings.BOARD_SIZE, id / GameSettings.BOARD_SIZE);
    }
    
    public int getId(int gridSize) {
        return x + gridSize * y;
    }

    public static Coords getCoordsById(int id, int gridSize) {
        return new Coords(id % gridSize, id / gridSize);
    }

    // only works for cardinal direction
    public static Coords getDirection(Coords a, Coords b){
        return b.subtract(a).divide((int)a.distanceTo(b));
    }

    // Static factory methods
    public static Coords origin() {
        return new Coords(0, 0);
    }
    
    public static Coords of(int x, int y) {
        return new Coords(x, y);
    }

    // 0 1 2              7 6 5
    // 3   4  opposite -> 4   3
    // 5 6 7              2 1 0
    public static int oppositeDirIndex(int dirIndex) { return 7 - dirIndex; }

    public static final Coords NW = new Coords(-1, -1);
    public static final Coords N  = new Coords(0, -1);
    public static final Coords NE = new Coords(1, -1);
    public static final Coords W  = new Coords(-1, 0);
    public static final Coords E  = new Coords(1, 0);
    public static final Coords SW = new Coords(-1, 1);
    public static final Coords S  = new Coords(0, 1);
    public static final Coords SE = new Coords(1, 1);
    
    public static final Coords[] DIRECTION8 = {
        NW, N, NE, W, E, SW, S, SE
    };
    
    // 4 directions
    public static final Coords[] DIRECTION4 = {
        E, S, SE, NE
    };
    
}