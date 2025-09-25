package main.java.app;

import javafx.application.Application;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.java.game.Board;
import main.java.game.BoardAnalyser;
import main.java.game.BoardGame;
import main.java.game.GomokuAI;
import main.java.ui.GameUI;
import main.java.ui.GameUISettings;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;

public class GameApp extends Application {

    private ImageCursor cursorLight;
    private ImageCursor cursorDark;

    @Override
    public void start(Stage stage) {
        Board board = new Board();
        BoardAnalyser boardAnalyser = new BoardAnalyser(board);
        GomokuAI AI = new GomokuAI();
        BoardGame game = new BoardGame(board, boardAnalyser, AI);

        GameUI gameUI = new GameUI(game, AI);
        new GameUISettings(gameUI, stage);

        Scene scene = new Scene(gameUI.getRoot());

        setupScene(scene);
        setupCursor(scene, game);
    
        stage.setScene(scene);
        stage.setTitle("Board Game Template");
        stage.show();

        gameUI.update();
    }

    private void setupCursor(Scene scene, BoardGame game) {
        Image cursorImageLight = new Image("file:src/main/resources/CursorLight96_32.png", 96, 96, true, false);
        Image cursorImageDark = new Image("file:src/main/resources/CursorDark96_32.png", 96, 96, true, false);
    
        cursorLight = new ImageCursor(cursorImageLight);
        cursorDark = new ImageCursor(cursorImageDark);
    
        scene.setCursor(cursorLight);
    
        game.currentPlayerProperty().addListener((obs, oldPlayer, newPlayer) -> {
            if ((int)newPlayer == 1) {
                scene.setCursor(cursorLight);
            } else {
                scene.setCursor(cursorDark);
            }
        });
    }

    private void setupScene(Scene scene)
    {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            }
        });

        scene.getStylesheets().add("file:src/main/resources/style.css");

    }

    public static void main(String[] args) {
        launch(args);
    }
}
