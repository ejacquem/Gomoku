package main.java.ui;

import main.java.GameSettings;
import main.java.game.Board;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.swing.Renderer;

import javafx.scene.control.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class GameUISettings {

    private final GameUI gameUI;
    private final BoardRenderer renderer;

    public GameUISettings(GameUI gameUI) {
        this.gameUI = gameUI;
        this.renderer = this.gameUI.getRenderer();

        Menu settingsMenu = new Menu("Settings");

        settingsMenu.getItems().addAll(
            createSettingItem("Draw Debug Number",       () -> GameSettings.drawDebugNumber,       val -> GameSettings.drawDebugNumber = val),
            createSettingItem("Draw Best Move",          () -> GameSettings.drawBestMove,          val -> GameSettings.drawBestMove = val),
            createSettingItem("Draw Neighbour",          () -> GameSettings.drawNeighbour,         val -> GameSettings.drawNeighbour = val),
            createSettingItem("Draw Evaluated Position", () -> GameSettings.drawEvaluatedPosition, val -> GameSettings.drawEvaluatedPosition = val),
            createSettingItem("Draw Heatmap Neighbour",  () -> GameSettings.drawHeatmapNeighbour,  val -> GameSettings.drawHeatmapNeighbour = val),
            createSettingItem("Draw Heatmap Score",      () -> GameSettings.drawHeatmapScore,      val -> GameSettings.drawHeatmapScore = val)
        );

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(settingsMenu);

        // menuBar.setStyle("-fx-background-color: #2c3e50;");
        // settingsMenu.setStyle("-fx-text-fill: white;");

        gameUI.getRoot().setTop(menuBar);
    }

    private CustomMenuItem createSettingItem(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        CheckBox box = new CheckBox(label);
        box.setSelected(getter.getAsBoolean());
        box.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setter.accept(newVal);
            renderer.draw();
        });

        CustomMenuItem item = new CustomMenuItem(box);
        item.setHideOnClick(false); // keeps menu open after clicking
        return item;
    }
}
