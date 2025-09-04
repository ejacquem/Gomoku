package main.java;

public class Cell {
    public int player = 0;
    public boolean winning = false;
    public boolean invalid_move = false;
    public boolean can_be_captured = false;
    // true if a piece can be placed and it creates a free-three
    public boolean can_be_free3_h = false; // horizontal
    public boolean can_be_free3_v = false; // vertical
    public boolean can_be_free3_p = false; // positive
    public boolean can_be_free3_n = false; // negative

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
        return player != 0;
    }

    public void reset(){
        player = 0;
        winning = false;
        invalid_move = false;
        can_be_captured = false;
        resetFreeThree();
    }

    public void resetFreeThree(){
        can_be_free3_h = false;
        can_be_free3_v = false;
        can_be_free3_p = false;
        can_be_free3_n = false;
    }
}
