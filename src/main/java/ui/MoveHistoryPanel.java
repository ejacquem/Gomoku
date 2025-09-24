package main.java.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Consumer;

public class MoveHistoryPanel {

    private VBox moveList;        // VBox containing rows
    private ScrollPane scrollPane;

    // Create the scrollable panel
    public ScrollPane createMoveHistoryPanel() {
        moveList = new VBox(2);
        moveList.setPadding(new Insets(5));
        
        scrollPane = new ScrollPane(moveList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    // Create a single move row
    public HBox createMoveHistoryRow(int moveNumber, String moveText, Consumer<Integer> onClick) {
        Label numberLabel = new Label(moveNumber + ". ");
        numberLabel.setFont(Font.font("Monospaced", 12));

        Label moveLabel = new Label(moveText);
        moveLabel.setFont(Font.font("Monospaced", 12));
        moveLabel.setTextFill(Color.BLUE);

        HBox row = new HBox(5, numberLabel, moveLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2));

        // Make clickable
        row.setOnMouseClicked((MouseEvent e) -> {
            onClick.accept(moveNumber - 1); // zero-based index
        });

        // Optional: highlight on hover
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: lightgray;"));
        row.setOnMouseExited(e -> row.setStyle(""));

        return row;
    }

    // Set/update the move history
    public void setMoveHistoryData(List<String> moves, Consumer<Integer> onClick) {
        moveList.getChildren().clear();

        for (int i = 0; i < moves.size(); i++) {
            HBox row = createMoveHistoryRow(i + 1, moves.get(i), onClick);
            moveList.getChildren().add(row);
        }

        // Scroll to bottom
        scrollPane.layout(); // ensure layout is updated
        scrollPane.setVvalue(1.0);
    }
}

