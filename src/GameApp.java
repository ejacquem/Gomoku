// src/GameApp.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;

public class GameApp extends Application {

    @Override
    public void start(Stage stage) {
        BoardGame game = new BoardGame();

        GameUI gameUI = new GameUI(game);

        Scene scene = new Scene(gameUI.getRoot());

        setupScene(scene);
    
        stage.setScene(scene);
        stage.setTitle("Board Game Template");
        stage.show();

        gameUI.update();
    }

    private void setupScene(Scene scene)
    {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
