package main.java.ui;

import main.java.GameSettings;
import main.java.game.Board;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.swing.Renderer;

import javafx.scene.control.*;

public class GameUISettings{

    private GameUI gameUI;
    private BoardRenderer renderer;

    public GameUISettings(GameUI gameUI) {
        this.gameUI = gameUI;
        renderer = this.gameUI.getRenderer();
        MenuBar menuBar = new MenuBar();
        Menu settingsMenu = new Menu("Settings");

        settingsMenu.getItems().addAll(
            createSettingItem("Draw Debug Number",      () -> GameSettings.drawDebugNumber,      val -> GameSettings.drawDebugNumber = val),
            createSettingItem("Draw Best Move",         () -> GameSettings.drawBestMove,         val -> GameSettings.drawBestMove = val),
            createSettingItem("Draw Neighbour",         () -> GameSettings.drawNeighbour,        val -> GameSettings.drawNeighbour = val),
            createSettingItem("Draw Evaluated Position",() -> GameSettings.drawEvaluatedPosition,val -> GameSettings.drawEvaluatedPosition = val),
            createSettingItem("Draw Heatmap Neighbour", () -> GameSettings.drawHeatmapNeighbour, val -> GameSettings.drawHeatmapNeighbour = val),
            createSettingItem("Draw Heatmap Score",     () -> GameSettings.drawHeatmapScore,     val -> GameSettings.drawHeatmapScore = val)
        );

        menuBar.getMenus().add(settingsMenu);

        // BorderPane root = new BorderPane();
        gameUI.getRoot().setTop(menuBar);
    }

    private CheckMenuItem createSettingItem(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        CheckMenuItem item = new CheckMenuItem(label);
        item.setSelected(getter.getAsBoolean());
        item.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setter.accept(newVal);
            renderer.draw();
        });
        return item;
    }

}
