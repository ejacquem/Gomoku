package main.java;
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
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;

public class GameUI {
    private VBox rightPanel; // contains info about the game, reset button, winner, player turn
    private VBox leftPanel; // contains player1 game info, canvas, player2 game info
    private HBox root;
    private HBox player1Panel;
    private HBox player2Panel;
    private Label infoLabel, playerLabel, winnerLabel;
    private Button restartButton, startButton, randomButton, undoButton;
    private Background background;
    private VBox titlePane;
    private StackPane evalBar;

    private Font customFont = Font.loadFont("file:src/main/resources/AttackOfMonster.ttf", 60); 
    private Font robotoFont = Font.loadFont("file:src/main/resources/Roboto.ttf", 20);

    private Canvas canvas;
    private BoardRenderer renderer;
    private BoardGame game;
    private GomokuAI AI;

    public class PlayerData {
        public final int id;
        public final String name;
        public final Color avatarColor;
        public final FlowPane capturedPane;
        public final LongProperty timerProperty;
        public final IntegerProperty pointsProperty;

        public PlayerData(int id, String name, Color avatarColor, LongProperty timerProperty, IntegerProperty pointsProperty) {
            this.id = id;
            this.name = name;
            this.avatarColor = avatarColor;
            this.capturedPane = new FlowPane();;
            this.timerProperty = timerProperty;
            this.pointsProperty = pointsProperty;
        }
    }
    private PlayerData data1;
    private PlayerData data2;


    public GameUI(BoardGame game, GomokuAI AI) {
        this.game = game;
        this.AI = AI;

        titlePane = createTitlePane();

        infoLabel = creatLabel("Game Info", robotoFont, Color.WHITE);
        playerLabel = creatLabel("Player: ", robotoFont, Color.WHITE);
        winnerLabel = creatLabel("", robotoFont, Color.WHITE);
        startButton = new Button("Start");
        restartButton = new Button("Restart");
        randomButton = new Button("Random");
        undoButton = new Button("Undo");
        restartButton.getStyleClass().addAll("button-base", "simple-button");
        startButton.getStyleClass().addAll("button-base", "simple-button");
        randomButton.getStyleClass().addAll("button-base", "simple-button");
        undoButton.getStyleClass().addAll("button-base", "undo-button");

        // Button button = new Button("Click Me");
    
        data1 = new PlayerData(1, "Player 1", GameSettings.PLAYER1_COLOR, game.player1TimerProperty(), game.player1CapturedPiecesProperty());
        data2 = new PlayerData(2, "Player 2", GameSettings.PLAYER2_COLOR, game.player2TimerProperty(), game.player2CapturedPiecesProperty());
        
        background = new Background(new BackgroundFill(GameSettings.UI_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY));
        
        canvas = new Canvas(
            (game.getBoardSize() - 1) * BoardRenderer.TILE_SIZE + BoardRenderer.MARGIN * 2,
            (game.getBoardSize() - 1) * BoardRenderer.TILE_SIZE + BoardRenderer.MARGIN * 2);
            
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleClick(e));
        
        Rectangle clip = new Rectangle(canvas.getWidth(), canvas.getHeight());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        canvas.setClip(clip);

        evalBar = createEvaluationBar(20);
        setBarPercentage(evalBar, 0.5);

        renderer = new BoardRenderer(canvas, game);
        player1Panel = createPlayerPanel(data1, GameSettings.PLAYER2_COLOR);
        player2Panel = createPlayerPanel(data2, GameSettings.PLAYER1_COLOR);
        rightPanel = new VBox(15, titlePane, infoLabel, restartButton, startButton, randomButton, undoButton, playerLabel, winnerLabel);
        leftPanel = new VBox(0, player1Panel, canvas, player2Panel);
        root = new HBox(0, evalBar, leftPanel, rightPanel);
        
        rightPanel.setMinWidth(300);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        player1Panel.setMinHeight(50);
        player2Panel.setMinHeight(50);
        root.setBackground(background);

        root.setAlignment(Pos.CENTER);

        HBox.setMargin(evalBar, new Insets(0, 10, 0, 10)); // top right bot left
        HBox.setMargin(rightPanel, new Insets(10, 10, 10, 10)); // top right bot left

