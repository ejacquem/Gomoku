// src/GameApp.java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;

public class GameApp extends Application {

    @Override
    public void start(Stage stage) {
        BoardGame game = new BoardGame();

        GameUI gameUI = new GameUI(game);

        // Main layout: board on left, info panel on right
        HBox root = new HBox(10, gameUI.getCanvas(), gameUI.getPanel());
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Board Game Template");
        stage.show();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            }
        });

        gameUI.update();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
