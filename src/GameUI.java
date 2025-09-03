import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Alert;
import javafx.scene.Node;

public class GameUI {
    private VBox rightPanel; // contains info about the game, reset button, winner, player turn
    private VBox leftPanel; // contains player1 game info, canvas, player2 game info
    private HBox root;
    private HBox player1Panel;
    private HBox player2Panel;
    private Label infoLabel, playerLabel, winnerLabel;
    private Button restartButton;
    private Canvas canvas;
    private BoardRenderer renderer;
    private BoardGame game;
    private Background background;
    private FlowPane capturedPlayer1;
    private FlowPane capturedPlayer2;

    public GameUI(BoardGame game) {
        this.game = game;
        infoLabel = new Label("Game Info");
        playerLabel = new Label("Player: ");
        winnerLabel = new Label("");
        infoLabel.setTextFill(Color.WHITE);
        playerLabel.setTextFill(Color.WHITE);
        winnerLabel.setTextFill(Color.WHITE);
        restartButton = new Button("Restart");
        capturedPlayer1 = new FlowPane();
        capturedPlayer2 = new FlowPane();
        
        background = new Background(new BackgroundFill(GameSettings.UI_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY));
        
        canvas = new Canvas(
            (game.getBoardSize() - 1) * BoardRenderer.TILE_SIZE + BoardRenderer.MARGIN * 2,
            (game.getBoardSize() - 1) * BoardRenderer.TILE_SIZE + BoardRenderer.MARGIN * 2);
            
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleClick(e));
        
        Rectangle clip = new Rectangle(canvas.getWidth(), canvas.getHeight());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        canvas.setClip(clip);

        renderer = new BoardRenderer(canvas, game);
        player1Panel = createPlayerPanel("Player 1", GameSettings.PLAYER1_COLOR, GameSettings.PLAYER2_COLOR, capturedPlayer1);
        player2Panel = createPlayerPanel("Player 2", GameSettings.PLAYER2_COLOR, GameSettings.PLAYER1_COLOR, capturedPlayer2);
        rightPanel = new VBox(10, infoLabel, restartButton, playerLabel, winnerLabel);
        leftPanel = new VBox(0, player1Panel, canvas, player2Panel);
        root = new HBox(10, leftPanel, rightPanel);
        
        rightPanel.setMinWidth(300);
        player1Panel.setMinHeight(50);
        player2Panel.setMinHeight(50);
        root.setBackground(background);

        HBox.setMargin(leftPanel, new Insets(0, 0, 0, 10)); // top right bot left

        bindStuff();
    }

    private HBox createPlayerPanel(String name, Color avatarColor, Color capturedColor, FlowPane capturedPane) {
        StackPane avatarBox = createAvatar(50, 18, avatarColor, Color.web("#403e3b"));

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.WHITE);

        VBox.setVgrow(capturedPane, Priority.ALWAYS);
        capturedPane.setAlignment(Pos.BOTTOM_LEFT);
        capturedPane.setHgap(4); // spacing between captured pieces

        for (int i = 0; i < GameSettings.WINNING_CAPTURED_PIECES; i++) {
            Circle piece = new Circle(6, capturedColor); // small circle for captured piece
            piece.setStroke(GameSettings.PIECE_BORDER_COLOR); // border color
            piece.setStrokeWidth(1); // border thickness
            piece.setVisible(false); // hidden initially
            capturedPane.getChildren().add(piece);
        }

        // Vertical box with name and captured pieces
        VBox rightBox = new VBox(4, nameLabel, capturedPane);
        rightBox.setAlignment(Pos.TOP_LEFT);

        // Main panel (avatar on left, rightBox on right)
        HBox panel = new HBox(10, avatarBox, rightBox);
        panel.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(nameLabel, new Insets(5, 0, 0, 0));
        VBox.setMargin(capturedPane, new Insets(0, 0, 5, 0));
        VBox.setMargin(panel, new Insets(10, 10, 10, 0)); // top right bot left

        return panel;
    }

    private StackPane createAvatar(int squareRadius, int circleRadius, Color circleColor, Color boxColor) {
        Circle circle = new Circle(circleRadius, circleColor); // avatar

        circle.setStroke(GameSettings.PIECE_BORDER_COLOR);     // border color
        circle.setStrokeWidth(2);        // border thickness
        circle.setVisible(true); // hidden initially

        StackPane box = new StackPane(circle);
        box.setPrefSize(squareRadius, squareRadius);

        box.setBackground(new Background(
            new BackgroundFill(boxColor, new CornerRadii(5), Insets.EMPTY) // rounded corners
        ));
        box.setBorder(new Border(new BorderStroke(
            Color.web("#494745"), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1)
        )));

        return box;
    }

    private void bindStuff()
    {
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

        game.winnerProperty().addListener((obs, oldVal, newVal) -> {
            renderer.draw();
            if (newVal.intValue() != 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("Player " + newVal.intValue() + " is the Winner !!!");
                alert.showAndWait();
            }
        });

        game.player1Property().addListener((obs, oldVal, newVal) -> {
            updateCapturedPieces(newVal.intValue(), capturedPlayer1);
        });
        game.player2Property().addListener((obs, oldVal, newVal) -> {
            updateCapturedPieces(newVal.intValue(), capturedPlayer2);
        });
    }

    private void updateCapturedPieces(int n, FlowPane pane){
        for (Node node : pane.getChildren()) {
            node.setVisible(n > 0);
            n--;
        }
    }

    // private void setPlayerText(){
    //     playerLabel.setText(null);
    // }

    public void update() {
        renderer.draw();
    }

    public VBox getPanel() {
        return rightPanel;
    }

    public HBox getRoot() {
        return root;
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
