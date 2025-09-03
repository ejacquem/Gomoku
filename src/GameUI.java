import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class GameUI {
    private VBox panel;
    private Label infoLabel, playerLabel, winnerLabel;
    private Button restartButton;
    private Canvas canvas;
    private BoardRenderer renderer;

    public GameUI(BoardGame game) {
        infoLabel = new Label("Game Info");
        playerLabel = new Label("Player: ");
        winnerLabel = new Label("");
        restartButton = new Button("Restart");

        canvas = new Canvas(
            (game.getBoardSize() - 1) * BoardRenderer.TILE_SIZE + BoardRenderer.MARGIN * 2,
            (game.getBoardSize() - 1) * BoardRenderer.TILE_SIZE + BoardRenderer.MARGIN * 2);

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleClick(e));

        renderer = new BoardRenderer(canvas, game);

        // Restart button action
        restartButton.setOnAction(e -> {
            game.reset();     // implement reset() in BoardGame
            renderer.draw();  // redraw board
        });

        playerLabel.textProperty().bind(
            game.currentPlayerProperty().asString("Player %d's turn")
        );

        winnerLabel.textProperty().bind(
            game.winnerProperty().asString("Winner is player %d")
        );

        panel = new VBox(10, infoLabel, restartButton, playerLabel, winnerLabel);
        panel.setMinWidth(150);
    }

    // private void setPlayerText(){
    //     playerLabel.setText(null);
    // }

    public void update() {
        renderer.draw();
    }

    public VBox getPanel() {
        return panel;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setInfoText(String text) {
        infoLabel.setText(text);
    }

    private void handleClick(MouseEvent e) {
        // setPlayerText();
    }
}
