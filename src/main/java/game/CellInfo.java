package main.java.game;

public class CellInfo{
    public Coords pos;
    private float[] playerScore = new float[]{0, 0};

    public CellInfo(Coords pos){
        this.pos = pos.clone();
    }

    public CellInfo(Coords pos, float score1, float score2){
        this.pos = pos;
        this.playerScore[0] = score1;
        this.playerScore[1] = score2;
    }

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

    public CellInfo clone(){
        return new CellInfo(pos.clone(), playerScore[0], playerScore[1]);
    }
}