package main.java.ui;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javafx.scene.control.*;
import javafx.stage.Stage;
import main.java.app.GameSettings;
import main.java.utils.FileHelper;

public class GameUISettings {

    private final GameUI gameUI;
    private final BoardRenderer renderer;
    private final Stage stage;
    private final FileHelper fileHelper;

    public GameUISettings(GameUI gameUI, Stage stage) {
        this.stage = stage;
        this.gameUI = gameUI;
        this.renderer = this.gameUI.getRenderer();
        fileHelper = new FileHelper(stage);
        createMenu();
    }

    private void createMenu(){
        Menu debugSettingsMenu = new Menu("Debug");
        Menu visualMenu = new Menu("Visual");
        Menu settingsMenu = new Menu("Settings");
        Menu fileMenu = createFileMenu();

        /* File Setting Item */

        

        /* Board setting Toggle */
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

        /* Grid Width slider */
        CustomMenuItem gridWidthLabel = new CustomMenuItem(new Label("Grid Width"), false);
        gridWidthLabel.setHideOnClick(false); // keep menu open
        Slider gridWidthSlider = new Slider(0, 10, GameSettings.gridWidth); // min, max, initial
        gridWidthSlider.setBlockIncrement(1);
        gridWidthSlider.setMinorTickCount(1);

        gridWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            GameSettings.gridWidth = Math.max(0.01, newVal.intValue());
            renderer.draw();
        });

        CustomMenuItem gridWidthItem = new CustomMenuItem(gridWidthSlider);
        gridWidthItem.setHideOnClick(false);

        /* Menu */
        visualMenu.getItems().addAll(
            createSettingItem("Show Grid",              () -> GameSettings.gridToggle,        val -> GameSettings.gridToggle = val),
            chessItem,
            gomokuItem,
            gridWidthLabel,
            gridWidthItem,
            new SeparatorMenuItem(),
            createSettingItem("Show Label",             () -> GameSettings.labelToggle,       val -> GameSettings.labelToggle = val),
            createSettingItem("Label X Number Based",   () -> GameSettings.labelXNumberBase,  val -> GameSettings.labelXNumberBase = val),
            createSettingItem("Label Y Number Based",   () -> GameSettings.labelYNumberBase,  val -> GameSettings.labelYNumberBase = val),
            new SeparatorMenuItem(),
            createSettingItem("Show Symbols",           () -> GameSettings.showSymbolToggle,  val -> GameSettings.showSymbolToggle = val)
        );
        debugSettingsMenu.getItems().addAll(
            createSettingItem("Draw Index Number",       () -> GameSettings.drawIndexNumber,        val -> GameSettings.drawIndexNumber = val),
            new SeparatorMenuItem(),
            createSettingItem("Draw Best Move",          () -> GameSettings.drawBestMove,           val -> GameSettings.drawBestMove = val),
            createSettingItem("Draw Evaluated Position", () -> GameSettings.drawEvaluatedPosition,  val -> GameSettings.drawEvaluatedPosition = val),
            createSettingItem("Draw Sorted Position",    () -> GameSettings.drawSortedPosition,     val -> GameSettings.drawSortedPosition = val),
            new SeparatorMenuItem(),
            createSettingItem("Draw Heatmap Score",      () -> GameSettings.drawScoreHeatmap,       val -> GameSettings.drawScoreHeatmap = val),
            createSettingItem("Draw Score Number",       () -> GameSettings.drawScoreNumber,        val -> GameSettings.drawScoreNumber = val),
            createSettingItem("Draw Score Player Number",() -> GameSettings.drawScorePlayerNumber,  val -> GameSettings.drawScorePlayerNumber = val),
            new SeparatorMenuItem(),
            createSettingItem("Draw Mouse Pos",             () -> GameSettings.drawMousePos,           val -> GameSettings.drawMousePos = val),
            createSettingItem("Draw Mouse Grid Pos",        () -> GameSettings.drawMouseGridPos,       val -> GameSettings.drawMouseGridPos = val),
            createSettingItem("Draw Mouse Cell Pos",        () -> GameSettings.drawMouseCellPos,       val -> GameSettings.drawMouseCellPos = val)
            );
        settingsMenu.getItems().addAll(
            createSettingItem("Analyse Board",             () -> GameSettings.analyseBoard,              val -> GameSettings.analyseBoard = val),
            new SeparatorMenuItem(),
            createSettingItem("Player 1 AI",             () -> GameSettings.player1AI,              val -> GameSettings.player1AI = val),
            createSettingItem("Player 2 AI",             () -> GameSettings.player2AI,              val -> GameSettings.player2AI = val)
        );

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(settingsMenu);
        menuBar.getMenus().add(visualMenu);
        menuBar.getMenus().add(debugSettingsMenu);

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

    public Menu createFileMenu() {
        Menu fileMenu = new Menu("File");

        MenuItem importItem = new MenuItem("Import Position");
        MenuItem exportItem = new MenuItem("Export Position");
        MenuItem exportAsItem = new MenuItem("Export Position As");
        MenuItem exportGameItem = new MenuItem("Export Game");

        importItem.setOnAction(e -> {
            try {
                String boardString = fileHelper.readFileAt();
                gameUI.importBoard(boardString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exportItem.setOnAction(e -> {
            try {
                String boardString = gameUI.exportBoard();
                fileHelper.saveFile("saves/positions/position_" + FileHelper.getTimeStampString() + ".pos", boardString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exportAsItem.setOnAction(e -> {
            try {
                String boardString = gameUI.exportBoard();
                fileHelper.saveFileAt("saves/positions", "position_", "pos", boardString);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exportGameItem.setOnAction(e -> {
            gameUI.exportGame();
        });

        fileMenu.getItems().addAll(importItem, exportItem, exportAsItem, new SeparatorMenuItem(), exportGameItem);
        return fileMenu;
    }
}
