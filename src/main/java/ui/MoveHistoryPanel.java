package main.java.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.List;

public class MoveHistoryPanel {

    private VBox moveList;        // VBox containing rows
    private ScrollPane scrollPane;
    private GameUI UI;

    // Create the scrollable panel
    public ScrollPane createMoveHistoryPanel(GameUI UI) {
        this.UI = UI;
        moveList = new VBox(2);
        moveList.setPadding(new Insets(5));
        
        scrollPane = new ScrollPane(moveList);
        scrollPane.setFitToWidth(true);
        // scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        scrollPane.getStyleClass().addAll(".scroll-pane");
        moveList.getStyleClass().addAll(".scroll-pane-parent");

        // scrollPane.vvalueProperty().bind(moveList.heightProperty());

        moveList.heightProperty().addListener((obs, oldVal, newVal) -> {
            // only auto-scroll if the user was already at the bottom
            scrollPane.setVvalue(1.0);
        });
        

        return scrollPane;
    }

    // Create a single move row
    public HBox createMoveHistoryRow(int moveNumber, String moveText, boolean isSelected) {
        Label numberLabel = new Label(moveNumber + ". ");
        numberLabel.setFont(Font.font("Monospaced", 12));
        numberLabel.setTextFill(Color.WHITE);

        int i = 0;
        while (moveText.charAt(i) != ' '){
            i++;
        }

        Label moveLabel = new Label(moveText.substring(i));
        moveLabel.setFont(Font.font("Monospaced", 12));
        moveLabel.setTextFill(Color.WHITESMOKE);

        HBox row = new HBox(5, numberLabel, moveLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2));

        // clickable row
        row.setOnMouseClicked((MouseEvent e) -> {
            System.out.println("Clicked move index: " + moveNumber);
            UI.goToMove(moveNumber);
        });

        if (isSelected) {
            row.setStyle("-fx-background-color: #303030;");
        }

        return row;
    }

    public void setMoveHistoryData(List<String> moves, int currentMove) {
        clearMoveHistoryData();

        System.out.println("setMoveHistoryData");
        System.out.println("currentMove: " + currentMove);
        for (int i = 0; i < moves.size(); i++) {
            System.out.println("i: " + i);
            HBox row = createMoveHistoryRow(i + 1, moves.get(i), currentMove == (i + 1));
            moveList.getChildren().add(row);
        }
    }

    public void clearMoveHistoryData(){
        moveList.getChildren().clear();
    }
}

