package main.java.ui;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javafx.scene.control.*;
import main.java.app.GameSettings;

public class GameUISettings {

    private final GameUI gameUI;
    private final BoardRenderer renderer;

    public GameUISettings(GameUI gameUI) {
        this.gameUI = gameUI;
        this.renderer = this.gameUI.getRenderer();

        Menu debugSettingsMenu = new Menu("Debug");
        Menu visualMenu = new Menu("Visual");
        Menu debugMouseSettingsMenu = new Menu("Debug On Mouse");
        Menu settingsMenu = new Menu("Settings");

        CustomMenuItem chessItem = createSettingItem("Chess Board", () -> GameSettings.chessBoard, val -> GameSettings.chessBoard = val );
        CustomMenuItem gomokuItem = createSettingItem("Gomoku Board", () -> GameSettings.gomokuBoard, val -> GameSettings.gomokuBoard = val );

        CheckBox chessBox = (CheckBox) chessItem.getContent();
        CheckBox gomokuBox = (CheckBox) gomokuItem.getContent();

        chessBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) gomokuBox.setSelected(false);
        });
        gomokuBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) chessBox.setSelected(false);
        });

        visualMenu.getItems().addAll(
            createSettingItem("Show Grid",              () -> GameSettings.gridToggle,        val -> GameSettings.gridToggle = val),
            createSettingItem("Show Label",             () -> GameSettings.labelToggle,       val -> GameSettings.labelToggle = val),
            createSettingItem("Label X Number Based",   () -> GameSettings.labelXNumberBase,  val -> GameSettings.labelXNumberBase = val),
            createSettingItem("Label Y Number Based",   () -> GameSettings.labelYNumberBase,  val -> GameSettings.labelYNumberBase = val),
            chessItem,
            gomokuItem,
            createSettingItem("Show Symbols",           () -> GameSettings.showSymbolToggle,  val -> GameSettings.showSymbolToggle = val)
        );
        debugSettingsMenu.getItems().addAll(
            createSettingItem("Draw Debug Number",       () -> GameSettings.drawDebugNumber,        val -> GameSettings.drawDebugNumber = val),
            createSettingItem("Draw Best Move",          () -> GameSettings.drawBestMove,           val -> GameSettings.drawBestMove = val),
            createSettingItem("Draw Evaluated Position", () -> GameSettings.drawEvaluatedPosition,  val -> GameSettings.drawEvaluatedPosition = val),
            createSettingItem("Draw Sorted Position",    () -> GameSettings.drawSortedPosition,     val -> GameSettings.drawSortedPosition = val),
            createSettingItem("Draw Heatmap Score",      () -> GameSettings.drawScoreHeatmap,       val -> GameSettings.drawScoreHeatmap = val),
            createSettingItem("Draw Score Number",       () -> GameSettings.drawScoreNumber,        val -> GameSettings.drawScoreNumber = val),
            createSettingItem("Draw Score Player Number",() -> GameSettings.drawScorePlayerNumber,  val -> GameSettings.drawScorePlayerNumber = val)
        );
        settingsMenu.getItems().addAll(
            createSettingItem("Ai Play Automatic",       () -> GameSettings.aiPlaysAutomatic,       val -> GameSettings.aiPlaysAutomatic = val),
            createSettingItem("Player 1 AI",             () -> GameSettings.player1AI,              val -> GameSettings.player1AI = val),
            createSettingItem("Player 2 AI",             () -> GameSettings.player2AI,              val -> GameSettings.player2AI = val)
        );
        debugMouseSettingsMenu.getItems().addAll(
            createSettingItem("Draw Mouse Pos",             () -> GameSettings.drawMousePos,           val -> GameSettings.drawMousePos = val),
            createSettingItem("Draw Mouse Grid Pos",        () -> GameSettings.drawMouseGridPos,       val -> GameSettings.drawMouseGridPos = val),
            createSettingItem("Draw Mouse Cell Pos",        () -> GameSettings.drawMouseCellPos,       val -> GameSettings.drawMouseCellPos = val),
            createSettingItem("Draw Sequence Data On Mouse",() -> GameSettings.drawSequenceDataOnMouse,val -> GameSettings.drawSequenceDataOnMouse = val)
        );

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(settingsMenu);
        menuBar.getMenus().add(visualMenu);
        menuBar.getMenus().add(debugSettingsMenu);
        menuBar.getMenus().add(debugMouseSettingsMenu);

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
