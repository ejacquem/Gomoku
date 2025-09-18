package main.java.game;

import java.util.Comparator;

public class CellInfo{
    public Coords pos;
    private float[] playerScore = new float[]{0, 0};

    public CellInfo(){}

    public void setScore(float score, int player) {
        playerScore[player - 1] = score;
    }

    public void addScore(float score, int player) {
        playerScore[player - 1] += score;
    }

    public float getPlayerScore(int player) {
        return playerScore[player - 1];
    }

    public float getScore(){
        return playerScore[0] + playerScore[1];
    }

    public void addTo(CellInfo other) {
        this.playerScore[0] += other.playerScore[0];
        this.playerScore[1] += other.playerScore[1];
    }

    public static final Comparator<CellInfo> SCORE_COMPARATOR =
        (a, b) -> Float.compare(b.getScore(), a.getScore());
}