        bindStuff();
    }

    private Label creatLabel(String text, Font font, Color color){
        Label label;
        label = new Label(text);
        label.setFont(font);
        label.setTextFill(color);
        return label;
    }

    private VBox createTitlePane()
    {
        Label title = new Label("Gomoku");
        title.setTextFill(Color.WHITE);
        title.setFont(customFont);

        Line underline = new Line();
        underline.setStartX(0);
        underline.setEndX(300); // width of the line
        underline.setStroke(Color.WHITE);
        underline.setStrokeWidth(3);
        
        VBox titleBox = new VBox(title, underline);
        titleBox.setAlignment(Pos.CENTER); // center title + line
        titleBox.setSpacing(5); // space between title and line
        return titleBox;
    }

    private HBox createPlayerPanel(PlayerData data, Color capturedColor) {
        StackPane avatarBox = createAvatar(50, 18, data.avatarColor, Color.web("#403e3b"));

        Label nameLabel = new Label(data.name);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font(robotoFont.getFamily(), FontWeight.NORMAL, 16));

        VBox.setVgrow(data.capturedPane, Priority.ALWAYS);
        data.capturedPane.setAlignment(Pos.BOTTOM_LEFT);
        data.capturedPane.setHgap(4); // spacing between captured pieces

        for (int i = 0; i < GameSettings.WINNING_CAPTURED_PIECES; i++) {
            Circle piece = new Circle(6, capturedColor); // small circle for captured piece
            piece.setStroke(GameSettings.PIECE_BORDER_COLOR); // border color
            piece.setStrokeWidth(1); // border thickness
            piece.setVisible(false); // hidden initially
            data.capturedPane.getChildren().add(piece);
        }

        // Vertical box with name and captured pieces
        VBox rightBox = new VBox(4, nameLabel, data.capturedPane);
        rightBox.setAlignment(Pos.TOP_LEFT);

        // player timer
        HBox clockPanel = createClockPanel(data);

        // Main panel (avatar on left, rightBox on right)
        HBox panel = new HBox(0, avatarBox, rightBox, clockPanel);
        // panel.setBackground(createBackground(Color.PINK));
        panel.setAlignment(Pos.CENTER);
        VBox.setMargin(nameLabel, new Insets(5, 0, 0, 0));
        VBox.setMargin(data.capturedPane, new Insets(0, 0, 5, 0));
        VBox.setMargin(panel, new Insets(10, 10, 10, 0)); // top right bot left
        HBox.setMargin(avatarBox, new Insets(0, 10, 0, 0));

        data.pointsProperty.addListener((obs, oldVal, newVal) -> {
            updateCapturedPieces(newVal.intValue(), data.capturedPane);
        });
        // HBox.setHgrow(panel, Priority.ALWAYS);
        // panel.setMaxWidth(Double.MAX_VALUE);

        return panel;
    }

    private HBox createClockPanel(PlayerData data){
        Label clockLabel = new Label("0:00.0");
        HBox clockPanel = new HBox(clockLabel);
        // clockLabel.setBackground(createBackground(Color.BLACK));
        clockLabel.setTextFill(Color.WHITESMOKE);
        clockLabel.setFont(Font.font(robotoFont.getFamily(), FontWeight.EXTRA_BOLD, 30));
        HBox.setHgrow(clockPanel, Priority.ALWAYS);
        clockPanel.setMaxWidth(Double.MAX_VALUE);
        clockPanel.setAlignment(Pos.CENTER_RIGHT);

        clockLabel.setBackground(new Background(new BackgroundFill(
            Color.web("#403e3b"),                // fill color
            new CornerRadii(3),         // rounded corners
            Insets.EMPTY                 // no internal padding (optional)
        )));
        clockLabel.setPadding(new Insets(2, 8, 2, 8));

        clockLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                long millis = data.timerProperty.get();
                long totalSeconds = millis / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                long tenth = (millis / 100) % 10;
                return String.format("%d:%02d.%d", minutes, seconds, tenth);
            }, data.timerProperty)
        );

        game.currentPlayerProperty().addListener((obs, oldVal, newVal) -> {
            // Example: highlight if it's this player's turn
            if (newVal.intValue() == data.id) {
                clockLabel.setTextFill(Color.WHITESMOKE); // active player
            } else {
                clockLabel.setTextFill(Color.GRAY); // inactive player
            }
        });

        return clockPanel;
    }

    private StackPane createEvaluationBar(double width) {
        StackPane bar = new StackPane();
        double height = canvas.getHeight();
        bar.setPrefSize(width, height);
        bar.setMaxHeight(height);
        bar.setBackground(createBackground(GameSettings.PLAYER1_COLOR));

        // bar.setBackground(new Background(
        //     new BackgroundFill(GameSettings.PLAYER1_COLOR, new CornerRadii(2), Insets.EMPTY) // rounded corners
        // ));
    
        Rectangle fill = new Rectangle(width, 50); // start at 0
        fill.setFill(GameSettings.PLAYER2_COLOR);
        StackPane.setAlignment(fill, Pos.BOTTOM_CENTER);
    
        bar.getChildren().add(fill);
        bar.setUserData(fill);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(5);
        clip.setArcHeight(5);
        bar.setClip(clip);
    
        return bar;
    }
    
    private void setBarPercentage(StackPane bar, double percentage) {
        Rectangle fill = (Rectangle) bar.getUserData();
        double barHeight = bar.getPrefHeight();
        fill.setHeight(Math.round(barHeight * percentage));
    }

    private Background createBackground(Color color){
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
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
            game.reset();
            renderer.draw();
        });

        startButton.setOnAction(e -> {
            game.startGame();
            renderer.draw();
        });

        randomButton.setOnAction(e -> {
            game.startGame();
            game.randomBoard(.3f);
            AI.evaluate();
            game.checkBoard();
            renderer.draw();
        });

        undoButton.setOnAction(e -> {
            game.undo();
            game.checkBoard();
            renderer.draw();
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

        AI.percentageProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("percentage score: " + newVal);
            setBarPercentage(evalBar, (double)newVal);
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
