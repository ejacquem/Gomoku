package main.java.game;

public class Cell {
    public int player = 0;
    public boolean winning = false;
    public boolean can_be_captured = false;
    // true if a piece can be placed and it creates a free-three
    public boolean can_be_free3_h = false; // horizontal
    public boolean can_be_free3_v = false; // vertical
    public boolean can_be_free3_p = false; // positive
    public boolean can_be_free3_n = false; // negative
    
    private int neighbours; // count of the eight neibours

    public boolean isNeighbour(){
        return neighbours != 0;
    }

    public void setNeighbour(Coords pos, int bit){
        setNeighbour(pos.getId(3), bit);
    }

    public void setNeighbour(int index, int bit){
        neighbours = ((neighbours & ~(1 << index)) | (bit << index));
        neighbours |= (bit << index);
    }

    public int getNeighbourNumber(){
        int count = 0;
        for (int i = 0; i < 9; i++){
            count += (neighbours >> i) & 1;
        }
        return count;
    }

    Cell(){
        
    }

    Cell(int player){
        this.player = player;
    }

    public boolean isDoubleFreeThree() {
        int count = 0;
        if (can_be_free3_h) count++;
        if (can_be_free3_v) count++;
        if (can_be_free3_p) count++;
        if (can_be_free3_n) count++;
        return count >= 2;
    }

    public boolean isFreeThree() {
        return can_be_free3_h || can_be_free3_v || can_be_free3_p || can_be_free3_n ;
    }

    public boolean has_piece(){
        return player == 1 || player == 2;
    }

    public boolean empty(){
        return player == 0;
    }

    public void reset(){
        player = 0;
        winning = false;
        can_be_captured = false;
        neighbours = 0;
        resetFreeThree();
    }

    public void resetFreeThree(){
        can_be_free3_h = false;
        can_be_free3_v = false;
        can_be_free3_p = false;
        can_be_free3_n = false;
    }

    // H -> 1, 0 || -1, 0
    // V -> 0, 1 || 0, 1
    // P -> 1, -1 || -1, 1
    // N -> 1, 1 || -1, -1
    public void setFreeThree(Coords dir){
        if (dir.y == 0) can_be_free3_h = true;
        else if (dir.x == 0) can_be_free3_v = true;
        else if (dir.x == dir.y) can_be_free3_n = true;
        else can_be_free3_p = true;
    }
}
