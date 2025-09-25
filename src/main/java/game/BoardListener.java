package main.java.game;

public interface BoardListener {
    void onMovePlaced();
    void onUndo();
    void onRedo();
    void onGoto();
    void onReset();
}